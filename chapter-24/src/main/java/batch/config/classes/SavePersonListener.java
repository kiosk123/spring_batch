package batch.config.classes;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.annotation.BeforeStep;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SavePersonListener {

    public static class SavepersonStepExecutionListener {
        @BeforeStep
        public void beforeStep(StepExecution stepExecution) {
            log.info("beforeStep");
        }

        @AfterStep
        public ExitStatus afterStep(StepExecution stepExecution) {
            log.info("after step : {}", stepExecution.getWriteCount());

            /** 스프링 배치에서는 내부적으로 Step이 실패하거나 종료할 때 상태를 StepExecution에 저장한다. */
            /** 프로그래머가 직접 ExistStatus 상태를 다음과 같이 결정할 수도 있다.*/
            if (stepExecution.getWriteCount() == 0) {
                return ExitStatus.FAILED;
            }
            return stepExecution.getExitStatus();
        }
    }


    /** 인터페이스 구현 기반 */
    public static class SavePersonJobExecutionListener implements JobExecutionListener {

        /** Job 수행 전 실행 */
		@Override
		public void beforeJob(JobExecution jobExecution) {
			log.info("beforeJob");
			
		}

        /** Job 수행 후 실행 */
		@Override
		public void afterJob(JobExecution jobExecution) {
            int sum = jobExecution.getStepExecutions()
                            .stream()
                            .mapToInt(StepExecution::getWriteCount)
                            .sum();

			log.info("afterJob : {} ", sum);
			
		}
        
    }

    /** 애너테이션 기반 */
    public static class SavePersonAnnotationJobExecutionListener {
        
        /** Job 수행 전 실행 */
		@BeforeJob
		public void beforeJob(JobExecution jobExecution) {
			log.info("beforeJob");
			
		}

        /** Job 수행 후 실행 */
		@AfterJob
		public void afterJob(JobExecution jobExecution) {
            int sum = jobExecution.getStepExecutions()
                            .stream()
                            .mapToInt(StepExecution::getWriteCount)
                            .sum();

			log.info("afterJob : {} ", sum);
			
		}
    }
}
