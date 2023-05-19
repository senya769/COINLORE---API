package tokens.info.task.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tokens.info.task.dto.RequestNotifyDto;
import tokens.info.task.dto.ResponseTokenDto;
import tokens.info.task.exception.ErrorResponse;
import tokens.info.task.exception.TokenException;
import tokens.info.task.service.TokenService;

import java.util.*;

@RestController
@RequestMapping("/")
public class MainController {

    private final TokenService tokenService;
    private final Logger logger = LoggerFactory.getLogger("NOTiFY");

    @Autowired
    public MainController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping
    public List<Token> allAvailable() {
        return tokenService.showAvailableTokens();
    }

    @PostMapping
    boolean notify(@RequestBody RequestNotifyDto request) {
        Optional<ResponseTokenDto> byId = tokenService.findById(request.getIdToken());
        tokenService.registrUsernameNotify(request.getUsername(), request.getIdToken());
        if (byId.isPresent()) {
            logger.info(request.getUsername() + " --- " + byId.get().toString());
            return true;
        }
        return false;
    }

    @GetMapping("/{id}")
    public ResponseTokenDto findTokenById(@PathVariable long id) {
        Optional<ResponseTokenDto> byId = tokenService.findById(id);
        if (byId.isPresent()) {
            return byId.get();
        } else throw TokenException.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Not found this Token with id")
                .detail("idToken", String.valueOf(id))
                .build();

    }
}
