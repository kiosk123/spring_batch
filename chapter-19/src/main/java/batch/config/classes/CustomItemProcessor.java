package batch.config.classes;

import java.util.HashSet;
import java.util.Set;

import org.springframework.batch.item.ItemProcessor;

public class CustomItemProcessor implements ItemProcessor<Person, Person>{

    private Boolean isDuplicate;
    private Set<String> personNamesSet;

    public CustomItemProcessor(Boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
        personNamesSet = new HashSet<String>();
    }

    @Override
    public Person process(Person person) throws Exception {
        if (!isDuplicate) {
            if (!personNamesSet.contains(person.getName())) {
                personNamesSet.add(person.getName());
                return person;
            } else {
                return null;
            }
        } else {
            return person;
        }
      
    }
}
