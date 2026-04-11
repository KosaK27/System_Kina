package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private int id;
    private String name;
    private String email;
    private String role;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getRole() { return role; }
    public void setRole(String r) { this.role = r; }

    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isEmployee() { return "EMPLOYEE".equals(role); }
    public boolean isClient() { return "CLIENT".equals(role); }
    public boolean isGuest() { return "GUEST".equals(role); }
    public boolean canManage() { return isAdmin() || isEmployee(); }

    @Override public String toString() { return name + " (" + role + ")"; }
}