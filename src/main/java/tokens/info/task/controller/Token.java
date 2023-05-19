package tokens.info.task.controller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token implements Serializable {
    private long id;
    private String symbol;
    @JsonAlias("price_usd")
    private double priceUsd;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public Token(int i, String btc) {
        this.id = i;
        this.symbol = btc;
    }
}
