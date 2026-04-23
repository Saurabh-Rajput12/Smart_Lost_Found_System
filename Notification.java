package com.lostfound.models;

import java.sql.Timestamp;

/**
 * Notification.java
 * ------------------------------------------------
 * Represents a row in the 'notifications' table.
 * Each user has their own notification inbox.
 *
 * isRead = false → unread (shows as badge on login)
 * isRead = true  → already seen by user
 * ------------------------------------------------
 */
public class Notification {

    private int       notifId;
    private int       userId;
    private String    message;
    private boolean   isRead;      // false = unread, true = read
    private Timestamp createdAt;

    // ── Constructors ──────────────────────────────────────────
    public Notification() {}

    // Constructor for creating a new notification
    public Notification(int userId, String message) {
        this.userId  = userId;
        this.message = message;
        this.isRead  = false;      // always unread when first created
    }

    // ── Getters ───────────────────────────────────────────────
    public int       getNotifId()   { return notifId;   }
    public int       getUserId()    { return userId;    }
    public String    getMessage()   { return message;   }
    public boolean   isRead()       { return isRead;    }
    public Timestamp getCreatedAt() { return createdAt; }

    // ── Setters ───────────────────────────────────────────────
    public void setNotifId(int notifId)        { this.notifId   = notifId;   }
    public void setUserId(int userId)          { this.userId    = userId;    }
    public void setMessage(String message)     { this.message   = message;   }
    public void setRead(boolean isRead)        { this.isRead    = isRead;    }
    public void setCreatedAt(Timestamp createdAt){ this.createdAt = createdAt;}

    @Override
    public String toString() {
        return "Notification{" +
               "notifId=" + notifId   +
               ", userId=" + userId   +
               ", isRead=" + isRead   +
               ", message='"+ message + '\'' +
               '}';
    }
}