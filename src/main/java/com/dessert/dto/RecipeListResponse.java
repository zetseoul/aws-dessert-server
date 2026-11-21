package com.dessert.dto;

public record RecipeListResponse(
        Integer recipe_pk_id,
        String recipe_name,
        String recipe_image,
        Integer scrap_count,
        String author_name
) {
}
