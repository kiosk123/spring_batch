package batch.config.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Person {
    private int id;
    private String name;
    private String age;
    private String address;
}
