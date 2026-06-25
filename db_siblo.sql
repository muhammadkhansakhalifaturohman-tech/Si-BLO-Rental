-- ============================================================
-- SI-BLO (Pro Court Rentals) - Full Schema + Seed Data
-- Target: PostgreSQL
-- Generated from JPA entities
-- ============================================================

-- Users
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL CHECK (role IN ('MEMBER', 'ADMIN')),
    membership_tier VARCHAR(100) DEFAULT 'PREMIUM MEMBER',
    avatar_url      VARCHAR(500)
);

-- Sports
CREATE TABLE sports (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    icon            VARCHAR(50),
    location_count  INTEGER DEFAULT 0,
    image_url       VARCHAR(500)
);

-- Venues
CREATE TABLE venues (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    address         VARCHAR(500),
    zone            VARCHAR(100),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION
);

-- Courts
CREATE TABLE courts (
    id              BIGSERIAL PRIMARY KEY,
    venue_id        BIGINT REFERENCES venues(id) ON DELETE SET NULL,
    sport_id        BIGINT REFERENCES sports(id) ON DELETE SET NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    surface_type    VARCHAR(100),
    indoor          BOOLEAN DEFAULT TRUE,
    price_per_hour  INTEGER NOT NULL,
    capacity        INTEGER NOT NULL,
    rating          DOUBLE PRECISION DEFAULT 0,
    review_count    INTEGER DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'INACTIVE')),
    image_url       VARCHAR(500),
    badge_label     VARCHAR(100),
    open_time       TIME DEFAULT '06:00:00',
    close_time      TIME DEFAULT '22:00:00',
    payment_link_url VARCHAR(500)
);

-- Time slots
CREATE TABLE time_slots (
    id              BIGSERIAL PRIMARY KEY,
    court_id        BIGINT NOT NULL REFERENCES courts(id) ON DELETE CASCADE,
    date            DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'HELD', 'BOOKED', 'BLOCKED')),
    version         BIGINT DEFAULT 0,
    UNIQUE (court_id, date, start_time)
);

-- Bookings
CREATE TABLE bookings (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    court_id        BIGINT NOT NULL REFERENCES courts(id) ON DELETE CASCADE,
    date            DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    total_price     INTEGER NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT' CHECK (status IN ('CONFIRMED', 'PENDING_PAYMENT', 'COMPLETED', 'CANCELLED', 'ACTIVE')),
    payment_expires_at TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Booking slot IDs (many-to-many relationship between bookings and time slots)
CREATE TABLE booking_slot_ids (
    booking_id      BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    slot_id         BIGINT NOT NULL,
    PRIMARY KEY (booking_id, slot_id)
);

-- Payments
CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    order_id        VARCHAR(255) NOT NULL UNIQUE,
    transaction_id  VARCHAR(255),
    booking_id      BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    amount          INTEGER NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    method          VARCHAR(50),
    paid_at         TIMESTAMP
);

-- Indexes
CREATE INDEX idx_courts_sport_id ON courts(sport_id);
CREATE INDEX idx_courts_status ON courts(status);
CREATE INDEX idx_time_slots_court_date ON time_slots(court_id, date);
CREATE INDEX idx_time_slots_date ON time_slots(date);
CREATE INDEX idx_time_slots_status ON time_slots(status);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_court_id ON bookings(court_id);
CREATE INDEX idx_bookings_date ON bookings(date);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_user_status ON bookings(user_id, status);
CREATE INDEX idx_payments_booking_id ON payments(booking_id);

-- ============================================================
-- SEED DATA
-- ============================================================
-- Password untuk seed: admin123 (BCrypt hash)
-- Password untuk seed: john123  (BCrypt hash)
-- Ganti dengan bcrypt hash yang benar jika perlu

INSERT INTO users (name, email, password, role, membership_tier)
VALUES ('Admin User', 'admin@siblo.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'System Master');

INSERT INTO users (name, email, password, role, membership_tier)
VALUES ('John Doe', 'john@siblo.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MEMBER', 'PREMIUM MEMBER');

INSERT INTO sports (name, slug, icon, location_count) VALUES
('Basketball', 'basketball', '🏀', 12),
('Futsal', 'futsal', '⚽', 8),
('Padel', 'padel', '🎾', 15),
('Badminton', 'badminton', '🏸', 6),
('Tennis', 'tennis', '🎾', 4);

INSERT INTO venues (name, address, zone, latitude, longitude) VALUES
('Downtown Arena', '123 Main St', 'Central', -6.2088, 106.8456),
('Glass Hub', '456 Park Ave', 'Westside', -6.2250, 106.8000),
('East Side Complex', '789 East Blvd', 'Eastside', -6.2200, 106.8800),
('Grand Slam Center', '321 Sports Rd', 'Central', -6.2400, 106.8600);

INSERT INTO courts (name, venue_id, sport_id, description, surface_type, indoor, price_per_hour, capacity, rating, review_count, status, image_url, badge_label, open_time, close_time, payment_link_url)
VALUES
('Skyline Hoops Premium', 1, 1, 'Premium basketball court with professional flooring and stadium lighting.', 'Hardwood', TRUE, 199000, 10, 4.9, 128, 'ACTIVE', '/images/skyline-Hoops.png', 'AVAILABLE', '06:00', '22:00', NULL),

('Velocity Padel Center', 2, 3, 'Professional padel court with synthetic turf and glass walls.', 'Synthetic', TRUE, 260000, 4, 5.0, 89, 'ACTIVE', '/images/velocity-padelCenter.png', 'TOP RATED', '06:00', '22:00', 'https://app.sandbox.midtrans.com/payment-links/414db0fa-bb87-44f8-a891-0cd9f6c57838-NhDpaERV'),

('Striker Futsal Indoor', 3, 2, 'Indoor futsal court with high-quality artificial grass.', 'Artificial Grass', TRUE, 255000, 12, 4.7, 215, 'ACTIVE', '/images/strikerFustalIndoor.png', '2 SLOTS LEFT', '06:00', '22:00', NULL),

('Grand Slam Center - Court 04', 4, 5, 'Premium professional acrylic surface with advanced shock absorption technology.', 'Acrylic', TRUE, 350000, 4, 4.8, 56, 'ACTIVE', '/images/grandSlamCourt04.png', 'PREMIUM', '06:00', '22:00', NULL),

('Downtown Badminton Hall', 1, 4, 'Professional badminton court with wooden flooring.', 'Wooden', TRUE, 150000, 4, 4.5, 34, 'ACTIVE', '/images/downtownBadmintonHall.png', 'AVAILABLE', '06:00', '22:00', NULL),

('Premium SoccerField 73', 1, 2, NULL, 'Hard Court', TRUE, 255000, 10, 4.5, 100, 'ACTIVE', '/images/area73.png', 'PREMIUM', '06:00', '22:00', NULL),

('Lapangan Voli', 3, 1, NULL, 'Clay', FALSE, 150000, 12, 4.2, 78, 'ACTIVE', '/images/Volypremium.png', 'AVAILABLE', '06:00', '22:00', 'https://app.sandbox.midtrans.com/payment-links/46fe5a45-61ee-4f1f-8ce9-649a56c07be5-6O37zTNI');
