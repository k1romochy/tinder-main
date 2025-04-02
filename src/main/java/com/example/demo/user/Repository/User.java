package com.example.demo.user.Repository;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.example.demo.preferences.Repository.Preferences;
import com.example.demo.stack.Repositrory.Stack;
import com.example.demo.stack.StackMatchingDataUsers.StackMatchingData;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "users")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id")
public class User implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @Column
    private String name;

    @Column
    private String password;

    @Column(columnDefinition = "geometry(Point,4326)", nullable = true)
    private Point point;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "preferences_id", referencedColumnName = "id", nullable = true)
    @JsonIgnore
    private Preferences preferences;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "stack_id", referencedColumnName = "id", nullable = true)
    @JsonIgnore
    private Stack stack;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "stackMatchingData_id", referencedColumnName = "id", nullable = true)
    @JsonIgnore
    private StackMatchingData stackMatchingDatak;

    public User() {
    }

    public User(Long id, String email, String name, String password) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.roles = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    public void removeRole(String role) {
        this.roles.remove(role);
    }

    public boolean hasRole(String role) {
        return this.roles.contains(role);
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
        preferences.setUser(this);
    }

    public Preferences getPreferences() {
        return this.preferences;
    }

    public void setStack (Stack stack) {
        this.stack = stack;
        stack.setUser(this);
    }

    public Stack getStack() {
        return stack;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public Point getPoint() {
        return this.point;
    }
}
