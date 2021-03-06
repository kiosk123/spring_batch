package batch.config;


import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;

import batch.config.classes.JobParametersDecide;
import batch.config.classes.LevelUpJobExecutionListener;
import batch.config.classes.OrderStatistics;
import batch.config.classes.SaveUserTasklet;
import batch.config.classes.User;
import batch.config.classes.UserLevelUpPartitioner;
import batch.config.classes.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class PartitionStepUserConfiguration {
    private final static String JOB_NAME = "partitionUserJob";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final UserRepository userRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;
    private final TaskExecutor taskExecutor;

    public PartitionStepUserConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory,
                              UserRepository userRepository,
                              EntityManagerFactory entityManagerFactory,
                              DataSource dataSource,
                              TaskExecutor taskExecutor) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.userRepository = userRepository;
        this.entityManagerFactory = entityManagerFactory;
        this.dataSource = dataSource;
        this.taskExecutor = taskExecutor;
    }

    @Bean(JOB_NAME)
    public Job userJob() throws Exception {
        return this.jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .start(this.saveUserStep())

                /** Slave Step ?????? Mester Step??? Job??? ?????? */
                .next(this.userLevelUpManagerStep())
                //.next(this.userLevelUpStep())
                .listener(new LevelUpJobExecutionListener(userRepository))

                /** ?????? ?????? ???????????? jobParameter date key?????? ?????? ??? ?????? */
                .next(new JobParametersDecide("date"))

                /** JobParametersDecide?????? ?????? ???????????? CONTINUE?????? */
                .on(JobParametersDecide.CONTINUE.getName())

                /** orderStatisticsStep */
                .to(this.orderStatisticsStep(null))
                .build()
                .build();
    }

    @Bean
    @JobScope
    public Step orderStatisticsStep(@Value("#{jobParameters[date]}") String date) throws Exception  {
        log.debug("<<<<<<<<<<<<<<<< date : {} >>>>>>>>>>>>>>>>>" , date);
        return this.stepBuilderFactory.get(JOB_NAME + "_orderStatisticsStep")
                .<OrderStatistics, OrderStatistics>chunk(1000)
                .reader(orderStatisticsItemReader(date))
                .writer(orderStatisticsItemWriter(date))
                .build();
    }

    private ItemReader<? extends OrderStatistics> orderStatisticsItemReader(String date) throws Exception {
        /** ????????? 12??? ?????? ?????? */
        YearMonth yearMonth = YearMonth.parse(date);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", yearMonth.atDay(1)); /** 12??? 1??? */
        parameters.put("endDate", yearMonth.atEndOfMonth());/** 12??? 31??? */

        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("created_date", Order.ASCENDING); /**created_date ????????? ???????????? ?????? */

        /** JDBC PagingItemReader??? ????????? ?????? ?????? */
        JdbcPagingItemReader<OrderStatistics> itemReader = new JdbcPagingItemReaderBuilder<OrderStatistics>()
                            .dataSource(this.dataSource)
                            .rowMapper((resultSet, i) -> 
                                OrderStatistics.builder()
                                .amount(resultSet.getString(1))
                                .date(LocalDate.parse(resultSet.getString(2), DateTimeFormatter.ISO_DATE))
                                .build())
                            .pageSize(100)
                            /**?????? ?????? ?????? */
                            .name("orderStatisticsItemReader")
                            .selectClause("sum(amount), created_date")
                            .fromClause("orders")
                            .whereClause("created_date >= :startDate and created_date <= :endDate")
                            .groupClause("created_date")
                            .parameterValues(parameters)
                            .sortKeys(sortKey)
                            .build();

        itemReader.afterPropertiesSet();
		return itemReader;
	}

	private ItemWriter<? super OrderStatistics> orderStatisticsItemWriter(String date) throws Exception {
        YearMonth yearMonth = YearMonth.parse(date);
        String fileName = yearMonth.getYear() + "???_" + yearMonth.getMonthValue() + "???_??????_??????_??????.csv";
        BeanWrapperFieldExtractor<OrderStatistics> fieldExtractor = new BeanWrapperFieldExtractor<OrderStatistics>();
        fieldExtractor.setNames(new String[] {"amount", "date"});

        DelimitedLineAggregator<OrderStatistics> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);
        FlatFileItemWriter<OrderStatistics> itemWriter = new FlatFileItemWriterBuilder<OrderStatistics>()
                    .resource(new FileSystemResource("output/" + fileName))
                    .lineAggregator(lineAggregator)
                    .name("orderStatisticsItemWriter")
                    .encoding("UTF-8")
                    .headerCallback(writer -> writer.write("total_amount.date"))
                    .build();
        itemWriter.afterPropertiesSet();
        return itemWriter;
	}

	@Bean
    public Step saveUserStep() throws Exception {
        return this.stepBuilderFactory.get(JOB_NAME  + "_saveUserStep")
                .tasklet(new SaveUserTasklet(userRepository))
                .build();
    }

    /** Slave Step */
    @Bean(JOB_NAME + "_userLevelUpStep")
    public Step userLevelUpStep() throws Exception {
        return this.stepBuilderFactory.get(JOB_NAME + "_userLevelUpStep")
                .<User, Future<User>>chunk(100)
                .reader(itemReader(null, null))
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }


    /**
     * Master Step
     */
    @Bean(JOB_NAME + "_userLevelUpStep.manager")
    public Step userLevelUpManagerStep() throws Exception {
        return this.stepBuilderFactory.get(JOB_NAME + "_userLevelUpStep.manager")

                    /** Partitioner ????????? ??????*/
                    .partitioner(JOB_NAME + "_userLevelUpStep", new UserLevelUpPartitioner(userRepository))

                    /** Slave Step ?????? */
                    .step(userLevelUpStep())
                    .partitionHandler(taskExecutorPartitionHandler())
                    .build();
    }

    @Bean
    PartitionHandler taskExecutorPartitionHandler() throws Exception {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        /** Slave Step ?????? */
        handler.setStep(userLevelUpStep());

        /** taskExecutor ?????? */
        handler.setTaskExecutor(this.taskExecutor);

        /** Partitioner?????? ????????? gridSize */
        handler.setGridSize(8);
		return handler;
	}

	/** 
     * Slave Step?????? ????????? ItemReader
     * ItemReader??? ????????? ?????? ??? @StepScope??? ????????????. 
     * UserLevelUpPartitioner?????? ????????? Step??? ExecutionContext??? ItemReader??? ???????????? ????????? ??????
     * 
     * @StepScope??? ???????????? ??????????????? ????????? ????????? ?????? ???????????? ??????????????? ????????? ?????????????????????.
     */
    @Bean
    @StepScope
    JpaPagingItemReader<? extends User> itemReader(@Value("#{stepExecutionContext[minId]}")Long minId, 
                                          @Value("#{stepExecutionContext[maxId]}")Long maxId) throws Exception {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("minId", minId);
        parameters.put("maxId", maxId);

        JpaPagingItemReader<User> itemReader = new JpaPagingItemReaderBuilder<User>()
                .queryString("select u from User u where u.id between :minId and :maxId")
                .entityManagerFactory(entityManagerFactory)
                .parameterValues(parameters)
                .pageSize(100)

                /** name ?????? ????????? */
                .name("userItemReader")
                .build();
        itemReader.afterPropertiesSet();
        return itemReader;
    }

    /** AsyncItemProcessor */
    private AsyncItemProcessor<User, User> itemProcessor() {
        ItemProcessor<User, User> itemProcessor = user -> {
            if (user.availableLevelUp()) {
                return user;
            }

            return null;
        };
    
        AsyncItemProcessor<User, User> asyncItemProcessor = new AsyncItemProcessor<User, User>();
        asyncItemProcessor.setDelegate(itemProcessor);
        asyncItemProcessor.setTaskExecutor(this.taskExecutor);
        return asyncItemProcessor;
    }

    /** AsyncItemWriter */
    private AsyncItemWriter<User> itemWriter() {
        ItemWriter<User> itemWriter = users -> {
            users.forEach(x -> {
                x.levelUp();
                userRepository.save(x);
            });
        };

        AsyncItemWriter<User> asyncItemWriter = new AsyncItemWriter<User>();
        asyncItemWriter.setDelegate(itemWriter);
        return asyncItemWriter;
    }
}