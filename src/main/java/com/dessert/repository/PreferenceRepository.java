package com.dessert.repository;

import com.dessert.entity.Preference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PreferenceRepository extends JpaRepository<Preference, Integer> {
    Optional<Preference> findByUserUsersPkId(Integer userId);
    Optional<Preference> findByUserUsersPkIdAndIsResearchTrue(Integer userId);
}
