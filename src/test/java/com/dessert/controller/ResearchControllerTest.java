package com.dessert.controller;

import com.dessert.service.ResearchService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ResearchControllerTest {
    private static final String BODY = """
            {"level":"0","habit":["저당"],"find":["제과"]}
            """;

    @Test
    void submissionUsesSessionUser() throws Exception {
        var service = mock(ResearchService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new ResearchController(service)).build();
        var session = new MockHttpSession();
        session.setAttribute("USER_PK_ID", 42);
        mvc.perform(post("/api/users/research/res/ok").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        verify(service).submit(eq(42), any());
    }

    @Test
    void anonymousSubmitAndSkipAreRejected() throws Exception {
        var service = mock(ResearchService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new ResearchController(service)).build();
        mvc.perform(post("/api/users/research/res/ok").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/users/research/res/no")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void invalidValuesAreRejectedBeforeSaving() throws Exception {
        var service = mock(ResearchService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new ResearchController(service)).build();
        var session = new MockHttpSession();
        session.setAttribute("USER_PK_ID", 42);
        for (String body : new String[]{BODY.replace("\"0\"", "\"9\""),
                BODY.replace("[\"저당\"]", "[]"), BODY.replace("제과", "invalid")}) {
            mvc.perform(post("/api/users/research/res/ok").session(session)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test
    void skipRequiresNoAnswerPayload() throws Exception {
        var service = mock(ResearchService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new ResearchController(service)).build();
        var session = new MockHttpSession();
        session.setAttribute("USER_PK_ID", 42);
        mvc.perform(post("/api/users/research/res/no").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        verify(service).skip(42);
    }
}
