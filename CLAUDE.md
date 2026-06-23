# 소설 리뷰 서비스 - 백엔드

## 기술 스택
- Java 17, Spring Boot 4.0.6
- Spring Security 6 + OAuth2 Client
- MyBatis + MariaDB
- JWT (JJWT 0.12.6)
- Lombok, Gradle

## 프로젝트 구조
도메인별 패키지 안에 `controller → service → mapper → dto` 4계층을 두는 구조.
- **controller**: HTTP 요청/응답, 인증 정보(userId·role) 추출 후 service 호출
- **service**: 비즈니스 로직, 트랜잭션(`@Transactional`), 존재·권한 검증(`ResponseStatusException`)
- **mapper**: MyBatis 매퍼 인터페이스 (`@Mapper`, `resources/mapper/*.xml`와 매핑)
- **dto**: 계층 간 데이터 운반 객체 (기존 domain 폴더 대체)

```
src/main/java/com/example/backend_novel_review/
├── config/
│   ├── SecurityConfig.java       # Security 설정 (OAuth2, JWT 필터, 권한)
│   └── CorsConfig.java           # CORS 설정 (localhost:5173 허용)
├── auth/
│   ├── controller/AuthController.java        # GET /api/auth/me, POST /api/auth/logout
│   ├── service/
│   │   ├── CustomOAuth2UserService.java      # 네이버/카카오 OAuth2 처리
│   │   ├── CustomOidcUserService.java        # 구글 OIDC 처리
│   │   ├── JwtService.java                  # JWT 생성/검증
│   │   ├── OAuthAttributes.java             # 소셜별 속성 매핑
│   │   └── SocialUnlinkService.java         # 소셜 연동 해제 (탈퇴/추방 시)
│   ├── handler/
│   │   ├── OAuth2SuccessHandler.java         # 로그인 성공 → JWT 쿠키 발급 → 프론트 리다이렉트
│   │   └── OAuth2FailureHandler.java
│   ├── filter/JwtAuthenticationFilter.java  # 매 요청 JWT 검증
│   ├── dto/UserPrincipal.java               # SecurityContext 사용자 객체
│   └── util/CookieUtil.java                 # HttpOnly 쿠키 생성/삭제
├── user/        controller / service / mapper(UserMapper) / dto(User)
├── novel/       controller / service / mapper(NovelMapper) / dto(Novel, NovelRequest)
├── genre/       controller / service / mapper(GenreMapper) / dto(Genre)
├── review/      controller / service / mapper(ReviewMapper, ReviewLikeMapper, ReviewReportMapper)
│                                      / dto(Review, ReviewLike, ReviewReport, ReviewRequest)
├── inquiry/     controller / service / mapper(InquiryMapper) / dto(Inquiry)
└── search/      controller / service / mapper(SearchLogMapper) / dto(SearchLog)
```

## MyBatis 매퍼 위치
```
src/main/resources/mapper/
├── UserMapper.xml
├── NovelMapper.xml
├── GenreMapper.xml
├── ReviewMapper.xml
├── ReviewLikeMapper.xml
├── ReviewReportMapper.xml
├── InquiryMapper.xml
└── SearchLogMapper.xml
```

## DB 정보
- DB: MariaDB, 포트: 3306
- DB명: study_db
- 유저: study_user / mariadb
- 테이블: users, novels, genres, reviews, review_likes

## DB 특이사항
- reviews.user_id: NULL 허용 + ON DELETE SET NULL (자진 탈퇴 시 리뷰 유지)
- users.social_access_token: 로그인 시 소셜 access_token 저장 (연동 해제용)
- novels.cover_image_url: TEXT 타입 (긴 URL 허용)
- review_likes: review_id + user_id UNIQUE, 둘 다 ON DELETE CASCADE

## API 엔드포인트
### 공개 (인증 불필요)
- GET /api/genres
- GET /api/novels?page=0&size=12&genreId=&keyword=&sortBy=latest  # sortBy: latest(기본)/rating/reviews
- GET /api/novels/{id}
- GET /api/novels/{novelId}/reviews
- GET /api/auth/me (비로그인 시 401)

### 로그인 필요
- POST /api/auth/logout
- POST /api/novels/{novelId}/reviews
- PUT /api/reviews/{reviewId}
- DELETE /api/reviews/{reviewId}
- POST /api/reviews/{reviewId}/likes  # 좋아요 토글 (liked, likeCount 반환)
- GET /api/users/me/reviews
- DELETE /api/users/me         # 자진 탈퇴 (리뷰 유지, 소셜 연동 해제)

### ADMIN 전용 (/api/admin/**)
- GET /api/admin/users
- POST/PUT/DELETE /api/admin/genres
- DELETE /api/admin/users/{id}  # 회원 추방 (리뷰 삭제, 소셜 연동 해제)

### OAuth2
- GET /oauth2/authorization/{google|naver|kakao}

## 인증 흐름
1. 소셜 로그인 → CustomOAuth2UserService/CustomOidcUserService → DB 저장 + social_access_token 저장
2. OAuth2SuccessHandler → JWT 생성 → HttpOnly 쿠키 → 프론트 홈으로 리다이렉트
3. 매 요청: JwtAuthenticationFilter → 쿠키에서 JWT 추출 → SecurityContext에 Claims 저장
4. 컨트롤러에서 유저 ID 추출: `Claims claims = (Claims) authentication.getPrincipal(); Long userId = Long.valueOf(claims.getSubject());`

## 회원 탈퇴 vs 관리자 추방 차이
| 구분 | 리뷰 처리 | 소셜 연동 해제 |
|------|-----------|---------------|
| 자진 탈퇴 (DELETE /api/users/me) | 유지 → "탈퇴된 회원" 표시 | O |
| 관리자 추방 (DELETE /api/admin/users/{id}) | 전체 삭제 | O |

## 소셜별 이메일 처리
- 구글: email 필드 사용
- 네이버: email 없으면 `naver_{providerId}@naver.com` 임시 생성
- 카카오: email 없으면 `kakao_{providerId}@kakao.com` 임시 생성

## 소셜 연동 해제 방식
- 카카오: Admin Key 사용 (application.properties의 kakao.admin-key)
- 구글: social_access_token으로 revoke
- 네이버: social_access_token으로 delete
- 실패해도 탈퇴/추방은 계속 진행 (예외 삼킴)

## 중요 설정
- application.properties는 .gitignore에 포함 (시크릿 보호)
- application.properties.example 참고해서 생성
- JWT 쿠키: HttpOnly, SameSite=Lax, secure=false (개발환경)
- JWT payload에 userId(subject), email, nickname, role 저장

## 앱 실행
- IntelliJ에서 BackendNovelReviewApplication 실행
- 포트: 8080
- 재시작 후 반드시 로그아웃 → 재로그인 필요 (JWT 무효화)
