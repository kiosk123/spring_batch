package batch.config.classes;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;
import org.springframework.util.StringUtils;

public class PersonValidationRetryProcessor implements ItemProcessor<Person, Person> {
    
    private final RetryTemplate retryTemplate;

    
	public PersonValidationRetryProcessor() {
        retryTemplate = new RetryTemplateBuilder()
                        /** 재시도 최대 횟수 */
                        .maxAttempts(3)

                        /** NotFoundNameException 발생시 재시도 함 */
                        .retryOn(NotFoundNameException.class)
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
}
