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
        /** 리스트를 이용하셔 ListItemReader 초기화 */
        return new ListItemReader<>(getItems());
    }

    private ItemProcessor<? super String, ? extends String> itemProcessor() {
        /** ItemReader에서 읽은 것들을 처리 */
        return item -> item + ", Spring Batch";
    }

    private ItemWriter<? super String> itemWriter() {
        /** ItemProcess를 거친 것을 로그로 출력 - 실무에서는 DB에 데이터를 넣거나 파일을 생성하는 처리*/
        /** 여기서는 100개를 10개씩 처리하므로 size값이 10이 10번 찍힘 */
        return items -> log.info("chunk item size : {}", items.size());
    }

    /** tasklet 스텝 */
    @Bean
    @JobScope
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
