package com.lostfound.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Item.java
 * ------------------------------------------------
 * Represents a row in the 'items' table.
 * This is the CORE model of the entire system.
 *
 * type   : "LOST" or "FOUND"
 * status : "LOST" | "FOUND" | "CLAIMED" | "EXPIRED"
 * refCode: format LF-YYYY-XXXXX (generated in ItemDAO)
 * ------------------------------------------------
 */
public class Item {

    // ── Fields ────────────────────────────────────────────────
    private int           itemId;
    private String        refCode;         // LF-2026-00001
    private String        title;
    private String        description;
    private int           categoryId;
    private String        categoryName;    // joined from categories table
    private String        type;            // "LOST" or "FOUND"
    private String        status;          // "LOST","FOUND","CLAIMED","EXPIRED"
    private String        location;
    private LocalDateTime dateOccurred;    // when item was lost/found
    private String        imagePath;       // relative path to uploaded image
    private int           reportedBy;      // user_id of reporter
    private String        reporterName;    // joined from users table
    private Timestamp     createdAt;

    // ── Constructors ──────────────────────────────────────────
    public Item() {}

    // Constructor for creating a new item report (no ID yet)
    public Item(String refCode, String title, String description,
                int categoryId, String type, String status,
                String location, LocalDateTime dateOccurred,
                String imagePath, int reportedBy) {
        this.refCode      = refCode;
        this.title        = title;
        this.description  = description;
        this.categoryId   = categoryId;
        this.type         = type;
        this.status       = status;
        this.location     = location;
        this.dateOccurred = dateOccurred;
        this.imagePath    = imagePath;
        this.reportedBy   = reportedBy;
    }

    // ── Getters ───────────────────────────────────────────────
    public int           getItemId()       { return itemId;       }
    public String        getRefCode()      { return refCode;      }
    public String        getTitle()        { return title;        }
    public String        getDescription()  { return description;  }
    public int           getCategoryId()   { return categoryId;   }
    public String        getCategoryName() { return categoryName; }
    public String        getType()         { return type;         }
    public String        getStatus()       { return status;       }
    public String        getLocation()     { return location;     }
    public LocalDateTime getDateOccurred() { return dateOccurred; }
    public String        getImagePath()    { return imagePath;    }
    public int           getReportedBy()   { return reportedBy;   }
    public String        getReporterName() { return reporterName; }
    public Timestamp     getCreatedAt()    { return createdAt;    }

    // ── Setters ───────────────────────────────────────────────
    public void setItemId(int itemId)                   { this.itemId       = itemId;       }
    public void setRefCode(String refCode)              { this.refCode      = refCode;      }
    public void setTitle(String title)                  { this.title        = title;        }
    public void setDescription(String description)      { this.description  = description;  }
    public void setCategoryId(int categoryId)           { this.categoryId   = categoryId;   }
    public void setCategoryName(String categoryName)    { this.categoryName = categoryName; }
    public void setType(String type)                    { this.type         = type;         }
    public void setStatus(String status)                { this.status       = status;       }
    public void setLocation(String location)            { this.location     = location;     }
    public void setDateOccurred(LocalDateTime dateOccurred){ this.dateOccurred = dateOccurred;}
    public void setImagePath(String imagePath)          { this.imagePath    = imagePath;    }
    public void setReportedBy(int reportedBy)           { this.reportedBy   = reportedBy;   }
    public void setReporterName(String reporterName)    { this.reporterName = reporterName; }
    public void setCreatedAt(Timestamp createdAt)       { this.createdAt    = createdAt;    }

    // ── Helper methods ────────────────────────────────────────

    /** Returns true if item is still active (not claimed or expired) */
    public boolean isActive() {
        return "LOST".equals(status) || "FOUND".equals(status);
    }

    /** Returns true if this is a lost item report */
    public boolean isLost() { return "LOST".equalsIgnoreCase(type); }

    /** Returns true if this is a found item report */
    public boolean isFound() { return "FOUND".equalsIgnoreCase(type); }

    @Override
    public String toString() {
        return "Item{" +
               "itemId="   + itemId   +
               ", refCode='"+ refCode + '\'' +
               ", title='" + title    + '\'' +
               ", type='"  + type     + '\'' +
               ", status='"+ status   + '\'' +
               '}';
    }
}