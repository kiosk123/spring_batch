package batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class  HelloConfiguration {

    /** Job을 만드는 jobBuilderFactory */
    private final JobBuilderFactory jobBuilderFactory;

    /** Job에 필요한 스텝을 만든다  */
    private final StepBuilderFactory stepBuilderFactory;

    public HelloConfiguration(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job helloJob() {
                /** Job 이름은 helloJob으로 설정함 - Job이름은 스프링 배치를 실행시킬 수 있는 key이기도 함*/
        return jobBuilderFactory.get("helloJob")
                /** incrementer - Job 실행 단위 구분 */
                .incrementer(new RunIdIncrementer()) /**RunIdIncrementer는 Job이 실행할때마다 파라미터 아이디를 자동으로 생성 */

                /** Job 실행시 최초로 실행될 스텝을 설정한다.*/
                .start(this.helloStep())
                .build();
    }

    /** Job에서 사용할 Step을 선언한다. */
    @Bean
    public Step helloStep() {
        return stepBuilderFactory.get("helloStep")
                .tasklet((contribution, chunckContext) -> {
                    log.info("hello spring batch");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
