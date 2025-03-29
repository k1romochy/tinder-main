package com.example.demo.preferences.Repository;

import com.example.demo.user.Repository.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table
public class Preferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sex;

    private short age_min;
    private short age_max;

    @OneToOne(mappedBy = "preferences", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    public Preferences(Long id, String sex, short age_min, short age_max) {
        this.id = id;
        this.sex = sex;
        this.age_max = age_max;
        this.age_min = age_min;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public short getAgeMin() {
        return age_min;
    }

    public void setAgeMin(short age_min) {
        this.age_min = age_min;
    }

    public short getAgeMax() {
        return age_max;
    }

    public void setAgeMax(short age_max) {
        this.age_max = age_max;
    }

    public User getUser() {
        return user;
    }
}
