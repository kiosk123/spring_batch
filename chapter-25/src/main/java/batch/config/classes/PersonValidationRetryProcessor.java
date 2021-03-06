package batch.config.classes;


import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersonValidationRetryProcessor implements ItemProcessor<Person, Person> {
    
    private final RetryTemplate retryTemplate;

    
	public PersonValidationRetryProcessor() {
        retryTemplate = new RetryTemplateBuilder()
                        /** 재시도 최대 횟수 */
                        .maxAttempts(3)

                        /** NotFoundNameException 발생시 재시도 함 */
                        .retryOn(NotFoundNameException.class)

                        /** RetryListener 등록 */
                        .withListener(new SaverPersonRetryListener())
                        .build();
	}


	@Override
	public Person process(Person item) throws Exception {
		return this.retryTemplate.<Person, NotFoundNameException>execute(context -> {
            //retry 콜백 - maxAttempts에 설정된 횟수만큼 retry 콜백에서 지정된 예외가 호출되면 recovery 콜백이 호출됨
            if (StringUtils.hasText(item.getName())) {
                return item;
            }
            throw new NotFoundNameException();
        }, context -> {
            //recovery 콜백
            
            item.setName("UNKNOWN");
            return item;
        });
	}

    public static class SaverPersonRetryListener implements RetryListener {

        /** Retry를 시작하는 설정 - true어야 retry 적용 */
		@Override
		public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
			return true;
		}

        /** Retry가 종료 후 호출 */
		@Override
		public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
				Throwable throwable) {
			log.info("close");
			
		}

        /** RetryTemplate에서 정의한 예외가 던져졌을 때 발생 */
		@Override
		public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
				Throwable throwable) {
			log.info("onError");
			
		}
        
    }
}
