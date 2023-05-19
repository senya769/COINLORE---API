package tokens.info.task.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tokens.info.task.TaskApplication;
import tokens.info.task.controller.Token;
import tokens.info.task.dto.ResponseTokenDto;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TokenService implements TokenDAO {
    private final String QUERY_UPDATE_TOKENS = "UPDATE test.tokens set symbol=?, price_usd=?, last_update=? where id = ?";
    private final String QUERY_SELECT_TOKEN_BY_ID = "select * from test.tokens where id = ?";
    private final String QUERY_SELECT_TOKENS = "select * from test.tokens";

    private final Logger LOGGER = LoggerFactory.getLogger(TaskApplication.class.getName());
    private final String DB_URL = "jdbc:mysql://localhost:3306/test";
    private final String DB_USERNAME = "root";
    private final String DB_PASSWORD = "200357707";
    private final String COINLORE_API_URL = "https://api.coinlore.net/api/ticker/id=?";

    private Map<Long, String> notifyUsers = new HashMap<>();
    private final List<Token> availableTokens = new ArrayList<>();

    {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            try (PreparedStatement stmt = conn.prepareStatement(QUERY_SELECT_TOKENS)) {
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime localDateTime = LocalDateTime.parse(resultSet.getString("last_update"), formatter);
                    Token newToken = Token.builder()
                            .id(resultSet.getLong("id"))
                            .symbol(resultSet.getString("symbol"))
                            .priceUsd(resultSet.getDouble("price_usd"))
                            .lastUpdated(localDateTime)
                            .build();
                    availableTokens.add(newToken);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<ResponseTokenDto> findById(long id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {

            try (PreparedStatement stmt = conn.prepareStatement(QUERY_SELECT_TOKEN_BY_ID)) {
                stmt.setLong(1, id);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    String lastUpdate = resultSet.getString("last_update");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime localDateTime = LocalDateTime.parse(lastUpdate, formatter);


                    ResponseTokenDto build = ResponseTokenDto.builder()
                            .idToken(id)
                            .symbol(resultSet.getString("symbol"))
                            .priceUsd(resultSet.getDouble("price_usd")).build();
                    return Optional.of(build);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Token> showAvailableTokens() {
        return this.availableTokens;
    }

    //    @Scheduled(cron = "0 * * * * *")
    private void saveCryptoPrices(Token tickers) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {

            try (PreparedStatement stmt = conn.prepareStatement(QUERY_UPDATE_TOKENS)) {
                stmt.setString(1, tickers.getSymbol());
                double priceUsd = tickers.getPriceUsd();
                stmt.setDouble(2, priceUsd);
                LocalDateTime dateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = dateTime.format(formatter);
                stmt.setString(3, formattedDateTime);
                stmt.setLong(4, tickers.getId());

                stmt.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkValidPrice(double newPrice, long id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            try (PreparedStatement stmt = conn.prepareStatement(QUERY_SELECT_TOKEN_BY_ID)) {
                stmt.setLong(1, id);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    double oldPrice = resultSet.getDouble("price_usd");
                    double percentChangedValue = ((oldPrice - newPrice) / oldPrice) * 100;
                    if (Math.abs(percentChangedValue) >= 1) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public void registrUsernameNotify(String username, long idToken) {
        Optional<ResponseTokenDto> byId = findById(idToken);
        if (byId.isPresent()) {
            this.notifyUsers.put(byId.get().getIdToken(), username);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void sss() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            for (Token token : availableTokens) {
                Token token1 = restTemplate.getForObject(COINLORE_API_URL + token.getId(), Token[].class)[0];
                if (checkValidPrice(token1.getPriceUsd(), token1.getId())) {
                    String s = this.notifyUsers.get(token1.getId());
                    if (s != null) {
                        LOGGER.warn(s + " change VALUE: ".concat(token1.getSymbol())
                                .concat(" new price: ")
                                .concat(String.valueOf(token1.getPriceUsd()))
                        );
                    }
                }
                saveCryptoPrices(token1);
                LOGGER.info("UPDATE!  " + token1);
            }
            LOGGER.info("Цены криптовалют успешно обновлены!  ");
        } catch (Exception e) {
            LOGGER.error("Ошибка при обновлении цен криптовалют: " + e.getMessage());
        }
    }
}


