package batch.config;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import batch.config.classes.PersonRepository;

@SpringBatchTest /** @JobScope나 @StepScope로 설정된 부분이 올바르게 동작하기 위해 필요 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SavePersonConfiguration.class, TestConfiguration.class})
public class SavePersonConfigurationTest {
    
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PersonRepository personRepository;

    /** 영속성 컨텍스트의 데이터를 공유하는 것을 방지하기 위함 */
    @After
    public void tearDown() throws Exception {
        personRepository.deleteAll();
    }

    /** Step에서 Write된 총 데이터 갯수 검증 */
    @Test
    public void test_allow_duplicate() throws Exception {
        //given
        /** 테스트 job parameter 생성 */
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("allow_duplicate", "true")
                .toJobParameters();
        //when
        /** jobLauncherTestUtils를 이용한 Job 실행  */
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        /** N개의 StepExecution을 모두 가져와서 StepExcution에 의해 Write된 총 갯수를 검증  */
        int writeCount = jobExecution.getStepExecutions().stream().mapToInt(StepExecution::getWriteCount).sum();
        assertEquals(100, writeCount);

        /** DB에서 가져온 데이터 갯수 검증 */
        int readCount = (int)personRepository.count();
        assertEquals(100, readCount);
    }

    @Test
    public void test_allow_not_duplicate() throws Exception {
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
        int writeCount = jobExecution.getStepExecutions().stream().mapToInt(StepExecution::getWriteCount).sum();
        assertEquals(22, writeCount);

        /** DB에서 가져온 데이터 갯수 검증 */
        int readCount = (int)personRepository.count();
        assertEquals(22, readCount);
    }


    /** Step 테스트 */
    @Test
    public void test_step() {
        /** step에서도 job parameter 넘길 수 있음 - launchStep(String stepName, JobParameters jobParameters)*/
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("savePersonStep");
        
        /** N개의 StepExecution을 모두 가져와서 StepExcution에 의해 Write된 총 갯수를 검증  */
        int writeCount = jobExecution.getStepExecutions().stream().mapToInt(StepExecution::getWriteCount).sum();
        assertEquals(100, writeCount);

        /** DB에서 가져온 데이터 갯수 검증 */
        int readCount = (int)personRepository.count();
        assertEquals(100, readCount);
    }
}
