package batch.config.classes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import batch.config.classes.User.UserBuilder;

public class SaveUserTasklet implements Tasklet {
        
    private final UserRepository userRepository;

	public SaveUserTasklet(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		List<User> users = createUsers();
        Collections.shuffle(users);
        userRepository.saveAll(users);
		return RepeatStatus.FINISHED;
	}

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();
        /** 등급별 회원 가격정보 세팅 */
        for (int i = 1; i <= 50000; i++) {
            UserBuilder userbuilder= User.builder();
            if (i <= 10000) {
                userbuilder.orders(Collections.singletonList(Orders
                        .builder()
                        .amount(1000)
                        .createdDate(LocalDate.of(2020, 11, 5))
                        .itemName("item" + i)
                        .build()));
            }
            else if ( i <= 20000) {
                userbuilder.orders(Collections.singletonList(Orders
                .builder()
                .amount(200000)
                .createdDate(LocalDate.of(2020, 11, 1))
                .itemName("item" + i)
                .build()));
            }
            else if ( i <= 30000) {
                userbuilder.orders(Collections.singletonList(Orders
                .builder()
                .amount(300000)
                .createdDate(LocalDate.of(2020, 11, 2))
                .itemName("item" + i)
                .build()));
            }
            else if ( i <= 40000) {
                userbuilder.orders(Collections.singletonList(Orders
                .builder()
                .amount(400000)
                .createdDate(LocalDate.of(2020, 11, 3))
                .itemName("item" + i)
                .build()));
            }
            else {
                userbuilder.orders(Collections.singletonList(Orders
                .builder()
                .amount(500000)
                .createdDate(LocalDate.of(2020, 11, 4))
                .itemName("item" + i)
                .build()));
            }
            userbuilder.username("test username" + i);
            User user = userbuilder.build();
            users.add(user);
        }
        return users;
    }
    
}
