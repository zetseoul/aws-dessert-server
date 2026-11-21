package com.dessert.service;

import com.dessert.config.PasswordConfig;
import com.dessert.dto.SignupRequest;
import com.dessert.dto.LoginRequest;
import com.dessert.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({UserService.class, PasswordConfig.class})
class UserServiceTest {

    @Test
    void initialInterestDoesNotMeanSurveyComplete() {
        var id = service.signup(request("baker", "포근한스콘", "test1234"));
        var result = service.findMyPage(id);
        assertThat(result.userInfo().nickname()).isEqualTo("포근한스콘");
        assertThat(result.researchCompleted()).isFalse();
        assertThat(result.researchInfo().habit()).isEmpty();
    }

    @Test
    @org.springframework.test.context.jdbc.Sql({"/sql/recipe-test-data.sql", "/sql/preference-test-data.sql"})
    void completedSurveyReturnsAnswers() {
        var result = service.findMyPage(1001);
        assertThat(result.researchCompleted()).isTrue();
        assertThat(result.researchInfo().level()).isEqualTo("1");
        assertThat(result.researchInfo().habit()).containsExactly("채식", "저당");
    }

    @Test
    @org.springframework.test.context.jdbc.Sql("/sql/recipe-test-data.sql")
    void authorUsesStoredNicknameAndIntroduction() {
        var author = service.findAuthor(1001);
        assertThat(author.nickname()).isEqualTo("test_baker");
        assertThat(author.users_intro()).isNull();
    }

    @Test
    void missingAuthorIs404() {
        assertThatThrownBy(() -> service.findAuthor(9999))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(404));
    }
    @Autowired UserService service;
    @Autowired UserRepository repository;
    @Autowired PasswordEncoder encoder;

    private SignupRequest request(String id, String nickname, String confirmation) {
        return new SignupRequest(id, "test1234", confirmation, "테스터", nickname, null, null, null);
    }

    @Test
    void loginChecksStoredBcryptPassword() {
        var id = service.signup(request("baker", "포근한스콘", "test1234"));
        assertThat(service.login(new LoginRequest("baker", "test1234")).userId()).isEqualTo(id);
        var wrong = service.login(new LoginRequest("baker", "wrong123"));
        assertThat(wrong.existing()).isTrue();
        assertThat(wrong.userId()).isNull();
    }

    @Test
    void unknownLoginDoesNotCreateUser() {
        assertThat(service.login(new LoginRequest("missing", "test1234")).existing()).isFalse();
        assertThat(repository.count()).isZero();
    }

    @Test
    void signupStoresHashedPasswordAndInitialInterest() {
        var id = service.signup(request("baker", "포근한스콘", "test1234"));
        var user = repository.findById(id).orElseThrow();
        assertThat(user.getNickname()).isEqualTo("포근한스콘");
        assertThat(user.getBakingInterest()).isEqualTo("모두");
        assertThat(user.getUsersPwd()).isNotEqualTo("test1234");
        assertThat(encoder.matches("test1234", user.getUsersPwd())).isTrue();
    }

    @Test
    void duplicateIdReturns409() {
        service.signup(request("baker", "포근한스콘", "test1234"));
        assertThatThrownBy(() -> service.signup(request("baker", "달콤한머랭", "test1234")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(409));
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void duplicateNicknameReturns409() {
        service.signup(request("baker", "포근한스콘", "test1234"));
        assertThatThrownBy(() -> service.signup(request("other", "포근한스콘", "test1234")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(409));
    }

    @Test
    void mismatchedPasswordDoesNotSaveUser() {
        assertThatThrownBy(() -> service.signup(request("baker", "포근한스콘", "different")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode().value()).isEqualTo(400));
        assertThat(repository.count()).isZero();
    }
}
