package batch.config.classes;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Person {
    
    @Id
    private Integer id;
    private String name;
    private String age;
    private String address;

    public Person(String name, String age, String address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }
}
