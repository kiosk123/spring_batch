package batch.config;





import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class  ChunkProcessingConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public ChunkProcessingConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job chunkJob() {        
        return jobBuilderFactory.get("chunkJob")
                .incrementer(new RunIdIncrementer())
                .start(this.taskBaseStep(null))
                .start(this.chunkBaseStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step chunkBaseStep(@Value("#{jobParameters[chunkSize]}") String chunkSize) {
        return stepBuilderFactory.get("chunkBaseStep")

                .<String, String>chunk(10)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }
    
    private ItemReader<? extends String> itemReader() {
        /** ???????????? ???????????? ListItemReader ????????? */
        return new ListItemReader<>(getItems());
    }

    private ItemProcessor<? super String, ? extends String> itemProcessor() {
        /** ItemReader?????? ?????? ????????? ?????? */
        return item -> item + ", Spring Batch";
    }

    private ItemWriter<? super String> itemWriter() {
        /** ItemProcess??? ?????? ?????? ????????? ?????? - ??????????????? DB??? ???????????? ????????? ????????? ???????????? ??????*/
        /** ???????????? 100?????? 10?????? ??????????????? size?????? 10??? 10??? ?????? */
        return items -> log.info("chunk item size : {}", items.size());
    }

    /** tasklet ?????? */
    @Bean
    @StepScope
    public Step taskBaseStep(@Value("#{jobParameters[chunkSize]}") String value) {
        return stepBuilderFactory.get("taskBaseStep")
                .tasklet(this.tasklet(value))
                .build();
    }

    
    private Tasklet tasklet(String value) {
        List<String> items = getItems();

        return (contribution, chunkContext) -> {
            StepExecution stepExecution = contribution.getStepExecution();
            JobParameters jobParameters = stepExecution.getJobParameters();

            int chunkSize = StringUtils.hasText(value) ? Integer.parseInt(value) : 10;
            int fromIndex = stepExecution.getReadCount();
            int toIndex = fromIndex + chunkSize;

            if (fromIndex >= items.size()) {
                return RepeatStatus.FINISHED;
            }

            List<String> subList = items.subList(fromIndex, toIndex);
            log.info("task item size: {}",  subList.size());

            return RepeatStatus.CONTINUABLE;
        };
    }

    private List<String> getItems() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(i + "hello");
        }
        return items;
    }
    
}
