package batch.config;





import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import batch.config.classes.CustomItemProcessor;
import batch.config.classes.Person;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class  CsvToH2BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    public CsvToH2BatchConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory,
                              EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Job csvToH2BatchJob() throws Exception {        
        return jobBuilderFactory.get("csvToH2BatchJob")
                .incrementer(new RunIdIncrementer())
                .start(this.csvFileToH2Step(null))
                .build();
    }

    @Bean
    @JobScope
    public Step csvFileToH2Step(@Value("#{jobParameters[allow_duplicate]?: 'true'}")Boolean isDuplicate) throws Exception {
        log.debug("job parameter isDuplicate is {}", isDuplicate);
        return this.stepBuilderFactory.get("batchStep")
                .<Person, Person>chunk(10)
                .reader(csvFileItemReader())
                .processor(new CustomItemProcessor(isDuplicate))
                .writer(JpaItemWriter())
                .build();
    }

    /** 데이터를 읽기 위한 설정을 한다. */
    private FlatFileItemReader<Person> csvFileItemReader() throws Exception {
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
                .usePersist(true)
                .build();
        item.afterPropertiesSet();
        return item;
    }

}
