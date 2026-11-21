package com.dessert.service;

import com.dessert.dto.ResearchRequest;
import com.dessert.entity.Preference;
import com.dessert.repository.PreferenceRepository;
import com.dessert.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResearchService {
    private final UserRepository users;
    private final PreferenceRepository preferences;

    public ResearchService(UserRepository users, PreferenceRepository preferences) {
        this.users = users;
        this.preferences = preferences;
    }

    @Transactional
    public void submit(Integer userId, ResearchRequest request) {
        if ((request.habit().contains("없음") && request.habit().size() > 1)
                || (request.find().contains("없음") && request.find().size() > 1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "없음은 다른 항목과 함께 선택할 수 없습니다.");
        }
        // 사용자 행을 잠가 동시 제출 시에도 사용자당 선호도 한 행 유지
        var user = users.findForPreferenceUpdate(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        var preference = preferences.findByUserUsersPkId(userId).orElseGet(() -> Preference.forUser(user));
        var habit = request.habit().stream().distinct().toList();
        var find = request.find().stream().distinct().toList();
        preference.complete(request.level(), String.join(",", habit), String.join(",", find));
        preferences.save(preference);
        String interest = find.size() == 1 && !find.contains("없음") ? find.getFirst() : "모두";
        user.updateBakingPreference(Short.parseShort(request.level()), interest);
    }

    @Transactional(readOnly = true)
    public void skip(Integer userId) {
        if (!users.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        // 미완료 사용자는 행을 만들지 않으며, 이전에 제출한 답변도 지우지 않음
    }
}
