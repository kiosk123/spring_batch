package batch.config;





import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import batch.config.classes.Person;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class  ItemReaderConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    public ItemReaderConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory,
                              DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    public Job itemReaderJob() throws Exception {        
        return jobBuilderFactory.get("jdbcJob")
                .incrementer(new RunIdIncrementer())
                .start(jdbcStep())
                .build();
    }

    @Bean
    public Step jdbcStep() throws Exception {
        return stepBuilderFactory.get("jdbcStep")
                .<Person, Person>chunk(10)
                .reader(JdbcCusorItemReader())
                .writer(jdbcBatchItemWriter())
                .build();
    }

    /** JdbcBatchItemWriter 작성*/
    private ItemWriter<Person> jdbcBatchItemWriter() throws Exception {
        JdbcBatchItemWriter<Person> itemWriter = new JdbcBatchItemWriterBuilder<Person>()
                                            .dataSource(dataSource)

                                            /** Person 객체의 프로퍼티를 파라미터로 자동 생성해주는 설정*/
                                            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                                            .sql("insert into person(name, age, address) values(:name, :age, :address)")
                                            .build();
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    /** JdbcCursorItemReader 설정 */
    private JdbcCursorItemReader<Person> JdbcCusorItemReader() throws Exception {
        JdbcCursorItemReaderBuilder<Person> itembBuilder = new JdbcCursorItemReaderBuilder<Person>();
        JdbcCursorItemReader<Person> itemReader 
                = itembBuilder
                    .name("jdbcCursorItemReader")
                    
                    /** dataSource 설정 */
                    .dataSource(dataSource)

                    /** JdbcCursorItemReader는 sql 메서드로 데이터 가져옴 */
                    .sql("select id, name, age, address from person") 

                    /** row와 객체 매핑 */
                    .rowMapper((rs, rowNum) -> {
                        return new Person(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
                    })
                    .build();
        /** 객체의 프로퍼티가 모두 올바르게 설정되어 있는 지 확인 */
        itemReader.afterPropertiesSet();
        return itemReader;
    }
}
