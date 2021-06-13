package batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableBatchProcessing
public class BatchApplication {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(BatchApplication.class, args)));
	}

	@Bean
	@Primary /** boot에서 기본적으로 TaskExecutor빈이 존재하기 때문에 @Bean 기본빈으로 설정*/
	TaskExecutor taskExecutor() {

		/** ThreadPoolTaskExecutor - 미리 스레드풀을 만들어서 풀에서 스레드를 꺼내쓴다 */
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(10); /** 풀의 기본 크기 */
		taskExecutor.setMaxPoolSize(20); /** 최대 풀 크기 */
		taskExecutor.setThreadNamePrefix("batch-thread-"); /** 쓰레드 풀의 쓰레드 이름 접두어 설정 */
		taskExecutor.initialize();
		return taskExecutor;
	}
}
