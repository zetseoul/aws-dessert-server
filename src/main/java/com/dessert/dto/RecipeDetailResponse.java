package com.dessert.dto;

import java.util.List;

public record RecipeDetailResponse(List<Info> recipeResult, List<Method> recipeMethodResult) {
    public record Info(Integer recipe_pk_id, String recipe_name, String recipe_intro,
                       String recipe_image, Integer recipe_servings, Short baking_level,
                       String tags, Integer scrap_count, String ingredient1,
                       String ingredient2, Integer author_id, String tips) {}

    public record Method(Integer method_pk_id, Integer recipe_pk_id,
                         String method, Integer method_number) {}
}
