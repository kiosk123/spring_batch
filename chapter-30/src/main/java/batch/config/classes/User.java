package batch.config.classes;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    
    @Enumerated(EnumType.STRING)
    private Level level = Level.NORMAL;

    /** 멀티스레드 환경에서 Lazy설정된 엔티티에 접근하면 Lazy로딩 에러가 발생할 수 있기 때문에 변경*/
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    private List<Orders> orders;
    private LocalDate updatedDate;

    @Builder
    private User(String username, List<Orders> orders) {
        this.username = username;
        this.orders = orders;
    }

    /** 이부분 때문에 List<Orders>를 EAGER로 변경 */
    private int getTotalAmount() {
        return this.orders.stream()
                .mapToInt(Orders::getAmount)
                .sum();
    }

    public boolean availableLevelUp() {
        return Level.availableLevelUp(this.getLevel(), this.getTotalAmount());
    }

    public void levelUp() {
        Level nextLevel = Level.getNextLevel(this.getTotalAmount());
        this.level = nextLevel;
        this.updatedDate = LocalDate.now();
    }
}
