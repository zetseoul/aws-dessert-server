package com.dessert.repository;

import com.dessert.entity.Recipe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private DataSource dataSource;

    @Test
    @Sql("/sql/recipe-test-data.sql")
    void findAllReturnsPreparedRecipe() {
        var recipes = recipeRepository.findAll();

        recipes.forEach(recipe -> System.out.println("조회 결과: " + recipe.getRecipeName()));

        assertThat(recipes).hasSize(1);
        assertThat(recipes).extracting(Recipe::getRecipeName).containsExactly("초코쿠키");
    }

    @Test
    @Sql("/sql/recipe-test-data.sql")
    void findByIdReturnsPreparedRecipe() {
        var recipe = recipeRepository.findById(2001);

        assertThat(recipe).isPresent();
        assertThat(recipe.orElseThrow().getRecipeName()).isEqualTo("초코쿠키");
    }

    @Test
    @Sql("/sql/recipe-test-data.sql")
    void findByIdReturnsEmptyForMissingRecipe() {
        assertThat(recipeRepository.findById(9999)).isEmpty();
    }

    @Test
    void repositoryConnectsToH2() throws SQLException {
        assertThat(recipeRepository).isNotNull();

        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).startsWith("jdbc:h2:mem:");
        }
    }
}
