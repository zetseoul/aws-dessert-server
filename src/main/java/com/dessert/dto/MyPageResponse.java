package com.dessert.dto;

import java.util.List;

public record MyPageResponse(Info userInfo, Research researchInfo, boolean researchCompleted) {
    public record Info(Long users_kakao_id, String users_id, String users_name,
                       String nickname, String format_signup_date, String users_img) {}
    public record Research(String level, List<String> habit, List<String> find) {}
}
