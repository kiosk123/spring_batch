package batch.config;





import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import batch.config.classes.CustomItemReader;
import batch.config.classes.Person;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class  ItemReaderConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public ItemReaderConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job itemReaderJob() {        
        return jobBuilderFactory.get("chunkJob")
                .incrementer(new RunIdIncrementer())
                .start(this.customItemReaderStep())
                .build();
    }

    @Bean
    public Step customItemReaderStep() {
        return this.stepBuilderFactory.get("customItemReaderStep")
                .<Person, Person>chunk(10)
                .reader(new CustomItemReader<>(getItems()))
                .writer(itemWriter())
                .build();
    }

    
    private ItemWriter<Person> itemWriter() {
        return items -> log.info("item size: {}, {}", items.size(), items.stream().map(Person::getName).collect(Collectors.joining(",")));
    }

    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(new Person(i + 1, "test name - " + i, "test age", "test address"));
        }
        return items;
    }
    
}
