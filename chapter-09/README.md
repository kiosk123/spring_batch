# 09. @JobScope와 @StepScope 이해

@JobScope와 @StepScope를 설정 후 메서드 파라미터에 `#{jobParameters[chunkSize]}` 같이 설정한다면,  
프로그램 실행시 arguments로 `--chunkSize=20`형태로 넘겼을 경우 `chunkSize`에 대한 값을 읽을 수 있다. 

![](./img/1.png)


## 예제코드
```java
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
    public Step chunkBaseStep(@Value("#{jobParameters[chunkSize]}") String value) {
        int chunkSize = StringUtil.hasText(value) ? Integer.parseInt(value) : 10;
        return stepBuilderFactory.get("chunkBaseStep")

                .<String, String>chunk(chunkSize)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }
    
    // 생략...

    /** tasklet 스텝 */
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
```