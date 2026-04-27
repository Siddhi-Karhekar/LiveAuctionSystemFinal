-- ============================================================
-- Live Auction Bidding System - MySQL Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS live_auction_db;
USE live_auction_db;

-- ----------------------------------------------------------------
-- Table: users
-- Stores all users (buyers and sellers)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,   -- SHA-256 hashed
    role        ENUM('buyer','seller','admin') NOT NULL DEFAULT 'buyer',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- Table: items
-- Items listed by sellers
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS items (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    seller_id   INT NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    start_price DECIMAL(12,2) NOT NULL,
    image_path  VARCHAR(500),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- Table: auctions
-- Each item has one auction with a time window
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auctions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    item_id         INT NOT NULL UNIQUE,
    start_time      DATETIME NOT NULL,
    end_time        DATETIME NOT NULL,
    status          ENUM('scheduled','active','ended','cancelled') DEFAULT 'scheduled',
    winner_id       INT,                -- set by seller after auction ends
    winning_bid     DECIMAL(12,2),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id)    REFERENCES items(id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id)  REFERENCES users(id) ON DELETE SET NULL
);

-- ----------------------------------------------------------------
-- Table: bids
-- All bids placed by buyers
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bids (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    auction_id  INT NOT NULL,
    bidder_id   INT NOT NULL,
    bid_amount  DECIMAL(12,2) NOT NULL,
    bid_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    FOREIGN KEY (bidder_id)  REFERENCES users(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- Table: notifications
-- Win/loss notices generated when a seller selects a winner.
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    auction_id  INT NOT NULL,
    type        ENUM('win','loss') NOT NULL,
    message     VARCHAR(255) NOT NULL,
    is_read     TINYINT(1) NOT NULL DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- Sample Data
-- ----------------------------------------------------------------

-- Users (password = 'password123' hashed with SHA-256)
INSERT INTO users (name, email, password, role) VALUES
('Admin User',   'admin@auction.com',  SHA2('password123',256), 'admin'),
('Alice Seller', 'alice@auction.com',  SHA2('password123',256), 'seller'),
('Bob Buyer',    'bob@auction.com',    SHA2('password123',256), 'buyer'),
('Carol Buyer',  'carol@auction.com',  SHA2('password123',256), 'buyer'),
('Dan Seller',   'dan@auction.com',    SHA2('password123',256), 'seller');

-- Items by Alice (seller_id=2)
INSERT INTO items (seller_id, name, description, start_price, image_path) VALUES
(2, 'Vintage Rolex Watch',  'A beautiful 1965 Rolex Submariner in excellent condition.', 2500.00, 'uploads/rolex.jpg'),
(2, 'Antique Vase',         'Ming Dynasty inspired ceramic vase, hand-painted.', 800.00,  'uploads/vase.jpg'),
(5, 'Gaming Laptop',        'ASUS ROG gaming laptop, RTX 4080, 32GB RAM, barely used.', 1500.00, 'uploads/laptop.jpg'),
(5, 'Rare Baseball Card',   '1952 Mickey Mantle Topps rookie card, PSA graded 6.', 5000.00, 'uploads/card.jpg');

-- Auctions (times relative so they work at demo time)
INSERT INTO auctions (item_id, start_time, end_time, status) VALUES
(1, DATE_SUB(NOW(), INTERVAL 1 HOUR),  DATE_ADD(NOW(), INTERVAL 5 HOUR),  'active'),
(2, DATE_SUB(NOW(), INTERVAL 2 HOUR),  DATE_ADD(NOW(), INTERVAL 2 HOUR),  'active'),
(3, DATE_ADD(NOW(), INTERVAL 2 HOUR),  DATE_ADD(NOW(), INTERVAL 8 HOUR),  'scheduled'),
(4, DATE_SUB(NOW(), INTERVAL 10 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR),  'ended');

-- Sample bids on auction 1 (Rolex)
INSERT INTO bids (auction_id, bidder_id, bid_amount) VALUES
(1, 3, 2600.00),
(1, 4, 2750.00),
(1, 3, 3000.00),
(1, 4, 3200.00);

-- Sample bids on auction 2 (Vase)
INSERT INTO bids (auction_id, bidder_id, bid_amount) VALUES
(2, 3, 850.00),
(2, 4, 900.00);

-- Sample bids on ended auction 4
INSERT INTO bids (auction_id, bidder_id, bid_amount) VALUES
(4, 3, 5100.00),
(4, 4, 5500.00),
(4, 3, 5800.00);
