package batch.config.classes;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderStatistics {

    private String amount;
    private LocalDate date;

    @Builder
    private OrderStatistics(String amount, LocalDate date) {
        this.amount = amount;
        this.date = date;
    }

}