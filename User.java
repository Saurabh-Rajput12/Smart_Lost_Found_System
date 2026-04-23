package com.lostfound.models;

import java.sql.Timestamp;

/**
 * User.java
 * ------------------------------------------------
 * Represents a row in the 'users' table.
 * Used for both regular users and admins.
 * role  : "ADMIN" or "USER"
 * status: "ACTIVE" or "INACTIVE"
 * ------------------------------------------------
 */
public class User {

    // ── Fields (match database columns exactly) ──────────────
    private int       userId;
    private String    name;
    private String    email;
    private String    password;       // stored as plain/hashed text
    private String    phone;
    private String    role;           // "ADMIN" or "USER"
    private String    status;         // "ACTIVE" or "INACTIVE"
    private Timestamp createdAt;

    // ── Default constructor (required for DAO layer) ──────────
    public User() {}

    // ── Parameterized constructor (for quick object creation) ─
    public User(int userId, String name, String email,
                String password, String phone,
                String role, String status, Timestamp createdAt) {
        this.userId    = userId;
        this.name      = name;
        this.email     = email;
        this.password  = password;
        this.phone     = phone;
        this.role      = role;
        this.status    = status;
        this.createdAt = createdAt;
    }

    // ── Getters ───────────────────────────────────────────────
    public int       getUserId()   { return userId;    }
    public String    getName()     { return name;      }
    public String    getEmail()    { return email;     }
    public String    getPassword() { return password;  }
    public String    getPhone()    { return phone;     }
    public String    getRole()     { return role;      }
    public String    getStatus()   { return status;    }
    public Timestamp getCreatedAt(){ return createdAt; }

    // ── Setters ───────────────────────────────────────────────
    public void setUserId(int userId)          { this.userId    = userId;    }
    public void setName(String name)           { this.name      = name;      }
    public void setEmail(String email)         { this.email     = email;     }
    public void setPassword(String password)   { this.password  = password;  }
    public void setPhone(String phone)         { this.phone     = phone;     }
    public void setRole(String role)           { this.role      = role;      }
    public void setStatus(String status)       { this.status    = status;    }
    public void setCreatedAt(Timestamp createdAt){ this.createdAt = createdAt;}

    // ── Helper methods ────────────────────────────────────────

    /** Returns true if this user is an admin */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    /** Returns true if this account is active */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    // ── toString (useful for debugging) ──────────────────────
    @Override
    public String toString() {
        return "User{" +
               "userId="  + userId  +
               ", name='" + name    + '\'' +
               ", email='"+ email   + '\'' +
               ", role='" + role    + '\'' +
               ", status='"+ status + '\'' +
               '}';
    }
}