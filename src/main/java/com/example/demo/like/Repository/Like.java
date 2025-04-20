package com.example.demo.like.Repository;

import com.example.demo.user.Repository.User;
import jakarta.persistence.*;

@Entity
@Table(name = "likes")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id" , referencedColumnName = "id", nullable = false)
    private User user;

    private Long userTargetId;

    public Like() {}

    public Like(Long id, User user, Long userTargetId) {
        this.id = id;
        this.user = user;
        this.userTargetId = userTargetId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getUserTargetId() {
        return userTargetId;
    }

    public void setUserTarget(Long userTargetId) {
        this.userTargetId = userTargetId;
    }
}
