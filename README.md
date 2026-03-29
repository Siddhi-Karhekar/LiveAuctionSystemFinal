# 🔨 Live Auction Bidding System
### Java Servlets + JSP + JDBC + MySQL + Tomcat

---

## 📁 Complete Project Structure

```
LiveAuctionSystem/
├── pom.xml                                    ← Maven build
├── sql/
│   └── schema.sql                             ← DB schema + sample data
└── src/main/
    ├── java/
    │   ├── model/
    │   │   ├── User.java
    │   │   ├── Item.java
    │   │   ├── Auction.java
    │   │   └── Bid.java
    │   ├── dao/
    │   │   ├── UserDAO.java
    │   │   ├── ItemDAO.java
    │   │   ├── AuctionDAO.java
    │   │   └── BidDAO.java
    │   ├── servlet/
    │   │   ├── AuthenticationServlet.java     → /auth
    │   │   ├── ItemServlet.java               → /item
    │   │   ├── AuctionServlet.java            → /auction
    │   │   ├── BidServlet.java                → /bid
    │   │   └── SellerServlet.java             → /seller
    │   ├── util/
    │   │   ├── DBConnection.java
    │   │   ├── PasswordUtil.java
    │   │   └── ValidationUtil.java
    │   └── swing/
    │       └── AdminPanel.java                ← Desktop admin tool
    └── webapp/
        ├── index.jsp                          ← Root redirect
        ├── css/style.css
        ├── js/main.js
        ├── uploads/                           ← Image uploads (auto-created)
        ├── WEB-INF/
        │   └── web.xml
        └── jsp/
            ├── header.jsp
            ├── footer.jsp
            ├── login.jsp
            ├── register.jsp
            ├── auction_list.jsp
            ├── auction_detail.jsp
            ├── seller_dashboard.jsp
            ├── create_auction.jsp
            ├── bid_history.jsp
            ├── error404.jsp
            └── error500.jsp
```

---

## ⚙️ Prerequisites

| Tool         | Version Required |
|--------------|-----------------|
| Java JDK     | 11 or later     |
| Apache Maven | 3.8+            |
| Apache Tomcat| 9.x or 10.x     |
| MySQL Server | 5.7+ or 8.x     |

---

## 🗄️ Step 1 — Database Setup

1. Start MySQL and open a client (MySQL Workbench, DBeaver, or CLI):

```bash
mysql -u root -p
```

2. Run the schema file:

```sql
SOURCE /path/to/LiveAuctionSystem/sql/schema.sql;
```

Or paste the file contents directly.

This creates:
- Database `live_auction_db`
- Tables: `users`, `items`, `auctions`, `bids`
- 5 sample users, 4 items, 4 auctions, sample bids

---

## 🔑 Step 2 — Configure Database Connection

Edit `src/main/java/util/DBConnection.java`:

```java
private static final String DB_URL      = "jdbc:mysql://localhost:3306/live_auction_db?useSSL=false&serverTimezone=UTC";
private static final String DB_USER     = "root";
private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";  // ← change this
```

---

## 🏗️ Step 3 — Build the WAR

From the project root (where `pom.xml` is):

```bash
mvn clean package
```

This generates:  
`target/LiveAuctionSystem.war`

---

## 🚀 Step 4 — Deploy to Tomcat

### Option A — Hot Drop (easiest)

Copy the WAR to Tomcat's webapps directory:

```bash
# Linux / macOS
cp target/LiveAuctionSystem.war /opt/tomcat/webapps/

# Windows
copy target\LiveAuctionSystem.war C:\tomcat\webapps\
```

Start Tomcat:
```bash
# Linux / macOS
$CATALINA_HOME/bin/startup.sh

# Windows
%CATALINA_HOME%\bin\startup.bat
```

Tomcat auto-deploys the WAR. Visit:  
**http://localhost:8080/LiveAuctionSystem**

### Option B — Tomcat Manager

1. Go to http://localhost:8080/manager/html
2. Log in with a manager user
3. Scroll to "Deploy" → "WAR file to deploy"
4. Upload `target/LiveAuctionSystem.war`

### Option C — Eclipse/IntelliJ

- **Eclipse**: Right-click project → Run As → Run on Server → pick Tomcat
- **IntelliJ**: Run/Edit Configurations → Tomcat Server → Local → add artifact (WAR exploded)

---

## 🌐 Step 5 — Access the Application

| URL | Description |
|-----|-------------|
| http://localhost:8080/LiveAuctionSystem/ | Root (auto-redirects based on login) |
| http://localhost:8080/LiveAuctionSystem/auth?action=login | Login page |
| http://localhost:8080/LiveAuctionSystem/auth?action=register | Registration |
| http://localhost:8080/LiveAuctionSystem/auction?action=list | Browse auctions (buyer) |
| http://localhost:8080/LiveAuctionSystem/seller?action=dashboard | Seller dashboard |

---

## 🧪 Demo Accounts

| Role   | Email               | Password    |
|--------|---------------------|-------------|
| Admin  | admin@auction.com   | password123 |
| Seller | alice@auction.com   | password123 |
| Seller | dan@auction.com     | password123 |
| Buyer  | bob@auction.com     | password123 |
| Buyer  | carol@auction.com   | password123 |

---

## 🖥️ Step 6 — Run Swing Admin Panel (Optional)

Compile with MySQL driver on classpath:

```bash
# Compile
javac -cp target/LiveAuctionSystem/WEB-INF/lib/mysql-connector-java-8.0.33.jar \
      -sourcepath src/main/java \
      -d target/classes \
      src/main/java/swing/AdminPanel.java \
      src/main/java/dao/*.java \
      src/main/java/model/*.java \
      src/main/java/util/*.java

# Run
java -cp target/classes:target/LiveAuctionSystem/WEB-INF/lib/mysql-connector-java-8.0.33.jar \
     swing.AdminPanel
```

On Windows, replace `:` with `;` in the classpath.

---

## 🔄 How Real-Time Updates Work

The auction detail page uses **AJAX polling** (no WebSockets):

```
Browser → GET /bid?action=status&id={auctionId} every 5 seconds
       ← { "highestBid": 3200.00, "totalBids": 4, "status": "active" }
Browser updates the bid display and countdown timer without full reload.
```

---

## 📋 Servlet URL Reference

| Servlet               | URL       | Method | Action           | Description                    |
|-----------------------|-----------|--------|------------------|-------------------------------|
| AuthenticationServlet | /auth     | GET    | login            | Show login page               |
| AuthenticationServlet | /auth     | POST   | login            | Authenticate user             |
| AuthenticationServlet | /auth     | GET    | register         | Show register page            |
| AuthenticationServlet | /auth     | POST   | register         | Create new account            |
| AuthenticationServlet | /auth     | GET    | logout           | Destroy session               |
| ItemServlet           | /item     | GET    | form             | Show create auction form      |
| ItemServlet           | /item     | POST   | create           | Save item + create auction    |
| AuctionServlet        | /auction  | GET    | list             | Browse active auctions        |
| AuctionServlet        | /auction  | GET    | detail&id=X      | Auction detail + bid form     |
| AuctionServlet        | /auction  | POST   | selectWinner     | Seller sets winner            |
| BidServlet            | /bid      | POST   | place            | Place a bid                   |
| BidServlet            | /bid      | GET    | status&id=X      | AJAX: current bid JSON        |
| BidServlet            | /bid      | GET    | history&id=X     | Full bid history page         |
| SellerServlet         | /seller   | GET    | dashboard        | Seller's auction list         |

---

## 🔒 Security Notes

- Passwords are **SHA-256 hashed** (no plain-text storage)
- Session checked on **every protected page**
- All DB queries use **PreparedStatements** (no SQL injection)
- File uploads restricted to **image MIME types**, max 5MB
- Server-side validation on all form inputs
- Sellers cannot place bids (enforced server-side)

---

## 🛠️ Troubleshooting

| Problem | Solution |
|---------|----------|
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Add mysql-connector-java JAR to WEB-INF/lib |
| `Access denied for user 'root'@'localhost'` | Check DB_PASSWORD in DBConnection.java |
| Images not showing | Ensure `uploads/` directory is writable by Tomcat |
| Port 8080 in use | Change Tomcat port in `conf/server.xml` |
| Session expires immediately | Check `session-timeout` in web.xml (default 30 min) |

---

## 📐 Database Schema Reference

```sql
users    (id, name, email, password, role, created_at)
items    (id, seller_id→users, name, description, start_price, image_path, created_at)
auctions (id, item_id→items, start_time, end_time, status, winner_id→users, winning_bid, created_at)
bids     (id, auction_id→auctions, bidder_id→users, bid_amount, bid_time)
```

Status flow: `scheduled` → `active` → `ended`  
(Auto-updated by `AuctionDAO.syncStatuses()` called on every list/detail request)

---

*Built with ❤️ using pure Java — no Spring, no React, no magic.*

---

## 🆕 Additional Components (v1.1)

### BuyerServlet (`/buyer`)
- `GET /buyer?action=dashboard` → `buyer_dashboard.jsp`
- Shows buyer's complete bid history, auctions entered, total bid value, and auctions won with winning amounts

### SessionFilter
- Automatically protects `/auction`, `/bid`, `/seller`, `/item`, `/buyer`
- Redirects unauthenticated requests to `/auth?action=login`
- Enforces seller-only access on seller/item routes
- Eliminates repeated session-check code in each servlet

### Eclipse / IntelliJ Import
- **Eclipse**: File → Import → Existing Maven Projects → select `LiveAuctionSystem/` folder
- **IntelliJ**: File → Open → select `LiveAuctionSystem/` folder → trust project → Maven auto-imports
