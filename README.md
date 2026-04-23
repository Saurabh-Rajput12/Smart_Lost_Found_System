# 🔍 Smart Lost & Found System

A full-stack **Dynamic Web Application** built with Java Servlets, JDBC, MySQL, and vanilla HTML/CSS/JavaScript. Designed to help institutions (colleges, offices, public spaces) manage lost and found items efficiently with smart matching, claim workflows, and real-time notifications.

---

## 📸 Screenshots

> Login Page · Admin Dashboard · User Dashboard · Search & Claim

---

## ✨ Features

### 👥 User Roles
| Feature | Admin | User |
|---|---|---|
| View Dashboard | ✅ | ✅ |
| Report Lost/Found Items | ✅ | ✅ |
| Search & Filter Items | ✅ | ✅ |
| Submit Claims | ❌ | ✅ |
| Approve / Reject Claims | ✅ | ❌ |
| Manage Users (Activate/Deactivate) | ✅ | ❌ |
| Export Reports (CSV) | ✅ | ❌ |
| Receive Notifications | ✅ | ✅ |

### 🔐 Authentication
- User Registration & Login
- Admin Login with role-based redirect
- Session management (30-minute timeout)
- Session filter protecting all pages
- Logout with session invalidation

### 📦 Item Reporting
- Report Lost or Found items with full details
- Image upload (stored as file path)
- Auto-generated unique reference codes: `LF-YYYY-XXXXX`
- Categories: Electronics, Clothing, Documents, Keys, Bags, Jewellery, Books, Sports, Other

### 🔍 Search & Smart Matching
- Filter by keyword, category, type, status, location, date range
- Smart matching engine pairs Lost ↔ Found items by category + keyword similarity
- Matched items stored in database and shown to users
- Notifications sent automatically on match

### 📬 Claim System
- Users submit claims with proof description
- Admin approves or rejects with one click
- On approval: item status → `CLAIMED`, all other claims → `REJECTED`
- Claimants notified via in-app notifications

### 🔔 Notification System
- In-app notifications for claim approval/rejection and smart matches
- Unread count badge on login
- Mark all as read

### ⏳ Auto-Expiry
- Items older than 30 days automatically marked `EXPIRED` on every login

### 📊 Admin Dashboard
- Live stats: Total Lost, Found, Claimed, Expired, Users, Pending Claims
- Pending claims management table
- Recent activity feed
- User management: view all users, activate/deactivate accounts

### 📁 Reports & Export
- Filter by date range and status
- Export to **CSV** (downloads instantly)

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 |
| **Backend** | Jakarta Servlets (Tomcat 10.1) |
| **Database** | MySQL 8.x |
| **DB Connectivity** | JDBC (mysql-connector-j 8.3.0) |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript |
| **JSON** | Gson 2.10.1 |
| **Build Tool** | Maven |
| **IDE** | Eclipse IDE |
| **Server** | Apache Tomcat 10.1 |

---

## 📁 Project Structure

```
LostFoundSystem/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/lostfound/
│       │       ├── auth/
│       │       │   ├── LoginServlet.java
│       │       │   ├── RegisterServlet.java
│       │       │   ├── LogoutServlet.java
│       │       │   └── SessionFilter.java
│       │       ├── models/
│       │       │   ├── User.java
│       │       │   ├── Item.java
│       │       │   ├── Category.java
│       │       │   ├── Claim.java
│       │       │   ├── Match.java
│       │       │   └── Notification.java
│       │       ├── dao/
│       │       │   ├── UserDAO.java
│       │       │   ├── ItemDAO.java
│       │       │   ├── CategoryDAO.java
│       │       │   ├── ClaimDAO.java
│       │       │   ├── MatchDAO.java
│       │       │   └── NotificationDAO.java
│       │       ├── servlet/
│       │       │   ├── AdminDashboardServlet.java
│       │       │   ├── UserDashboardServlet.java
│       │       │   ├── ItemReportServlet.java
│       │       │   ├── ItemSearchServlet.java
│       │       │   ├── ClaimServlet.java
│       │       │   ├── ClaimActionServlet.java
│       │       │   ├── CategoryServlet.java
│       │       │   ├── UserStatusServlet.java
│       │       │   ├── NotificationServlet.java
│       │       │   └── ReportExportServlet.java
│       │       └── utils/
│       │           ├── DBConfig.java
│       │           ├── DBConnection.java
│       │           └── DBTestServlet.java
│       └── webapp/
│           ├── pages/
│           │   ├── login.html
│           │   ├── register.html
│           │   ├── admin-dashboard.html
│           │   └── user-dashboard.html
│           ├── assets/
│           │   ├── css/
│           │   │   ├── auth.css
│           │   │   └── dashboard.css
│           │   ├── js/
│           │   │   ├── auth.js
│           │   │   └── dashboard.js
│           │   └── uploads/          ← uploaded item images stored here
│           └── WEB-INF/
│               └── web.xml
├── db/
│   └── lostfound.sql                 ← full database schema + seed data
└── pom.xml
```

---

## 🗃️ Database Schema

```
smartlostfound_db
├── users          → user_id, name, email, password, phone, role, status
├── categories     → category_id, category_name
├── items          → item_id, ref_code, title, description, category_id,
│                    type, status, location, date_occurred, image_path,
│                    reported_by, created_at
├── claims         → claim_id, item_id, claimed_by, proof_description,
│                    status, claimed_at
├── matches        → match_id, lost_item_id, found_item_id, match_date
└── notifications  → notif_id, user_id, message, is_read, created_at
```

**Item status lifecycle:**
```
LOST / FOUND  →  CLAIMED  (on claim approval)
LOST / FOUND  →  EXPIRED  (after 30 days)
```

---

## ⚙️ Getting Started

### Prerequisites

Make sure you have the following installed:

- [Java JDK 17+](https://www.oracle.com/java/technologies/downloads/)
- [Apache Tomcat 10.1](https://tomcat.apache.org/download-10.cgi)
- [MySQL 8.x](https://dev.mysql.com/downloads/mysql/)
- [MySQL Workbench](https://dev.mysql.com/downloads/workbench/)
- [Eclipse IDE for Enterprise Java](https://www.eclipse.org/downloads/)
- [Maven](https://maven.apache.org/) (or use Eclipse's built-in Maven)

---

### 🚀 Installation & Setup

#### 1. Clone the Repository

```bash
git clone https://github.com/your-username/SmartLostFoundSystem.git
cd SmartLostFoundSystem
```

#### 2. Set Up the Database

1. Open **MySQL Workbench**
2. Go to `File → Open SQL Script`
3. Select `db/lostfound.sql`
4. Press `Ctrl + Shift + Enter` to run the entire script
5. Verify: you should see `smartlostfound_db` with 6 tables and seed data

#### 3. Configure Database Credentials

Open `src/main/java/com/lostfound/utils/DBConfig.java` and update:

```java
public static final String USERNAME = "root";
public static final String PASSWORD = "your_mysql_password"; // ← change this
public static final String DATABASE = "smartlostfound_db";
```

#### 4. Import into Eclipse

1. `File → Import → Maven → Existing Maven Projects`
2. Select the cloned project folder
3. Click **Finish**
4. Right-click project → `Maven → Update Project` → tick **Force Update** → OK

#### 5. Add Tomcat Server

1. `Window → Preferences → Server → Runtime Environments`
2. Click **Add → Apache Tomcat v10.1**
3. Point to your Tomcat installation folder
4. Click **Finish**

#### 6. Deploy and Run

1. Right-click project → `Run As → Run on Server`
2. Select your Tomcat 10.1 server
3. Click **Finish**
4. Browser opens automatically at:

```
http://localhost:8080/LostFoundSystem/pages/login.html
```

---

## 🔑 Default Login Credentials

| Role | Email | Password |
|---|---|---|
| **Admin** | admin@lostfound.com | admin123 |
| **User 1** | alice@example.com | user123 |
| **User 2** | bob@example.com | user123 |

> ⚠️ Change these credentials before deploying to production.

---

## 🔗 API Endpoints

| Method | URL | Description | Access |
|---|---|---|---|
| `POST` | `/login` | Authenticate user | Public |
| `POST` | `/register` | Register new user | Public |
| `GET` | `/logout` | End session | Logged in |
| `GET` | `/admin/dashboard` | Admin stats + data | Admin |
| `GET` | `/user/dashboard` | User personal data | User |
| `POST` | `/item/report` | Report lost/found item | User |
| `GET` | `/item/search` | Search with filters | User |
| `GET` | `/categories` | List all categories | Logged in |
| `POST` | `/claim/submit` | Submit a claim | User |
| `POST` | `/claim/action` | Approve/reject claim | Admin |
| `POST` | `/user/status` | Activate/deactivate user | Admin |
| `POST` | `/notifications/read` | Mark all notifications read | Logged in |
| `GET` | `/report/export` | Download CSV report | Admin |

---

## 🧪 Testing the Application

### End-to-End Test Flow

```
1. Register a new user account
2. Login as the new user
3. Report a LOST item (e.g. "Black Wallet, Documents category")
4. Report a FOUND item (e.g. "Brown Wallet, Documents category")
   → Smart match notification should appear
5. Search for items using keyword filter
6. Submit a claim on a FOUND item
7. Logout → Login as Admin
8. View Pending Claims → Approve the claim
9. Login back as user → Check notifications
10. Admin: Export CSV report
```

### Verify Database After Each Step

```sql
USE smartlostfound_db;

SELECT * FROM users;
SELECT * FROM items ORDER BY created_at DESC;
SELECT * FROM claims;
SELECT * FROM matches;
SELECT * FROM notifications ORDER BY created_at DESC;
```

---

## 📦 Maven Dependencies

```xml
<!-- MySQL JDBC Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>

<!-- Jakarta Servlet API -->
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>6.0.0</version>
    <scope>provided</scope>
</dependency>

<!-- Gson for JSON serialization -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

---

## 🚧 Known Limitations & Future Improvements

- [ ] Password hashing (BCrypt) — currently stored as plain text
- [ ] Email OTP for Forgot Password
- [ ] PDF export (currently CSV only)
- [ ] Real-time notifications (WebSocket)
- [ ] Image thumbnail preview in item listings
- [ ] Pagination for large item lists
- [ ] Advanced NLP-based smart matching
- [ ] Mobile app (Android/iOS)
- [ ] Docker containerization

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m "Add: your feature description"`
4. Push to branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

---

## 👨‍💻 Author

**Your Name**
- GitHub: [Saurabh-Rajput12](https://github.com/Saurabh-Rajput12)
- Email: Saurabhkumarsingh.smn@gmail.com

---

## 🙏 Acknowledgements

- [Apache Tomcat](https://tomcat.apache.org/) — Java web server
- [MySQL](https://www.mysql.com/) — Database
- [Google Gson](https://github.com/google/gson) — JSON library
- [Eclipse IDE](https://www.eclipse.org/) — Development environment

---

> Built as a complete step-by-step project — from database schema to working web application.
