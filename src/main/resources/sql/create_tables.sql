-- =====================================================
-- 소설 리뷰 서비스 DB 테이블 생성 SQL
-- DB: study_db (MariaDB)
-- 실행 순서: genres → users → novels → reviews
-- =====================================================

-- 1. 장르 테이블
CREATE TABLE IF NOT EXISTS genres (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. 사용자 테이블 (소셜 로그인 전용, 비밀번호 없음)
CREATE TABLE IF NOT EXISTS users (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    email             VARCHAR(255) NOT NULL UNIQUE,
    nickname          VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(2000),
    role              ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    provider          ENUM('GOOGLE', 'NAVER', 'KAKAO') NOT NULL,
    provider_id       VARCHAR(255) NOT NULL,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_provider_provider_id (provider, provider_id)
);

-- 3. 소설 테이블
CREATE TABLE IF NOT EXISTS novels (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(300) NOT NULL,
    author          VARCHAR(200),
    description     TEXT,
    cover_image_url VARCHAR(2000),
    genre_id        BIGINT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_novel_genre FOREIGN KEY (genre_id) REFERENCES genres(id)
);

-- 4. 리뷰 테이블
CREATE TABLE IF NOT EXISTS reviews (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    novel_id   BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    rating     TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content    TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_novel FOREIGN KEY (novel_id) REFERENCES novels(id),
    CONSTRAINT fk_review_user  FOREIGN KEY (user_id)  REFERENCES users(id),
    UNIQUE KEY uq_novel_user (novel_id, user_id)
);

-- 5. 장르 기본 데이터
INSERT IGNORE INTO genres (name) VALUES
    ('판타지'),
    ('로맨스'),
    ('무협'),
    ('현대'),
    ('SF'),
    ('공포'),
    ('추리'),
    ('역사');
