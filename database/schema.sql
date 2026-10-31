CREATE DATABASE IF NOT EXISTS cinebook_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cinebook_db;

CREATE TABLE IF NOT EXISTS movies (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(150) NOT NULL,
  genre VARCHAR(60) NOT NULL,
  duration_minutes INT NOT NULL,
  rating DECIMAL(2,1) DEFAULT 0.0,
  description VARCHAR(500),
  active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS shows (
  id INT AUTO_INCREMENT PRIMARY KEY,
  movie_id INT NOT NULL,
  show_time DATETIME NOT NULL,
  screen_name VARCHAR(80) NOT NULL DEFAULT 'CineBook Grand Screen',
  FOREIGN KEY (movie_id) REFERENCES movies(id),
  INDEX idx_show_movie_time(movie_id, show_time)
);

CREATE TABLE IF NOT EXISTS bookings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_code VARCHAR(24) NOT NULL UNIQUE,
  show_id INT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
  amount DECIMAL(10,2) NOT NULL,
  status ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
  payment_method VARCHAR(80) NOT NULL,
  payment_status ENUM('SIMULATED','NOT_PAID','REFUND_SIMULATED') NOT NULL DEFAULT 'SIMULATED',
  snacks BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (show_id) REFERENCES shows(id),
  INDEX idx_booking_show_status(show_id,status)
);

CREATE TABLE IF NOT EXISTS booking_seats (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT NOT NULL,
  show_id INT NOT NULL,
  seat_code VARCHAR(3) NOT NULL,
  status ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
  FOREIGN KEY (booking_id) REFERENCES bookings(id),
  FOREIGN KEY (show_id) REFERENCES shows(id),
  UNIQUE KEY uq_show_seat_status_slot (show_id, seat_code, status)
);

INSERT INTO movies (title,genre,duration_minutes,rating,description) VALUES
('Beyond the Stars','Sci-Fi',138,9.1,'A signal from the edge of the universe changes everything.'),
('The Last Kingdom','Adventure',126,8.7,'One final journey will decide the fate of a kingdom.'),
('Midnight Letters','Drama',114,8.9,'Some messages arrive exactly when we need them.'),
('Silent Signal','Thriller',121,8.5,'A strange transmission. A city with no answers.'),
('Little Big Planet','Animation',102,9.0,'A tiny explorer discovers a universe of possibilities.'),
('Velvet Horizon','Drama',132,8.6,'Two strangers meet where the ocean touches the sky.'),
('Code Red','Thriller',118,8.4,'The clock is ticking, and every second matters.'),
('Wildwood','Adventure',129,8.8,'An unexpected path leads to a world hidden in plain sight.')
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- Create demo showtimes for each movie, starting tomorrow.
INSERT INTO shows (movie_id,show_time,screen_name)
SELECT m.id, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00'), 'CineBook Grand Screen'
FROM movies m WHERE NOT EXISTS (SELECT 1 FROM shows s WHERE s.movie_id=m.id);
INSERT INTO shows (movie_id,show_time,screen_name)
SELECT m.id, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:15:00'), 'CineBook Grand Screen'
FROM movies m WHERE NOT EXISTS (SELECT 1 FROM shows s WHERE s.movie_id=m.id AND TIME(s.show_time)='14:15:00');
INSERT INTO shows (movie_id,show_time,screen_name)
SELECT m.id, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:30:00'), 'CineBook Grand Screen'
FROM movies m WHERE NOT EXISTS (SELECT 1 FROM shows s WHERE s.movie_id=m.id AND TIME(s.show_time)='19:30:00');
