# 소설 리뷰 서비스 - Backend

소설에 대한 리뷰를 작성하고 공유하는 플랫폼의 백엔드 서버입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Security | Spring Security 6 + OAuth2 Client |
| ORM | MyBatis |
| Database | MariaDB |
| Auth | JWT (JJWT 0.12.6) + HttpOnly Cookie |
| Build | Gradle |
| Deploy | Railway + Docker |

## 주요 기능

- **소셜 로그인**: Google / Naver / Kakao OAuth2 소셜 로그인
- **JWT 인증**: HttpOnly 쿠키 기반 JWT 토큰 (XSS 방어)
- **소설 관리**: 소설 등록/수정/삭제, 장르 필터, 키워드 검색, 정렬(최신/별점/리뷰순)
- **리뷰 관리**: 리뷰 CRUD, 별점, 좋아요 토글, 리뷰 신고 (중복 신고 방지)
- **랭킹**: 별점 / 리뷰수 TOP 10 — 기간(전체/일간/주간/월간) + 장르 필터 지원
- **인기 검색어**: 검색어 로깅 + 최근 7일 TOP 5 (IP 기반 중복 방지로 순위 조작 차단)
- **1:1 문의**: 유저 문의 작성 → 관리자 답변
- **회원 탈퇴**: 탈퇴 시 리뷰는 "탈퇴된 회원"으로 유지, 소셜 연동 해제
- **관리자**: 회원 추방(리뷰 전체 삭제), 장르 관리, 리뷰 신고 처리, 문의 답변/삭제

## 인증 흐름

```
소셜 로그인 요청
  → Spring OAuth2 → 소셜 서버 인증
  → CustomOAuth2UserService (DB 저장 or 조회)
  → OAuth2SuccessHandler → JWT 생성 → HttpOnly 쿠키 발급
  → 프론트엔드 홈으로 리다이렉트

매 API 요청
  → JwtAuthenticationFilter → 쿠키에서 JWT 추출 → 검증
  → SecurityContext에 사용자 정보 저장
  → 컨트롤러에서 인증 정보 사용
```

## 보안 설계

- **XSS 방어**: JWT를 HttpOnly 쿠키에 저장 (JS에서 접근 불가)
- **CORS**: 허용된 출처만 접근 (환경변수 `FRONTEND_URL`로 관리)
- **SameSite=None + Secure**: 크로스 오리진 배포 환경(Vercel ↔ Railway) 대응
- **환경변수 분리**: OAuth2 키, JWT 시크릿 등 민감 정보는 환경변수로 관리

## API 엔드포인트

### 공개 API (인증 불필요)
```
GET  /api/genres
GET  /api/novels?page=0&size=10&genreId=&keyword=&sortBy=latest
GET  /api/novels/{id}
GET  /api/novels/rankings?type=rating&period=weekly&genreId=   # 랭킹 (기간/장르 필터)
GET  /api/novels/{novelId}/reviews
GET  /api/auth/me
POST /api/search-logs                  # 검색어 기록 (IP 기반 1시간 중복 방지)
GET  /api/search-logs/popular          # 인기 검색어 TOP 5 (최근 7일)
```

### 로그인 필요
```
POST   /api/auth/logout
POST   /api/novels/{novelId}/reviews
PUT    /api/reviews/{reviewId}
DELETE /api/reviews/{reviewId}
POST   /api/reviews/{reviewId}/likes   # 좋아요 토글
POST   /api/reviews/{reviewId}/reports # 리뷰 신고 (중복 시 409)
GET    /api/users/me/reviews
POST   /api/inquiries                  # 1:1 문의 작성
GET    /api/users/me/inquiries         # 내 문의 목록
DELETE /api/inquiries/{id}             # 본인 문의 삭제 (답변 전만)
DELETE /api/users/me                   # 회원 탈퇴
```

### 관리자 전용
```
GET    /api/admin/users
DELETE /api/admin/users/{id}           # 회원 추방
POST   /api/admin/genres
PUT    /api/admin/genres/{id}
DELETE /api/admin/genres/{id}
GET    /api/admin/review-reports       # 리뷰 신고 목록
DELETE /api/admin/review-reports/{id}  # 신고 거절
DELETE /api/admin/review-reports/{id}/delete-review/{reviewId}  # 신고 처리 (리뷰 삭제)
GET    /api/admin/inquiries            # 문의 목록
PUT    /api/admin/inquiries/{id}/answer  # 답변 등록/수정
DELETE /api/admin/inquiries/{id}       # 문의 삭제
```

### OAuth2
```
GET /oauth2/authorization/google
GET /oauth2/authorization/naver
GET /oauth2/authorization/kakao
```

## DB 테이블 구조

```
users           - 회원 정보 (소셜 provider, access_token 포함)
novels          - 소설 정보 (장르 FK, 평균 별점, 리뷰 수)
genres          - 장르 목록
reviews         - 리뷰 (user_id NULL 허용 - 탈퇴 회원 리뷰 유지)
review_likes    - 리뷰 좋아요 (review_id + user_id UNIQUE)
review_reports  - 리뷰 신고 (review_id + reporter_id UNIQUE - 중복 신고 방지)
search_logs     - 검색어 로그 (keyword + ip_address - 조작 방지)
inquiries       - 1:1 문의 (answer NULL 여부로 답변 상태 판단)
```

## 로컬 실행 방법

### 1. 환경변수 설정
`src/main/resources/application-secret.properties` 파일 생성:
```properties
JWT_SECRET=your-jwt-secret-32-chars-minimum
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret
KAKAO_ADMIN_KEY=your-kakao-admin-key
```

### 2. DB 설정
```sql
CREATE DATABASE study_db CHARACTER SET utf8mb4;
CREATE USER 'study_user'@'localhost' IDENTIFIED BY 'mariadb';
GRANT ALL ON study_db.* TO 'study_user'@'localhost';
```

### 3. 실행
```bash
./gradlew bootRun
# 또는 IntelliJ에서 BackendNovelReviewApplication 실행
```
서버: `http://localhost:8080`

## 배포

- **플랫폼**: Railway
- **DB**: Railway MariaDB (Docker 이미지)
- **컨테이너**: Dockerfile 기반 빌드 (eclipse-temurin:17)
- **환경변수**: Railway Variables에서 관리

```dockerfile
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod 755 gradlew && ./gradlew clean bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```
