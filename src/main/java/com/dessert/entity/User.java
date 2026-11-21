package com.dessert.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_pk_id")
    private Integer usersPkId;

    @Column(name = "users_name", nullable = false, length = 10)
    private String usersName;

    @Column(name = "nickname", unique = true, nullable = false, length = 20)
    private String nickname;

    @Column(name = "users_kakao_id", unique = true)
    private Long usersKakaoId;

    @Column(name = "users_id", unique = true, length = 20)
    private String usersId;

    @Column(name = "users_pwd", length = 100)
    private String usersPwd;

    @Column(name = "users_intro", length = 100)
    private String usersIntro;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "tel", length = 12)
    private String tel;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "birthday")
    private LocalDate birthday;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "baking_experience")
    private Boolean bakingExperience;

    @Column(name = "baking_interest", nullable = false, length = 10)
    private String bakingInterest;

    @Column(name = "baking_level", nullable = false)
    private Short bakingLevel;

    @Column(name = "eating_habit1")
    private Boolean eatingHabit1;

    @Column(name = "eating_habit2", columnDefinition = "TEXT")
    private String eatingHabit2;

    public String getNickname() {
        return nickname;
    }

    public String getUsersIntro() { return usersIntro; }
    public Long getUsersKakaoId() { return usersKakaoId; }
    public String getUsersId() { return usersId; }
    public String getUsersName() { return usersName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getProfileImage() { return profileImage; }

    public Integer getUsersPkId() { return usersPkId; }

    public static User register(String id, String passwordHash, String name, String nickname,
                                String tel, LocalDate birthday, String email) {
        var user = new User();
        user.usersId = id;
        user.usersPwd = passwordHash;
        user.usersName = name;
        user.nickname = nickname;
        user.tel = tel;
        user.birthday = birthday;
        user.email = email;
        user.bakingInterest = "모두";
        user.bakingLevel = 0;
        user.eatingHabit1 = false;
        user.profileImage = "/default_profile.png";
        return user;
    }

    public String getUsersPwd() { return usersPwd; }
    public String getBakingInterest() { return bakingInterest; }

    public void updateBakingPreference(short level, String interest) {
        bakingLevel = level;
        bakingInterest = interest;
    }

    protected User() {

    }
}
