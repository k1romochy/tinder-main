package com.example.demo.stack.Repositrory;

import com.example.demo.user.Repository.User;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table
public class Stack implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private List<Long> usersMatchingID;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    public Stack() {}

    public Stack(Long id, List<Long> usersMatchingID) {
        this.id = id;
        this.usersMatchingID = usersMatchingID;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public List<Long> getUsersMatchingID() {
        return usersMatchingID;
    }

    public void setUsers(List<Long> users) {
        this.usersMatchingID = users;
    }
}
