package com.dessert.controller;

import com.dessert.dto.RecipeListResponse;
import com.dessert.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RecipeControllerTest {

    @Test
    void searchReturnsLegacyListFormat() throws Exception {
        var service = mock(RecipeService.class);
        when(service.search("쿠키")).thenReturn(List.of(new RecipeListResponse(
                2001, "초코쿠키", "cookie.jpg", 0, "test_baker")));
        var mvc = MockMvcBuilders.standaloneSetup(new RecipeController(service)).build();
        mvc.perform(get("/api/recipe/search").param("keyword", "쿠키"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipe_name").value("초코쿠키"))
                .andExpect(jsonPath("$[0].author_name").value("test_baker"));
        verify(service).search("쿠키");
    }

    @Test
    void searchWithoutMatchReturnsEmptyArray() throws Exception {
        var service = mock(RecipeService.class);
        when(service.search("없음")).thenReturn(List.of());
        var mvc = MockMvcBuilders.standaloneSetup(new RecipeController(service)).build();
        mvc.perform(get("/api/recipe/search").param("keyword", "없음"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void searchRequiresKeywordParameter() throws Exception {
        var service = mock(RecipeService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new RecipeController(service)).build();
        mvc.perform(get("/api/recipe/search")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void detailReturnsLegacyArrays() throws Exception {
        var service = mock(RecipeService.class);
        var info = new com.dessert.dto.RecipeDetailResponse.Info(2001, "초코쿠키", null,
                "cookie.jpg", 2, (short) 1, null, 0, "밀가루", null, 1001, null);
        var method = new com.dessert.dto.RecipeDetailResponse.Method(1, 2001, "재료 준비", 1);
        when(service.findDetail(2001)).thenReturn(new com.dessert.dto.RecipeDetailResponse(
                List.of(info), List.of(method)));
        var mvc = MockMvcBuilders.standaloneSetup(new RecipeController(service)).build();
        mvc.perform(get("/api/recipe/detail/2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeResult[0].recipe_name").value("초코쿠키"))
                .andExpect(jsonPath("$.recipeMethodResult[0].method").value("재료 준비"))
                .andExpect(jsonPath("$.recipeMethodResult[0].method_number").value(1));
    }

    @Test
    void missingDetailReturns404() throws Exception {
        var service = mock(RecipeService.class);
        when(service.findDetail(9999)).thenThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND));
        var mvc = MockMvcBuilders.standaloneSetup(new RecipeController(service)).build();
        mvc.perform(get("/api/recipe/detail/9999")).andExpect(status().isNotFound());
    }

    @Test
    void getRecipesReturnsLegacyResponseFormat() throws Exception {
        var service = mock(RecipeService.class);
        when(service.findRecipeList()).thenReturn(List.of(new RecipeListResponse(
                2001, "초코쿠키", "https://example.com/cookie.jpg", 0, "test_baker")));

        var mvc = MockMvcBuilders.standaloneSetup(new RecipeController(service)).build();

        mvc.perform(get("/api/recipe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].recipe_pk_id").value(2001))
                .andExpect(jsonPath("$[0].recipe_name").value("초코쿠키"))
                .andExpect(jsonPath("$[0].recipe_image").value("https://example.com/cookie.jpg"))
                .andExpect(jsonPath("$[0].scrap_count").value(0))
                .andExpect(jsonPath("$[0].author_name").value("test_baker"))
                .andExpect(jsonPath("$[0].users_pwd").doesNotExist());
    }
}
