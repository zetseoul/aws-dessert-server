package com.dessert.service;

import com.dessert.dto.ResearchRequest;
import com.dessert.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(ResearchService.class)
@Sql("/sql/recipe-test-data.sql")
class ResearchServiceTest {
    @Autowired ResearchService service;
    @Autowired PreferenceRepository preferences;
    @Autowired UserRepository users;

    @Test
    void submitStoresOneRowAndUpdatesUserInterest() {
        service.submit(1001, new ResearchRequest("0", List.of("저당", "비건"), List.of("제과")));
        preferences.flush();
        var result = preferences.findByUserUsersPkIdAndIsResearchTrue(1001).orElseThrow();
        assertThat(result.getMyLevel()).isEqualTo("0");
        assertThat(result.getMyHabit()).isEqualTo("저당,비건");
        assertThat(users.findById(1001).orElseThrow().getBakingInterest()).isEqualTo("제과");
    }

    @Test
    void resubmissionUpdatesExistingRowAndSkipPreservesIt() {
        service.submit(1001, new ResearchRequest("1", List.of("저당"), List.of("제과")));
        service.submit(1001, new ResearchRequest("2", List.of("통밀"), List.of("제빵", "음료")));
        service.skip(1001);
        assertThat(preferences.count()).isEqualTo(1);
        assertThat(preferences.findByUserUsersPkIdAndIsResearchTrue(1001).orElseThrow().getMyLevel()).isEqualTo("2");
        assertThat(users.findById(1001).orElseThrow().getBakingInterest()).isEqualTo("모두");
    }

    @Test
    void skipDoesNotCreateIncompleteRow() {
        service.skip(1001);
        assertThat(preferences.count()).isZero();
    }

    @Test
    void conflictingNoneAnswerDoesNotSave() {
        assertThatThrownBy(() -> service.submit(1001,
                new ResearchRequest("1", List.of("없음", "저당"), List.of("제과"))))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThat(preferences.count()).isZero();
    }
}
