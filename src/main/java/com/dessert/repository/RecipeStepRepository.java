package com.dessert.repository;

import com.dessert.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Integer> {
    List<RecipeStep> findByRecipeRecipePkIdOrderByStepNumberAsc(Integer recipeId);
}
