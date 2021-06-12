package batch.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SavePersonConfiguration.class, TestConfiguration.class})
public class SavePersonConfigurationTest {
    
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void test_allow_duplicate() throws Exception {
        //given
        /** 테스트 job parameter 생성 */
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("allow_duplicate", "false")
                .toJobParameters();
        //when
        /** jobLauncherTestUtils를 이용한 Job 실행  */
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        /** N개의 StepExecution을 모두 가져와서 StepExcution에 의해 Write된 총 갯수를 검증  */
        assertEquals(jobExecution.getStepExecutions().stream().mapToInt(StepExecution::getWriteCount).sum(), 22);
        
    }
}
