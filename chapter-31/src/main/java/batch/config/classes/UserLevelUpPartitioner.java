package batch.config.classes;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserLevelUpPartitioner implements Partitioner {

    private final UserRepository userRepository;

	public UserLevelUpPartitioner(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

    /**
     * @param gridSize 슬레이브 스텝의 사이즈
     */
	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
        long minId = userRepository.findMinId(); // 1
        long maxId = userRepository.findMaxId(); // 400000

        long targetSize = (maxId - minId) / gridSize + 1; //5000

        /**
         * partition0 : 1, 5000
         * partition1 : 5001, 5000
         * //..
         * partition3 : 35001, 40000
         */
        Map<String, ExecutionContext> result = new HashMap<>();
        long number = 0;
        long start = minId;
        long end = start + targetSize - 1;

        while (start <= maxId) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);
            if (end >= maxId) {
                end = maxId;
            }
            value.putLong("minId", start);
            value.putLong("maxId", end);
            start += targetSize;
            end += targetSize;
            number++;
        }
		return result;
	}
    
}
