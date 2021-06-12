package batch.config.classes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import lombok.extern.slf4j.Slf4j;

/** 자바 컬렉션의 리스트를 Reader로 처리하는 ItemReader */
@Slf4j
public class CustomItemReader<T> implements ItemReader<T> {

    private final List<T> items;

	public CustomItemReader(List<T> items) {
		this.items = new ArrayList<>(items);
	}

	@Override
	public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (!items.isEmpty()) {
            log.info("read items size : {}", items.size());
            T t = items.remove(0);

            log.info(t.toString());
            return t;
        }
        /** null 반환하면 chunk 반복의 끝을 나타냄 */
		return null;
	}
}
