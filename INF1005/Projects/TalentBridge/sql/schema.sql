-- talentbridge database schema
-- mysql 8.0+ compatible
-- charset: utf8mb4, collation: utf8mb4_unicode_ci

CREATE DATABASE IF NOT EXISTS talentbridge
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE talentbridge;

-- ============================================================
-- users table — all platform users regardless of role
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    unique_id    INT UNSIGNED NOT NULL UNIQUE,
    name         VARCHAR(100)  NOT NULL,
    email        VARCHAR(255)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role         ENUM('seeker', 'employer', 'admin') NOT NULL DEFAULT 'seeker',
    status       VARCHAR(50)   NOT NULL DEFAULT 'Offline now',
    failed_login_attempts INT UNSIGNED NOT NULL DEFAULT 0,
    last_failed_login_at  DATETIME     NULL DEFAULT NULL,
    lockout_count         INT UNSIGNED NOT NULL DEFAULT 0,
    is_active    TINYINT(1)   NOT NULL DEFAULT 1,
    email_verified_at DATETIME     NULL DEFAULT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_email  (email),
    INDEX idx_users_role   (role),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS messages (
    message_id   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    incoming_id  INT UNSIGNED NOT NULL,
    outgoing_id  INT UNSIGNED NOT NULL,
    message      TEXT NOT NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_messages_incoming (incoming_id),
    INDEX idx_messages_outgoing (outgoing_id),
    INDEX idx_messages_conv (incoming_id, outgoing_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- seeker_profiles table — job seeker extended profile
-- ============================================================
CREATE TABLE IF NOT EXISTS seeker_profiles (
    profile_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id     INT UNSIGNED NOT NULL UNIQUE,
    headline    VARCHAR(255) DEFAULT NULL,
    skills      TEXT         DEFAULT NULL,
    cv_path     VARCHAR(512) DEFAULT NULL,
    location    VARCHAR(150) DEFAULT NULL,
    CONSTRAINT fk_seeker_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- companies table — employer company profiles
-- ============================================================
CREATE TABLE IF NOT EXISTS companies (
    company_id   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id      INT UNSIGNED NOT NULL UNIQUE,
    company_name VARCHAR(200) NOT NULL,
    industry     VARCHAR(150) DEFAULT NULL,
    description  TEXT         DEFAULT NULL,
    logo_path    VARCHAR(512) DEFAULT NULL,
    CONSTRAINT fk_company_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- job_listings table — jobs posted by employers
-- ============================================================
CREATE TABLE IF NOT EXISTS job_listings (
    job_id       INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    company_id   INT UNSIGNED NOT NULL,
    title        VARCHAR(200) NOT NULL,
    description  TEXT         NOT NULL,
    location     VARCHAR(150) DEFAULT NULL,
    type         ENUM('Full-time', 'Part-time', 'Contract', 'Internship', 'Remote') NOT NULL DEFAULT 'Full-time',
    salary_min    DECIMAL(10,2) DEFAULT NULL,
    salary_max    DECIMAL(10,2) DEFAULT NULL,
    salary_period ENUM('per hour','per day','per month','per annum') DEFAULT NULL,
    status       ENUM('active', 'closed', 'draft') NOT NULL DEFAULT 'active',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_listing_company
        FOREIGN KEY (company_id) REFERENCES companies (company_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_listing_status    (status),
    INDEX idx_listing_company   (company_id),
    INDEX idx_listing_created   (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- applications table — seeker applications for job listings
-- ============================================================
CREATE TABLE IF NOT EXISTS applications (
    application_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    job_id         INT UNSIGNED NOT NULL,
    user_id        INT UNSIGNED NOT NULL,
    cover_letter   TEXT         DEFAULT NULL,
    status         ENUM('Pending', 'Reviewed', 'Shortlisted', 'Rejected') NOT NULL DEFAULT 'Pending',
    applied_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_job
        FOREIGN KEY (job_id) REFERENCES job_listings (job_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_app_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    -- prevent duplicate applications
    UNIQUE KEY uq_application (job_id, user_id),
    INDEX idx_app_user   (user_id),
    INDEX idx_app_job    (job_id),
    INDEX idx_app_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- saved_jobs table — seeker bookmarked listings
-- ============================================================
CREATE TABLE IF NOT EXISTS saved_jobs (
    save_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id  INT UNSIGNED NOT NULL,
    job_id   INT UNSIGNED NOT NULL,
    saved_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_save_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_save_job
        FOREIGN KEY (job_id) REFERENCES job_listings (job_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    -- prevent duplicate saves
    UNIQUE KEY uq_saved_job (user_id, job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- contact_messages table — public contact form submissions
-- ============================================================
CREATE TABLE IF NOT EXISTS contact_messages (
    message_id   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    subject      VARCHAR(255) NOT NULL,
    body         TEXT         NOT NULL,
    submitted_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read      TINYINT(1)   NOT NULL DEFAULT 0,
    INDEX idx_msg_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- login_failures table — tracks failed login attempts by ip
-- ============================================================
CREATE TABLE IF NOT EXISTS login_failures (
    failure_id      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ip_address      VARCHAR(45)  NOT NULL,
    email_attempted VARCHAR(255) NOT NULL,
    attempted_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_failure_ip_time (ip_address, attempted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `audit_log`
--
CREATE TABLE `audit_log` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT UNSIGNED NULL,
  `ip_address` VARCHAR(45) NOT NULL,
  `action` VARCHAR(50) NOT NULL,
  `details` TEXT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_audit_user` (`user_id`),
  INDEX `idx_audit_action` (`action`),
  INDEX `idx_audit_ip` (`ip_address`),
  FULLTEXT `idx_audit_details` (`details`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `reviews`
--
CREATE TABLE IF NOT EXISTS reviews (
    review_id    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    reviewer_id  INT UNSIGNED NOT NULL,
    target_type  ENUM('company', 'seeker', 'platform') NOT NULL,
    target_id    INT UNSIGNED NULL,         -- NULL for 'platform' reviews
    rating       TINYINT UNSIGNED NOT NULL CHECK (rating BETWEEN 1 AND 5),
    body         TEXT NOT NULL,
    is_approved  TINYINT(1) NOT NULL DEFAULT 1,  -- set 0 if you want moderation
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_user
        FOREIGN KEY (reviewer_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_review_target (target_type, target_id),
    INDEX idx_review_reviewer (reviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- ============================================================
-- email_verification_tokens table — for account email verification
-- ============================================================
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    token_id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id      INT UNSIGNED NOT NULL,
    token_hash   VARCHAR(255) NOT NULL,
    expires_at   DATETIME     NOT NULL,
    used_at      DATETIME     NULL DEFAULT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ev_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_ev_user (user_id),
    INDEX idx_ev_hash (token_hash),
    INDEX idx_ev_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- password_reset_tokens table — for password reset flows
-- ============================================================
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    token_id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id      INT UNSIGNED NOT NULL,
    token_hash   VARCHAR(255) NOT NULL,
    expires_at   DATETIME     NOT NULL,
    used_at      DATETIME     NULL DEFAULT NULL,
    requested_ip VARCHAR(45)  NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prt_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_prt_user (user_id),
    INDEX idx_prt_hash (token_hash),
    INDEX idx_prt_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
