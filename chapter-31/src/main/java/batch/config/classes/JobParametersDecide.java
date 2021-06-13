package batch.config.classes;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.util.StringUtils;

public class JobParametersDecide implements JobExecutionDecider {

    /** Status를 커스텀하게 재정의 */
    public static final FlowExecutionStatus CONTINUE = new FlowExecutionStatus("CONTINUE");

    /** jobParameters key에 대한 값이 있는 지 없는 지 판단 */
    private final String key;

	public JobParametersDecide(String key) {
		this.key = key;
	}

	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		String value = jobExecution.getJobParameters().getString(key);

        if (!StringUtils.hasText(value)) {
            return FlowExecutionStatus.COMPLETED;
        }
		return CONTINUE;
	}
    
}
