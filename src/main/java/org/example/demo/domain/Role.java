package org.example.demo.domain;

public enum Role {
    ROLE_USER("USER"), ROLE_ADMIN("ADMIN");

    String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

}
