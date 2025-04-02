package com.example.demo.preferences.Repository;

import com.example.demo.user.Repository.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import org.locationtech.jts.geom.Point;

@Entity
@Table
public class Preferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Gender preferredGender;

    private int age;

    @OneToOne(mappedBy = "preferences", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    public Preferences(Long id, String sex, short age, Gender gender, Gender preferredGender) {
        this.id = id;
        this.age = age;
        this.gender = gender;
        this.preferredGender = preferredGender;
    }

    public Preferences() {}

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAge(short age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setPreferredGender(Gender preferredGender) {
        this.preferredGender = preferredGender;
    }

    public Gender getGender() {
        return gender;
    }

    public Gender getPreferredGender() {
        return preferredGender;
    }

    public User getUser() {
        return user;
    }
}
