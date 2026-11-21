package com.dessert.controller;

import com.dessert.dto.ResearchRequest;
import com.dessert.service.ResearchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/users/research/res")
public class ResearchController {
    private final ResearchService service;
    public ResearchController(ResearchService service) { this.service = service; }

    @PostMapping("/ok")
    public Map<String, Object> submit(@Valid @RequestBody ResearchRequest request, HttpServletRequest http) {
        service.submit(userId(http), request);
        return Map.of("success", true, "message", "리서치 응답이 저장되었습니다!😄🔅");
    }

    @PostMapping("/no")
    public Map<String, Object> skip(HttpServletRequest http) {
        service.skip(userId(http));
        return Map.of("success", true, "message", "설문은 나중에 작성할 수 있습니다.");
    }

    private Integer userId(HttpServletRequest http) {
        var session = http.getSession(false);
        if (session != null && session.getAttribute("USER_PK_ID") instanceof Integer id) return id;
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 후 응답해주세요.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> failure(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(
                Map.of("success", false, "message", exception.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> invalidInput() {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "설문 항목을 올바르게 선택해주세요."));
    }
}
