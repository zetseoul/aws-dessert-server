package com.dessert.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record SignupRequest(
        @NotBlank @Size(max = 20) String form_id,
        @NotBlank @Size(min = 5, max = 72) String form_pwd1,
        @NotBlank @Size(min = 5, max = 72) String form_pwd2,
        @NotBlank @Size(max = 10) String form_name,
        @NotBlank @Size(min = 2, max = 20) String form_nickname,
        @Size(max = 12) String form_tel,
        LocalDate form_birthday,
        @Email @Size(max = 100) String form_email
) {
    // 비밀번호가 자동 생성되는 record.toString()에 노출되지 않도록 처리
    @Override
    public String toString() { return "SignupRequest[redacted]"; }
}
