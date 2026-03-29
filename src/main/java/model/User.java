package model;

import java.sql.Timestamp;

/**
 * User - represents a registered user (buyer / seller / admin).
 */
public class User {
    private int id;
    private String name;
    private String email;
    private String password;   // stored as SHA-256 hash
    private String role;       // "buyer" | "seller" | "admin"
    private Timestamp createdAt;

    public User() {}

    public User(int id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId()                   { return id; }
    public void setId(int id)            { this.id = id; }

    public String getName()              { return name; }
    public void setName(String name)     { this.name = name; }

    public String getEmail()             { return email; }
    public void setEmail(String email)   { this.email = email; }

    public String getPassword()          { return password; }
    public void setPassword(String p)    { this.password = p; }

    public String getRole()              { return role; }
    public void setRole(String role)     { this.role = role; }

    public Timestamp getCreatedAt()                  { return createdAt; }
    public void setCreatedAt(Timestamp createdAt)    { this.createdAt = createdAt; }

    public boolean isSeller() { return "seller".equals(role) || "admin".equals(role); }
    public boolean isBuyer()  { return "buyer".equals(role); }
    public boolean isAdmin()  { return "admin".equals(role); }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', role='" + role + "'}";
    }
}
