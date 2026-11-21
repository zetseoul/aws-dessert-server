package com.dessert.service;

import com.dessert.entity.Recipe;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.web.server.ResponseStatusException;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(RecipeService.class)
class RecipeServiceTest {

    @Autowired
    private RecipeService recipeService;

    @Test
    @Sql("/sql/recipe-test-data.sql")
    void searchMatchesPartOfNameAndIncludesAuthor() {
        var result = recipeService.search("코쿠");
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().recipe_name()).isEqualTo("초코쿠키");
        assertThat(result.getFirst().author_name()).isEqualTo("test_baker");
    }

    @Test
    @Sql("/sql/recipe-test-data.sql")
    void searchWithoutMatchReturnsEmptyList() {
        assertThat(recipeService.search("없는레시피")).isEmpty();
    }

    @Test
    @Sql("/sql/recipe-test-data.sql")
    void emptyKeywordReturnsAllRecipes() {
        assertThat(recipeService.search("")).hasSize(1);
    }

    @Test
    @Sql({"/sql/recipe-test-data.sql", "/sql/recipe-step-test-data.sql"})
    void detailReturnsRecipeAndOrderedSteps() {
        var result = recipeService.findDetail(2001);
        assertThat(result.recipeResult()).hasSize(1);
        assertThat(result.recipeResult().getFirst().recipe_name()).isEqualTo("초코쿠키");
        assertThat(result.recipeResult().getFirst().author_id()).isEqualTo(1001);
        assertThat(result.recipeMethodResult()).extracting("method_number").containsExactly(1, 2);
        assertThat(result.recipeMethodResult()).extracting("method").containsExactly("재료 준비", "굽기");
    }

    @Test
    @Sql("/sql/recipe-test-data.sql")
    void detailWithoutStepsReturnsEmptyList() {
        assertThat(recipeService.findDetail(2001).recipeMethodResult()).isEmpty();
    }

    @Test
    void missingDetailReturns404() {
        assertThatThrownBy(() -> recipeService.findDetail(9999))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode().value()).isEqualTo(404));
    }

    @Test
    @Sql("/sql/recipe-test-data.sql")
    void findRecipeListIncludesAuthorNickname() {
        var recipes = recipeService.findRecipeList();
        assertThat(recipes).hasSize(1);
        assertThat(recipes.getFirst().recipe_name()).isEqualTo("초코쿠키");
        assertThat(recipes.getFirst().author_name()).isEqualTo("test_baker");
    }

    @Test
    @Sql("/sql/recipe-test-data.sql")
    void findAllReturnsPreparedRecipe() {
        assertThat(recipeService.findAll())
                .extracting(Recipe::getRecipeName)
                .containsExactly("초코쿠키");
    }
}
