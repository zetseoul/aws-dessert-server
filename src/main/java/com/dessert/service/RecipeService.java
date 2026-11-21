package com.dessert.service;

import com.dessert.entity.Recipe;
import com.dessert.dto.RecipeListResponse;
import com.dessert.repository.RecipeRepository;
import com.dessert.repository.RecipeStepRepository;
import com.dessert.dto.RecipeDetailResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecipeService {

    private static final Logger log = LoggerFactory.getLogger(RecipeService.class);

    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;

    public RecipeService(RecipeRepository recipeRepository, RecipeStepRepository recipeStepRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeStepRepository = recipeStepRepository;
    }

    public List<Recipe> findAll() {
        return recipeRepository.findAll();
    }

    public RecipeDetailResponse findDetail(Integer id) {
        var recipe = recipeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("DB에 없는 레시피 조회 요청: recipeId={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "레시피를 찾을 수 없습니다.");
                });
        var info = new RecipeDetailResponse.Info(id, recipe.getRecipeName(), recipe.getRecipeIntro(),
                recipe.getRecipeImage(), recipe.getRecipeServings(), recipe.getBakingLevel(),
                recipe.getTags(), recipe.getScrapCount(), recipe.getIngredient1(),
                recipe.getIngredient2(), recipe.getAuthor().getUsersPkId(), recipe.getTips());
        var steps = recipeStepRepository.findByRecipeRecipePkIdOrderByStepNumberAsc(id).stream()
                .map(step -> new RecipeDetailResponse.Method(step.getStepPkId(), id,
                        step.getStep(), step.getStepNumber())).toList();
        return new RecipeDetailResponse(List.of(info), steps);
    }

    public List<RecipeListResponse> findRecipeList() {
        return recipeRepository.findAll().stream()
                .map(recipe -> new RecipeListResponse(
                        recipe.getRecipePkId(), recipe.getRecipeName(),
                        recipe.getRecipeImage(), recipe.getScrapCount(),
                        recipe.getAuthor().getNickname()))
                .toList();
    }

    public List<RecipeListResponse> search(String keyword) {
        return recipeRepository.searchByName(keyword).stream()
                .map(recipe -> new RecipeListResponse(
                        recipe.getRecipePkId(), recipe.getRecipeName(),
                        recipe.getRecipeImage(), recipe.getScrapCount(),
                        recipe.getAuthor().getNickname()))
                .toList();
    }
}
