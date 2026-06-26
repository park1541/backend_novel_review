-- =====================================================
-- 소설 리뷰 서비스 DB 테이블 생성 SQL
-- DB: PostgreSQL (Supabase)
-- 실행 순서: 함수 → genres → users → novels → reviews
--           → review_likes → review_reports → search_logs → inquiries
--           → 트리거 → 장르 시드
-- =====================================================

-- updated_at 자동 갱신 트리거 함수
-- (MySQL의 ON UPDATE CURRENT_TIMESTAMP는 PostgreSQL에 없어 트리거로 대체)
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 1. 장르
CREATE TABLE IF NOT EXISTS genres (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. 사용자 (소셜 로그인 전용, 비밀번호 없음)
CREATE TABLE IF NOT EXISTS users (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    nickname            VARCHAR(100) NOT NULL,
    profile_image_url   VARCHAR(2000),
    role                VARCHAR(10)  NOT NULL DEFAULT 'USER'
                            CHECK (role IN ('USER', 'ADMIN')),
    provider            VARCHAR(10)  NOT NULL
                            CHECK (provider IN ('GOOGLE', 'NAVER', 'KAKAO')),
    provider_id         VARCHAR(255) NOT NULL,
    social_access_token TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_provider_provider_id UNIQUE (provider, provider_id)
);

-- 3. 소설
CREATE TABLE IF NOT EXISTS novels (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title           VARCHAR(300) NOT NULL,
    author          VARCHAR(200),
    description     TEXT,
    cover_image_url TEXT,
    genre_id        BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_novel_genre FOREIGN KEY (genre_id) REFERENCES genres(id)
);

-- 4. 리뷰 (user_id NULL 허용 + ON DELETE SET NULL → 자진 탈퇴 시 리뷰 유지)
CREATE TABLE IF NOT EXISTS reviews (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    novel_id   BIGINT NOT NULL,
    user_id    BIGINT,
    rating     SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    content    TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_novel FOREIGN KEY (novel_id) REFERENCES novels(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user  FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE SET NULL,
    CONSTRAINT uq_novel_user UNIQUE (novel_id, user_id)
);

-- 5. 리뷰 좋아요 (review_id + user_id UNIQUE, 둘 다 ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS review_likes (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    review_id  BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_like_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_user   FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT uq_review_user_like UNIQUE (review_id, user_id)
);

-- 6. 리뷰 신고 (review_id + reporter_id UNIQUE → 중복 신고 방지)
CREATE TABLE IF NOT EXISTS review_reports (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    review_id   BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason      VARCHAR(500),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_review FOREIGN KEY (review_id)   REFERENCES reviews(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_user   FOREIGN KEY (reporter_id) REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT uq_review_reporter UNIQUE (review_id, reporter_id)
);

-- 7. 검색어 로그 (keyword + ip_address → IP 기반 중복 방지로 순위 조작 차단)
CREATE TABLE IF NOT EXISTS search_logs (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    keyword    VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. 1:1 문의 (answer NULL 여부로 답변 상태 판단)
CREATE TABLE IF NOT EXISTS inquiries (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    category    VARCHAR(50)  NOT NULL,
    title       VARCHAR(300) NOT NULL,
    content     TEXT NOT NULL,
    answer      TEXT,
    answered_at TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inquiry_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- updated_at 트리거 (users, reviews)
DROP TRIGGER IF EXISTS trg_users_updated_at ON users;
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_reviews_updated_at ON reviews;
CREATE TRIGGER trg_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- 장르 기본 데이터
INSERT INTO genres (name) VALUES
    ('판타지'),
    ('로맨스'),
    ('무협'),
    ('현대'),
    ('SF'),
    ('공포'),
    ('추리'),
    ('역사')
ON CONFLICT (name) DO NOTHING;
