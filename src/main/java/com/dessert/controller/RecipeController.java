package com.dessert.controller;

import com.dessert.dto.RecipeListResponse;
import com.dessert.dto.RecipeDetailResponse;
import org.springframework.web.bind.annotation.PathVariable;
import com.dessert.service.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public List<RecipeListResponse> getRecipes() {
        return recipeService.findRecipeList();
    }

    @GetMapping("/detail/{id}")
    public RecipeDetailResponse getRecipeDetail(@PathVariable("id") Integer id) {
        return recipeService.findDetail(id);
    }

    @GetMapping("/search")
    public List<RecipeListResponse> searchRecipes(@RequestParam("keyword") String keyword) {
        return recipeService.search(keyword);
    }
}
