INSERT INTO users (users_pk_id, users_name, nickname, baking_interest, baking_level, created_at)
VALUES (1001, '테스터', 'test_baker', '쿠키', 1, CURRENT_TIMESTAMP);

INSERT INTO recipe (recipe_pk_id, recipe_name, recipe_image, recipe_servings, baking_level,
                    scrap_count, category_big, category_middle, category_machine,
                    ingredient1, author_id, created_at, updated_at)
VALUES (2001, '초코쿠키', 'https://example.com/cookie.jpg', 2, 1,
        0, '쿠키', '초코쿠키', '오븐',
        '밀가루 100g, 초콜릿 50g', 1001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
