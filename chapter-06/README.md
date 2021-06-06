# 06. ExecutionContext 이해

JobExecutionContext를 통해서 Step끼리 데이터를 공유할 수 있고  
StepExecutionContext를 이용해서 하나의 Step 내에서 데이터를 공유할 수 있다.

![5.png](./img/9.jpg)
![6.png](./img/10.png)

```java
@Configuration
@Slf4j
public class  SharedConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    public SharedConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job sharedJob() {        
        return jobBuilderFactory.get("sharedJob")
                .incrementer(new RunIdIncrementer())
                /** start -> next -> next 순으로 job의 step 실행 */
                .start(this.sharedStep())
                .next(this.sharedStep2()) 
                .build();
    }

    @Bean
    public Step sharedStep() {
        return stepBuilderFactory.get("sharedStep")
                .tasklet((contribution, chunkContext) -> {
                    StepExecution stepExecution = contribution.getStepExecution();
                    ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();

                    /** stepExecutionContext 데이터를 넣음 */
                    stepExecutionContext.putString("stepKey", "step execution context");

                    JobExecution jobExecution = stepExecution.getJobExecution();
                    JobInstance jobInstance = jobExecution.getJobInstance();
                    ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();

                    /** jobExecutionContext에 데이터를 넣음 */
                    jobExecutionContext.putString("jobKey", "job execution context");
                    JobParameters jobParameters = jobExecution.getJobParameters();

                    log.info("job name : {}, step name : {}, parameter : {}",
                            jobInstance.getJobName(),
                            stepExecution.getStepName(),
                            jobParameters.getLong("run.id"));
                    
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step sharedStep2() {
        return stepBuilderFactory.get("sharedStep2")
                .tasklet((contribution, chunkContext) -> {
                    StepExecution stepExecution = contribution.getStepExecution();
                    ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();

                    JobExecution jobExecution = stepExecution.getJobExecution();
                    ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();

                    /** sharedStep에서  sharedStep2에 넘긴 데이터 로그 확인*/
                    log.info("job key : {}, step key : {}",
                                jobExecutionContext.getString("jobKey", "emptyJobKey"),
                                stepExecutionContext.getString("stepKey", "emptyStepKey"));
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
```