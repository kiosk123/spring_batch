package batch.config;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import batch.config.classes.DuplicateValidationProcessor;
import batch.config.classes.NotFoundNameException;
import batch.config.classes.Person;
import batch.config.classes.PersonValidationRetryProcessor;
import batch.config.classes.SavePersonListener;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SavePersonConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public SavePersonConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory,
                              EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job savePersonJob() throws Exception {
        return this.jobBuilderFactory.get("savePersonJob")
                .incrementer(new RunIdIncrementer())
                .start(this.savePersonStep(null))
                /** 리스너 등록 - 여러개 등록 가능하며 등록한 순으로 실행 */
                .listener(new SavePersonListener.SavePersonJobExecutionListener())
                .listener(new SavePersonListener.SavePersonAnnotationJobExecutionListener())
                .build();
    }

    @Bean
    @JobScope
    public Step savePersonStep(@Value("#{jobParameters[allow_duplicate]?: 'true'}")Boolean allowDuplicate) throws Exception {
        return this.stepBuilderFactory.get("savePersonStep")
                .<Person, Person>chunk(10)
                .reader(itemReader())
                .processor(itemProcessor(allowDuplicate))
                .writer(JpaItemWriter())
                .listener(new SavePersonListener.SavepersonStepExecutionListener())
                .faultTolerant()
                .skip(NotFoundNameException.class)
                .skipLimit(3)
                /** retry 또한 faultTolerant를 호출한 다음에 설정해야한다. */
                .retry(NotFoundNameException.class)
                .retryLimit(3)
                .build();
    }

    private ItemProcessor<? super Person, ? extends Person> itemProcessor(Boolean allowDuplicate) throws Exception {
        DuplicateValidationProcessor<Person> duplicateValidationProcessor
            = new DuplicateValidationProcessor<>(Person::getName, allowDuplicate);

        /** 이름 데이터가 비어있을 경우 NotFoundNameException 발생*/
        ItemProcessor<Person, Person> validationProcessor = item -> {
            if (StringUtils.hasText(item.getName())) {
                return item;
            }
            throw new NotFoundNameException();
        };

        /** ItemProcessor가 여러개일 경우 묶는 역할을 한다. */
        CompositeItemProcessor<Person, Person> itemProcessor = new CompositeItemProcessorBuilder<Person, Person>()
                /** 등록한 순으로 ItemProcessor 실행 */
                .delegates(new PersonValidationRetryProcessor(), validationProcessor, duplicateValidationProcessor)
                .build();

        itemProcessor.afterPropertiesSet();
        return itemProcessor;
    }

    private ItemReader<? extends Person> itemReader() throws Exception {
        /** CSV 파일을 한 줄씩 읽기 위한 설정*/
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();

        /** CSV 파일을 Person 객체와 매핑하기 위해 Person 필드명을 설정 */
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();

        /** Person의 필드명을 설정한다. */
        tokenizer.setNames("id", "name", "age", "address");
        lineMapper.setLineTokenizer(tokenizer);

        /** CSV 라인의 값을 Person 객체와 매핑 */
        lineMapper.setFieldSetMapper(fieldSet -> {
            int id = fieldSet.readInt("id");
            String name = fieldSet.readString("name");
            String age = fieldSet.readString("age");
            String address = fieldSet.readString("address");
            return new Person(id, name, age, address);
        });

        FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
            .name("csvFileItemReader")
            .encoding("UTF-8")
            .resource(new ClassPathResource("test.csv"))
            .linesToSkip(1) /** 첫번째 라인은 skip하고 읽는 설정 */
            .lineMapper(lineMapper)
            .build();

        /** itemReader 필수 프로퍼티가 잘 설정되어 있는지 검사 */
        itemReader.afterPropertiesSet();
        return itemReader;
    }

    /** JpaItemWriter 생성 */
    private ItemWriter<Person> JpaItemWriter() throws Exception {
        JpaItemWriter<Person> item = new JpaItemWriterBuilder<Person>()
                .entityManagerFactory(entityManagerFactory)
                /** 
                 * @Id로 설정된 프로퍼티 값을 직접 설정 할때 
                 * 별다른 설정 없이 실행할 경우 entityManager의 merge 메서드를 호출
                 * @Id로 설정된 값이 이미 디비에 있는지 비교하고 없으면 insert 있으면 update 하기 때문에
                 * 단순 insert인 경우 select 쿼리를 하게 되므로 실제 쿼리 요청은 2배
                 * 이때 usePersist(true)를 설정하면 select쿼리 없이 insert한다 
                 */
                .usePersist(false)
                .build();
        item.afterPropertiesSet();
        return item;
    }
}
