package de.ait.smallBusiness_be.users.dto;

import java.io.Serializable;

public class SessionUserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String role;

    public SessionUserDto() {
    }

    public SessionUserDto(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
