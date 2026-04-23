package com.lostfound.models;

/**
 * Category.java
 * ------------------------------------------------
 * Represents a row in the 'categories' table.
 * Simple two-field model used to populate
 * dropdowns in the item reporting form.
 * ------------------------------------------------
 */
public class Category {

    private int    categoryId;
    private String categoryName;

    // ── Constructors ──────────────────────────────────────────
    public Category() {}

    public Category(int categoryId, String categoryName) {
        this.categoryId   = categoryId;
        this.categoryName = categoryName;
    }

    // ── Getters ───────────────────────────────────────────────
    public int    getCategoryId()   { return categoryId;   }
    public String getCategoryName() { return categoryName; }

    // ── Setters ───────────────────────────────────────────────
    public void setCategoryId(int categoryId)       { this.categoryId   = categoryId;   }
    public void setCategoryName(String categoryName){ this.categoryName = categoryName; }

    @Override
    public String toString() {
        return "Category{id=" + categoryId + ", name='" + categoryName + "'}";
    }
}