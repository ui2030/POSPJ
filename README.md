POSPJ – 콘솔 기반 POS 시스템 (Java)

요구사항에 맞춘 편의점/소매점 POS(Point of Sale) 콘솔 애플리케이션입니다.
직원 로그인·근무시간 기록, 상품 등록/입고/재고, 성인(19+) 상품 판별, 유통기한 체크, 카드/현금 결제, 영수 처리(매출 집계)까지 매장 운영 전 과정을 시뮬레이션합니다.

🔧 시연 영상·포트폴리오 링크 :

[프론트 & DB 시연](https://youtu.be/4ozVGK_rUEI)
[회원가입 생성 시연](https://youtu.be/_yvdUsxaZK4)

핵심 기능

직원/근무

로그인·로그아웃, 근무 시작/종료 기록

근무 시간 합산 및 시급 기준 일급 정산

상품/재고

상품 등록(제조사/유통기한/가격/성인용 여부) 및 키워드 검색

입고 처리(수량 증가), 재고 조회

유통기한 만료 상품 자동 차단(결제 불가)

결제/정산

장바구니 담기 → 현금/카드 결제, 거스름돈 계산

POS 잔고(현금/카드) 증감 반영

성인용 상품 구매 시 만 19세 이상 자동 판별

매출 조회

일자/기간별 매출 합계

기술 스택

Language: Java (권장 JDK 17+)

DB: JDBC 호환 DB (Oracle/MySQL 등 선택)

IDE: IntelliJ IDEA (POSPJ.iml 포함)

저장소 언어 통계는 Java가 100%입니다. 
GitHub

폴더 구조
```css
POSPJ/
├─ .idea/
├─ src/
│  └─ pos/
├─ .gitignore
├─ POSPJ.iml
└─ README.md
```

저장소 루트에 src/pos, .idea, .gitignore, POSPJ.iml, README.md가 있습니다. 
GitHub

빠른 실행
1) 사전 준비

JDK 17+ 설치

(선택) Oracle/MySQL 등 DB 준비 및 JDBC 드라이버 추가

2) 환경 변수/설정 (예시)

src/pos/util/DBConnect.java 또는 설정 파일에 아래와 같이 입력합니다.
(클래스/경로명은 실제 코드에 맞게 수정)

DB_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1
DB_USER=pos
DB_PASSWORD=pos1234

WAGE_PER_HOUR=11000
BLOCK_EXPIRED_ITEMS=true
ADULT_CHECK_ENABLED=true

3) 빌드 & 실행

IntelliJ: Main 클래스 실행

CLI(예시):

# 프로젝트 루트에서
javac -encoding UTF-8 -d out $(git ls-files "src/**/*.java")
java -cp out pos.Main   # 🔧 실제 메인 클래스 경로로 교체

DB 스키마 예시

실제 테이블은 프로젝트 SQL 스크립트/DAO 구현과 맞추어 적용하세요.

CREATE TABLE products (
  product_id     BIGINT PRIMARY KEY,
  name           VARCHAR(100) NOT NULL,
  manufacturer   VARCHAR(100),
  price          INT NOT NULL,
  is_adult_only  CHAR(1) DEFAULT 'N',   -- 'Y' or 'N'
  expire_date    DATE,
  stock_qty      INT DEFAULT 0
);

CREATE TABLE sales (
  sale_id      BIGINT PRIMARY KEY,
  sale_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  total_amount INT NOT NULL
);

CREATE TABLE sale_items (
  sale_item_id BIGINT PRIMARY KEY,
  sale_id      BIGINT REFERENCES sales(sale_id),
  product_id   BIGINT REFERENCES products(product_id),
  qty          INT NOT NULL,
  line_amount  INT NOT NULL
);

CREATE TABLE employees (
  emp_id     BIGINT PRIMARY KEY,
  login_id   VARCHAR(50) UNIQUE NOT NULL,
  name       VARCHAR(50) NOT NULL,
  password   VARCHAR(200) NOT NULL
);

CREATE TABLE work_logs (
  work_id     BIGINT PRIMARY KEY,
  emp_id      BIGINT REFERENCES employees(emp_id),
  start_time  TIMESTAMP,
  end_time    TIMESTAMP,
  worked_sec  INT
);

CREATE TABLE pos_balance (
  id          INT PRIMARY KEY,
  cash_amount INT DEFAULT 0,
  card_amount INT DEFAULT 0
);

사용 흐름(예)

직원 로그인 → 근무 시작

상품 등록/검색, 입고 → 재고 확인

장바구니 → 결제(현금/카드) → 잔고 반영

매출 조회(일/기간)

근무 종료 → 근무시간 × 시급 = 일급 정산

성인 판별 로직(예)
boolean isAdult(LocalDate birth, LocalDate today) {
    return Period.between(birth, today).getYears() >= 19;
}

로드맵

 단위/통합 테스트(JUnit)

 매출 통계(베스트/시간대)

 영수증 출력(파일/프린터)

 CSV/Excel 입출력

 (선택) 간단한 관리자 메뉴/권한

기여

이슈·PR 환영합니다. 버그/개선 제안은 Issues에 등록해 주세요.

라이선스 & 연락처

🔧 라이선스: MIT 등 원하는 라이선스를 LICENSE로 추가

🔧 Maintainer: 이름 / 이메일

저장소: https://github.com/ui2030/POSPJ
