package com.dessert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank @Size(max = 20) String login_id,
                           @NotBlank @Size(min = 5, max = 72) String login_pwd) {
    @Override
    public String toString() { return "LoginRequest[redacted]"; }
}
