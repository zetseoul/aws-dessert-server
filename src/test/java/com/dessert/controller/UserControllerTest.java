package com.dessert.controller;

import com.dessert.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserControllerTest {

    @Test
    void logoutInvalidatesSessionAndSessionQueryBecomes401() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(mock(UserService.class))).build();
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("USER_PK_ID", 42);
        mvc.perform(post("/api/users/logout").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/session"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousLogoutDoesNotCreateSession() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(mock(UserService.class))).build();
        var result = mvc.perform(post("/api/users/logout")).andExpect(status().isOk()).andReturn();
        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    void myPageRequiresLoginAndRejectsOtherUser() throws Exception {
        var service = mock(UserService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/42"))
                .andExpect(status().isUnauthorized());
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("USER_PK_ID", 1);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/42").session(session))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void ownProfileReturnsPublicProfileWithoutPassword() throws Exception {
        var service = mock(UserService.class);
        when(service.findMyPage(42)).thenReturn(new com.dessert.dto.MyPageResponse(
                new com.dessert.dto.MyPageResponse.Info(null, "baker", "테스터", "포근한스콘", "2026-11-21 12:00:00", null),
                new com.dessert.dto.MyPageResponse.Research("", java.util.List.of(), java.util.List.of()), false));
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("USER_PK_ID", 42);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/42").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.userInfo.nickname").value("포근한스콘"))
                .andExpect(jsonPath("$.userInfo.users_pwd").doesNotExist())
                .andExpect(jsonPath("$.researchCompleted").value(false));
    }

    @Test
    void anonymousSessionReturns401WithoutCreatingSession() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(mock(UserService.class))).build();
        var result = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/session"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isLogin").value(false))
                .andExpect(jsonPath("$.USER_PK_ID").isEmpty()).andReturn();
        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    void emptySessionIsNotLoggedIn() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(mock(UserService.class))).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/session")
                        .session(new org.springframework.mock.web.MockHttpSession()))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.isLogin").value(false));
    }

    @Test
    void sessionAfterLoginReturnsUserIdWithoutOAuthToken() throws Exception {
        var service = mock(UserService.class);
        when(service.login(any())).thenReturn(new UserService.LoginResult(true, 42));
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        var login = mvc.perform(post("/api/users/login/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login_id\":\"baker\",\"login_pwd\":\"test1234\"}"))
                .andExpect(status().isOk()).andReturn();
        var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false);
        session.setAttribute("ACCESS_TOKEN", "server-only-token");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/session").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.isLogin").value(true))
                .andExpect(jsonPath("$.USER_PK_ID").value(42))
                .andExpect(jsonPath("$.ACCESS_TOKEN").doesNotExist());
    }

    @Test
    void authorResponseOnlyExposesPublicFields() throws Exception {
        var service = mock(UserService.class);
        when(service.findAuthor(1)).thenReturn(new com.dessert.dto.AuthorResponse("포근한스콘", "안녕하세요"));
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/username").param("author", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("포근한스콘"))
                .andExpect(jsonPath("$.users_intro").value("안녕하세요"))
                .andExpect(jsonPath("$.users_pwd").doesNotExist())
                .andExpect(jsonPath("$.users_name").doesNotExist());
    }

    @Test
    void missingAuthorReturns404() throws Exception {
        var service = mock(UserService.class);
        when(service.findAuthor(9999)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "작성자를 찾을 수 없습니다."));
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users/username").param("author", "9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void loginRotatesSessionAndStoresUserId() throws Exception {
        var service = mock(UserService.class);
        when(service.login(any())).thenReturn(new UserService.LoginResult(true, 42));
        var session = new org.springframework.mock.web.MockHttpSession();
        var oldId = session.getId();
        session.setAttribute("ACCESS_TOKEN", "old-token");
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        mvc.perform(post("/api/users/login/register").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login_id\":\"baker\",\"login_pwd\":\"test1234\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        assertThat(session.getAttribute("USER_PK_ID")).isEqualTo(42);
        assertThat(session.getId()).isNotEqualTo(oldId);
        assertThat(session.getAttribute("ACCESS_TOKEN")).isNull();
    }

    @Test
    void wrongPasswordDoesNotCreateSession() throws Exception {
        var service = mock(UserService.class);
        when(service.login(any())).thenReturn(new UserService.LoginResult(true, null));
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        var result = mvc.perform(post("/api/users/login/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login_id\":\"baker\",\"login_pwd\":\"wrong123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(false)).andReturn();
        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    void unknownUserPreservesLegacyResponse() throws Exception {
        var service = mock(UserService.class);
        when(service.login(any())).thenReturn(new UserService.LoginResult(false, null));
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        mvc.perform(post("/api/users/login/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login_id\":\"missing\",\"login_pwd\":\"test1234\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.noExisting").value(true));
    }

    @Test
    void missingLoginPasswordReturns400() throws Exception {
        var service = mock(UserService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        mvc.perform(post("/api/users/login/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login_id\":\"baker\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    private static final String BODY = """
            {"form_id":"baker","form_pwd1":"test1234","form_pwd2":"test1234",
             "form_name":"테스터","form_nickname":"포근한스콘"}
            """;

    @Test
    void signupPreservesSuccessResponseAndSetsSession() throws Exception {
        var service = mock(UserService.class);
        when(service.signup(any())).thenReturn(42);
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        var result = mvc.perform(post("/api/users/signup/register")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.users_pwd").doesNotExist()).andReturn();
        assertThat(result.getRequest().getSession(false).getAttribute("USER_PK_ID")).isEqualTo(42);
    }

    @Test
    void blankNicknameReturns400WithoutSaving() throws Exception {
        var service = mock(UserService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        mvc.perform(post("/api/users/signup/register").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("포근한스콘", " ")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        verifyNoInteractions(service);
    }

    @Test
    void shortPasswordReturns400WithoutSaving() throws Exception {
        var service = mock(UserService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        mvc.perform(post("/api/users/signup/register").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("test1234", "123")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void duplicateReturns409WithoutCreatingSession() throws Exception {
        var service = mock(UserService.class);
        when(service.signup(any())).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "중복입니다."));
        var mvc = MockMvcBuilders.standaloneSetup(new UserController(service)).build();
        var result = mvc.perform(post("/api/users/signup/register").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.success").value(false)).andReturn();
        assertThat(result.getRequest().getSession(false)).isNull();
    }
}
