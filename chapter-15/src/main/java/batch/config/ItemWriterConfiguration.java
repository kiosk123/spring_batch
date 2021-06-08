package batch.config;


import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import batch.config.classes.CustomItemReader;
import batch.config.classes.Person;

@Configuration
public class ItemWriterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public ItemWriterConfiguration(JobBuilderFactory jobBuilderFactory, 
                                   StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job ItemWriterJob() throws Exception {
        return this.jobBuilderFactory.get("itemWriterJob")
                .incrementer(new RunIdIncrementer())
                .start(this.csvItemWriterStep())
                .build();
    }

    @Bean
    public Step csvItemWriterStep() throws Exception {
        return this.stepBuilderFactory.get("csvItemWriterStep")
                .<Person, Person>chunk(10)
                .reader(itemReader())
                .writer(csvFileItemWriter())
                .build();
    }

    private ItemWriter<? super Person> csvFileItemWriter() throws Exception {
        /** FlatFileItemWriter는 CSV에 작성에 필요한 데이터를 추출하기 위해 FieldExtractor가 필요 */
        BeanWrapperFieldExtractor<Person> fieldExtractor = new BeanWrapperFieldExtractor<>();

        /** CSV파일에 저장할 순서로 Person의 프로퍼티 명을 매핑한다.*/
        fieldExtractor.setNames(new String[]{"id", "name", "age", "address"});
        
        /** 각 필드의 데이터를 하나의 라인에 설정하기 위해서 구분값을 설정 */
        DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FlatFileItemWriter<Person> itemWriter = new FlatFileItemWriterBuilder<Person>()
                        .name("csvFileItemWriter")
                        .encoding("UTF-8")
                        /** 파일 생성을 위해서는 FileSystemResource를 사용*/
                        .resource(new FileSystemResource("output/test-output.csv"))
                        
                        /** lineAggregator 설정 */
                        .lineAggregator(lineAggregator)

                        /** CSV 파일의 헤더 생성 */
                        .headerCallback(writer -> writer.write("id,이름,거주지,나이,거주지"))

                        /** CSV 파일의 풋터 생성 */
                        .footerCallback(writer -> writer.write("--------------------------\n"))

                        /** CSV 파일의 내용을 쓰는 대상이 동일할 경우 기존 파일의 데이터는 그대로 있고 새로운 내용만 추가*/
                        .append(true)
                        .build();
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    private ItemReader<Person> itemReader() {
        return new CustomItemReader<>(getItems());
    }

    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(new Person(i + 1, "test name - " + i, "test age", "test address"));
        }
        return items;
    }
}
