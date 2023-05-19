package tokens.info.task.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseTokenDto {
    @JsonAlias("price_usd")
    private double priceUsd;
    private String symbol;
    private long idToken;
}
