package com.dessert.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record ResearchRequest(
        @NotNull @Pattern(regexp = "[0-4]") String level,
        @NotEmpty @Size(max = 4) List<@NotNull @Pattern(regexp = "저당|통밀|비건|없음") String> habit,
        @NotEmpty @Size(max = 4) List<@NotNull @Pattern(regexp = "제과|제빵|음료|없음") String> find
) {}
