package tokens.info.task.service;


import tokens.info.task.controller.Token;
import tokens.info.task.dto.ResponseTokenDto;

import java.util.List;
import java.util.Optional;

public interface TokenDAO {
   Optional<ResponseTokenDto> findById(long id);
   List<Token> showAvailableTokens();
}
