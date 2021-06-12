package batch.config.classes;

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
        for (int i = 1; i <= 500; i++) {
            UserBuilder userbuilder= User.builder();
            if (i <= 100) {
                userbuilder.totalAmount(10000);
            }
            else if ( i <= 200) {
                userbuilder.totalAmount(200000);
            }
            else if ( i <= 300) {
                userbuilder.totalAmount(300000);
            }
            else if ( i <= 400) {
                userbuilder.totalAmount(400000);
            }
            else {
                userbuilder.totalAmount(500000);
            }
            userbuilder.username("test username" + i);
            User user = userbuilder.build();
            users.add(user);
        }
        return users;
    }
    
}
