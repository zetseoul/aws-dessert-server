package com.dessert.controller;

import com.dessert.dto.SignupRequest;
import com.dessert.dto.LoginRequest;
import com.dessert.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest http) {
        var session = http.getSession(false);
        if (session != null) session.invalidate();
        return Map.of("success", true, "message", "로그아웃 되었습니다!");
    }

    @GetMapping("/{id}")
    public com.dessert.dto.MyPageResponse myPage(@PathVariable("id") Integer id, HttpServletRequest http) {
        var session = http.getSession(false);
        var userId = session == null ? null : session.getAttribute("USER_PK_ID");
        if (!(userId instanceof Integer)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "로그인 후 이용해주세요.");
        }
        if (!id.equals(userId)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "본인의 정보만 조회할 수 있습니다.");
        }
        return userService.findMyPage(id);
    }

    @GetMapping("/session")
    public ResponseEntity<com.dessert.dto.SessionResponse> session(HttpServletRequest http) {
        var session = http.getSession(false);
        var userId = session == null ? null : session.getAttribute("USER_PK_ID");
        if (userId instanceof Integer id) {
            return ResponseEntity.ok(new com.dessert.dto.SessionResponse(true, id, null));
        }
        return ResponseEntity.status(401).body(new com.dessert.dto.SessionResponse(
                false, null, "로그인되지 않았습니다."));
    }

    @GetMapping("/username")
    public com.dessert.dto.AuthorResponse getAuthor(@RequestParam("author") Integer id) {
        return userService.findAuthor(id);
    }

    @PostMapping("/signup/register")
    public Map<String, Object> signup(@Valid @RequestBody SignupRequest request, HttpServletRequest http) {
        var userId = userService.signup(request);
        var session = http.getSession();
        http.changeSessionId();
        session.setAttribute("USER_PK_ID", userId);
        return Map.of("success", true, "message", "회원가입이 완료되었습니다😄🔅");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> signupFailure(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("success", false, "ok", false, "message", exception.getReason(),
                        "alert", exception.getReason()));
    }

    @PostMapping("/login/register")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        var result = userService.login(request);
        if (!result.existing()) {
            return Map.of("noExisting", true, "failedMessage", "가입 이력이 없는 아이디입니다. \n 회원가입 후 로그인해 주세요.");
        }
        if (result.userId() == null) {
            return Map.of("success", false, "message", "아이디 또는 비밀번호가 틀렸습니다!");
        }
        var session = http.getSession();
        http.changeSessionId();
        session.setAttribute("USER_PK_ID", result.userId());
        session.removeAttribute("ACCESS_TOKEN");
        return Map.of("success", true, "message", "로그인 되었습니다!😄🔅");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> invalidInput() {
        return ResponseEntity.badRequest().body(Map.of("success", false, "ok", false,
                "message", "입력 정보의 필수값과 길이를 확인해주세요.",
                "alert", "입력 정보의 필수값과 길이를 확인해주세요."));
    }
}
