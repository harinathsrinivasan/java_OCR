package com.kapia.users;

import jakarta.persistence.*;

@Entity
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_user_seq")
    @SequenceGenerator(name = "application_user_seq", sequenceName = "application_user_seq", allocationSize = 1)
    private Integer id;

    private String username;
    private String password;
    private String authority;

    public ApplicationUser() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

}
