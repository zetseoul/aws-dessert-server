# 🍰 Dessert Server

- 목표: Express + MySQL → Spring Boot + JPA + PostgreSQL 이전
- 기존 React API 경로·응답 형식 호환 유지, 일부 입력 오류는 HTTP 400으로 정리
- 환경: Java 21 · Spring Boot 4.1.2 · Gradle · Neon PostgreSQL
- Spring 포트: 8088 / React 로컬 포트: 3000

## 완료한 단계 — 2026.11.21

- [x] Spring Boot·Neon 연결, 엔티티 4종 매핑
- [x] Preference의 누락된 Entity 선언 및 CHAR(1) 매핑 수정
- [x] 레시피 목록·상세·검색 API
- [x] 상세 조리 단계 번호순 반환, 없는 레시피 404·요청 ID 로그
- [x] 작성자 닉네임·소개 조회
- [x] 일반 회원가입: 닉네임 직접 입력·중복 검사·Validation·BCrypt
- [x] 일반 로그인·로그아웃·세션 조회
- [x] 본인 회원정보 조회: 비로그인 401·타인 접근 403
- [x] 설문 제출·재제출·나중에 하기·완료 여부 조회
- [x] React 연결: 서버 주소·CORS·쿠키·닉네임 입력 및 표시·오류 처리
- [x] JUnit 56개 통과: H2 저장·조회 및 MockMvc HTTP/세션/CORS 검증

## 실제 화면·Neon 확인 결과

- [x] 레시피 목록·상세·검색 및 작성자 닉네임 표시
- [x] 일반 회원가입 성공 후 설문 화면 이동
- [x] 로그인/비로그인 세션 응답, 타인 정보 조회 차단
- [x] 설문 제출 성공 및 본인 정보 API의 답변 조회
- [ ] 브라우저에서 일반 로그인→마이페이지→로그아웃 전체 흐름 최종 확인
- 마이페이지 설문 답변 표시 UI는 미구현, API 응답으로 저장 확인

## API

| 메서드 | 경로 | 기능 |
|---|---|---|
| GET | `/api/recipe` | 목록 |
| GET | `/api/recipe/detail/{id}` | 상세·조리 순서 |
| GET | `/api/recipe/search?keyword=검색어` | 이름 부분 일치 검색 |
| GET | `/api/users/username?author=사용자ID` | 작성자 닉네임·소개 |
| POST | `/api/users/signup/register` | 일반 가입 |
| POST | `/api/users/login/register` | 일반 로그인 |
| POST | `/api/users/logout` | 로그아웃 |
| GET | `/api/users/session` | 로그인 상태 |
| GET | `/api/users/{id}` | 본인 회원정보·설문 답변 |
| POST | `/api/users/research/res/ok` | 설문 제출·갱신 |
| POST | `/api/users/research/res/no` | 설문 건너뛰기 |

## 닉네임·설문 정책

- 일반 가입: 닉네임 직접 입력, 2~20자·중복 불가
- 카카오 신규 가입: 수식어 + 디저트명 자동 생성 예정, 기존 사용자 닉네임 유지
- 가입 직후 관심 분야 `모두`: 임시 초기값, 설문 완료를 뜻하지 않음
- preference 행 없음 또는 is_research=false: 미완료
- 설문 제출: 답변·is_research=true·사용자 관심 분야를 같은 트랜잭션으로 저장
- 복수 답변: 쉼표로 저장, API에서는 배열로 반환
- 재제출: 사용자당 한 행 갱신 / 나중에 하기: 기존 답변 삭제 안 함

## 실행·테스트

- IntelliJ의 DessertServerApplication 환경변수: DB_URL · DB_USER · DB_PWD
- 서버 CLIENT_ORIGIN: 기본 `http://localhost:3000`, 배포 시 실제 프론트 주소 지정
- React `.env.local`: `REACT_APP_SERVER_URL=http://localhost:8088`
- Spring 실행 + React `npm start`, 환경변수 변경 시 해당 앱 재실행
- React 설정에 DB 접속 정보·비밀번호를 넣지 않음
- JUnit: 테스트 클래스 왼쪽 ▶ → Run
- 결과 확인: **초록색 체크·통과 개수 → 연결 DB 주소 → 실패 시 오류 로그**
- 최신 검증: Repository·Service·Controller·CORS 테스트 56개 통과
- H2 데이터는 Neon과 별개, 테스트 후 롤백 / 기존 contextLoads 테스트는 이번 검증에서 제외
- ddl-auto는 validate 유지, 기존 DB 스키마 자동 변경 금지
- 현재 세션은 서버 메모리 저장: 서버 재시작 시 로그인 해제

## 남은 마이그레이션 범위

- [ ] 카카오 로그인·로그아웃·기본 닉네임 자동 생성
- [ ] 배포 시 세션 저장 방식 결정: Redis 사용 여부·쿠키·CORS 점검
- [ ] 기존 완성 기능의 React 전체 흐름 최종 검증
- [ ] Cloudtype 배포 설정 및 배포 환경 동작 확인

## 별도 후속 개발

- 기존에 미완성이었던 레시피 등록·수정·삭제·이미지 업로드는 이번 완료 범위에서 제외
- S3 사용 불가 전제, 이미지 저장소는 추후 결정
- 마이페이지 설문 답변 표시 UI

<!--
다음 시작 지점
- 일반 인증 전체 흐름 확인 후 카카오 로그인 이전
- 기존 완성 기능을 기준으로 마이그레이션 완료 판단, 신규 기능과 구분
-->
