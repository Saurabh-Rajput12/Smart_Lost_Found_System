package com.lostfound.utils;

/**
 * DBConfig.java
 * ------------------------------------------------------------
 * Central place for all database configuration constants.
 * If you change your DB password or host, edit ONLY this file.
 *
 * Place: src/main/java/com/lostfound/utils/DBConfig.java
 * ------------------------------------------------------------
 */
public class DBConfig {

    // ── Database host and port ────────────────────────────────
    public static final String HOST     = "localhost";
    public static final String PORT     = "3306";

    // ── Your database name (as confirmed) ────────────────────
    public static final String DATABASE = "smartlostfound_db";

    // ── MySQL credentials — change to match your setup ───────
    public static final String USERNAME = "root";
    public static final String PASSWORD = "25MCD10005"; // ← change this

    // ── Full JDBC connection URL ──────────────────────────────
    // useSSL=false          → disables SSL warning in local dev
    // serverTimezone=UTC    → avoids timezone mismatch errors
    // allowPublicKeyRetrieval=true → needed for MySQL 8.x auth
    public static final String URL =
        "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
        + "?useSSL=false"
        + "&serverTimezone=UTC"
        + "&allowPublicKeyRetrieval=true"
        + "&characterEncoding=UTF-8";

    // ── JDBC Driver class name ────────────────────────────────
    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // Private constructor — this class should never be instantiated
    private DBConfig() {}
}