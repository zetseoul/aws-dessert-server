package com.dessert.service;

import com.dessert.dto.SignupRequest;
import com.dessert.dto.LoginRequest;
import com.dessert.entity.User;
import com.dessert.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
public class UserService {
    @Transactional(readOnly = true)
    public com.dessert.dto.AuthorResponse findAuthor(Integer id) {
        var user = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "작성자를 찾을 수 없습니다."));
        return new com.dessert.dto.AuthorResponse(user.getNickname(), user.getUsersIntro());
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.dessert.repository.PreferenceRepository preferenceRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       com.dessert.repository.PreferenceRepository preferenceRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.preferenceRepository = preferenceRepository;
    }

    @Transactional(readOnly = true)
    public com.dessert.dto.MyPageResponse findMyPage(Integer id) {
        var user = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        var preference = preferenceRepository.findByUserUsersPkIdAndIsResearchTrue(id);
        var info = new com.dessert.dto.MyPageResponse.Info(user.getUsersKakaoId(), user.getUsersId(),
                user.getUsersName(), user.getNickname(), user.getCreatedAt().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "/default_profile.png".equals(user.getProfileImage()) ? null : user.getProfileImage());
        var research = preference.map(p -> new com.dessert.dto.MyPageResponse.Research(
                p.getMyLevel(), splitAnswers(p.getMyHabit()), splitAnswers(p.getMyFind())))
                .orElse(new com.dessert.dto.MyPageResponse.Research("", java.util.List.of(), java.util.List.of()));
        return new com.dessert.dto.MyPageResponse(info, research, preference.isPresent());
    }

    private java.util.List<String> splitAnswers(String value) {
        return java.util.Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    @Transactional
    public Integer signup(SignupRequest request) {
        if (!request.form_pwd1().equals(request.form_pwd2())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }
        if (request.form_pwd1().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
        }
        if (userRepository.existsByUsersId(request.form_id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByNickname(request.form_nickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }
        var user = User.register(request.form_id(), passwordEncoder.encode(request.form_pwd1()),
                request.form_name(), request.form_nickname(), request.form_tel(),
                request.form_birthday(), request.form_email());
        try {
            return userRepository.saveAndFlush(user).getUsersPkId();
        } catch (DataIntegrityViolationException exception) {
            // 동시 가입에 의한 UNIQUE 충돌도 DB에서 최종 방어
            if ("23505".equals(findSqlState(exception))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "아이디 또는 닉네임이 이미 사용 중입니다.");
            }
            throw exception;
        }
    }

    private String findSqlState(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof java.sql.SQLException sql) return sql.getSQLState();
        }
        return null;
    }

    public record LoginResult(boolean existing, Integer userId) {}

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        var user = userRepository.findByUsersId(request.login_id());
        if (user.isEmpty()) return new LoginResult(false, null);
        if (request.login_pwd().getBytes(StandardCharsets.UTF_8).length > 72
                || !passwordEncoder.matches(request.login_pwd(), user.get().getUsersPwd())) {
            return new LoginResult(true, null);
        }
        return new LoginResult(true, user.get().getUsersPkId());
    }
}
