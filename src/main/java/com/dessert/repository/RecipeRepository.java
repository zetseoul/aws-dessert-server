package com.dessert.repository;

import com.dessert.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    @Query("select r from Recipe r join fetch r.author where r.recipeName like concat('%', :keyword, '%')")
    List<Recipe> searchByName(@Param("keyword") String keyword);
}
