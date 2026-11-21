package com.dessert.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "preference")
public class Preference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_pk_id")
    private Integer preferencePkId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "my_level", nullable = false, length = 1)
    private String myLevel;

    @Column(name = "my_habit", nullable = false, length = 30)
    private String myHabit;

    @Column(name = "my_find", nullable = false, length = 30)
    private String myFind;

    @Column(name = "is_research", nullable = false)
    private Boolean isResearch;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "users_pk_id", unique = true, nullable = false)
    private User user;


    public String getMyLevel() { return myLevel; }
    public String getMyHabit() { return myHabit; }
    public String getMyFind() { return myFind; }

    public static Preference forUser(User user) {
        var preference = new Preference();
        preference.user = user;
        return preference;
    }

    public void complete(String level, String habit, String find) {
        this.myLevel = level;
        this.myHabit = habit;
        this.myFind = find;
        this.isResearch = true;
    }

    protected Preference() {

    }
}
