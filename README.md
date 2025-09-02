# POSPJ – 콘솔 기반 POS 시스템 (Java)
요구사항에 맞춘 편의점/소매점 POS(Point of Sale) 콘솔 애플리케이션입니다.
직원 로그인·근무시간 기록, 상품 등록/입고/재고, 성인(19+) 상품 판별, 유통기한 체크, 카드/현금 결제, 영수 처리(매출 집계)까지 매장 운영 전 과정을 시뮬레이션합니다.

🔧 시연 영상·포트폴리오 링크 :

[프론트 & DB 시연](https://youtu.be/4ozVGK_rUEI)

[회원가입 생성 시연](https://youtu.be/_yvdUsxaZK4)

## 핵심 기능

### 직원/근무

**ㆍ**로그인·로그아웃, 근무 시작/종료 기록

**ㆍ**근무 시간 합산 및 시급 기준 일급 정산

### 상품/재고

**ㆍ**상품 등록(제조사/유통기한/가격/성인용 여부) 및 키워드 검색

**ㆍ**입고 처리(수량 증가), 재고 조회

**ㆍ**유통기한 만료 상품 자동 차단(결제 불가)

### 결제/정산

**ㆍ**장바구니 담기 → 현금/카드 결제, 거스름돈 계산

**ㆍ**POS 잔고(현금/카드) 증감 반영

**ㆍ**성인용 상품 구매 시 만 19세 이상 자동 판별

### 매출 조회

**ㆍ**일자/기간별 매출 합계

## 기술 스택

Language: Java (권장 JDK 17+)

DB: JDBC 호환 DB (Oracle/MySQL 등 선택)

IDE: IntelliJ IDEA (POSPJ.iml 포함)

## 폴더 구조
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

## 빠른 실행
### 1) 사전 준비

**ㆍ**JDK 17+ 설치

**ㆍ**Oracle/MySQL 등 DB 준비 및 JDBC 드라이버 추가


### 2) 환경 변수/설정 (예시)

src/pos/util/DBConnect.java 또는 설정 파일에 아래와 같이 입력합니다.
(클래스/경로명은 실제 코드에 맞게 수정)
```prperties
DB_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1
DB_USER=pos
DB_PASSWORD=pos1234

WAGE_PER_HOUR=11000
BLOCK_EXPIRED_ITEMS=true
ADULT_CHECK_ENABLED=true
```
## 3) 빌드 & 실행

**ㆍ**IntelliJ: Main 클래스 실행

**ㆍ**CLI(예시):
```
# 프로젝트 루트에서
javac -encoding UTF-8 -d out $(git ls-files "src/**/*.java")
java -cp out pos.Main   # 🔧 실제 메인 클래스 경로로 교체
```
## DB 스키마 예시

실제 테이블은 프로젝트 SQL 스크립트/DAO 구현과 맞추어 적용하세요.
```
CREATE TABLE POSUser (
--사원식별코드, 아이디, 비밀번호, 이름, 생성날짜, 시간
login_member   VARCHAR2(10) NOT NULL,
login_id       VARCHAR2(10) NOT NULL,
login_password VARCHAR2(30) NOT NULL,
login_name     VARCHAR2(20) NOT NULL,
todays_date    VARCHAR2(10) NOT NULL,
todays_time     VARCHAR2(8) NOT NULL,

PRIMARY KEY (login_member)
);

CREATE TABLE Loginlog (
--로그식별번호, 로그인 날짜, 시간, 이름, 회원식별코드
log_id         NUMBER(10) NOT NULL,
ml_date      VARCHAR2(10) NOT NULL,
ml_time       VARCHAR2(8) NOT NULL,
ml_name      VARCHAR2(20) NOT NULL,
login_member VARCHAR2(10) NOT NULL,

PRIMARY KEY (log_id, login_member),
FOREIGN KEY (login_member) REFERENCES POSUser(login_member)
);

CREATE TABLE Product (
--제품식별번호, 제품명, 개수, 제조회사, 유통기한날짜, 유통기한시간, 가격, (로그, 사원 식별번호)
product_id       NUMBER(10) NOT NULL,
product_name   VARCHAR2(40) NOT NULL,
product_count    NUMBER(10),
manu_facturer  VARCHAR2(40) NOT NULL,
best_before    VARCHAR2(10) NOT NULL,
best_beforetime VARCHAR2(8) NOT NULL,
price            NUMBER(20) NOT NULL,
log_id           NUMBER(20) NOT NULL,
login_member   VARCHAR2(10) NOT NULL,

PRIMARY KEY (product_id),
FOREIGN KEY (log_id, login_member) REFERENCES Loginlog(log_id, login_member)
);

CREATE TABLE Product19 (
--제품식별번호, 제품명, 개수, 제조회사, 유통기한, 시간, 가격, (로그, 사원 식별번호)
product_id19        NUMBER(10) NOT NULL,
product_name19    VARCHAR2(40) NOT NULL,
product_count19     NUMBER(10),
manu_facturer19   VARCHAR2(40) NOT NULL,
best_before19     VARCHAR2(10) NOT NULL,
best_beforetime19  VARCHAR2(8) NOT NULL,
price19             NUMBER(20) NOT NULL,
log_id              NUMBER(20) NOT NULL,
login_member      VARCHAR2(10) NOT NULL,

PRIMARY KEY (product_id19),
FOREIGN KEY (log_id, login_member) REFERENCES Loginlog(log_id, login_member)
);

CREATE TABLE POSSales (
--POS기식별번호, 잔액, 매출, 날짜, 시간, (제품, 로그, 사원 식별번호)
pos_id         NUMBER(10) NOT NULL,
balance        NUMBER(20) DEFAULT 1234000,
sales          NUMBER(20) DEFAULT 0,
money_date     VARCHAR2(10) NOT NULL,
money_time     VARCHAR2(8) NOT NULL,
product_id     NUMBER(10),
product_id19   NUMBER(10),
log_id         NUMBER(10),
login_member   VARCHAR2(10) NOT NULL,

PRIMARY KEY (pos_id, log_id, login_member),
FOREIGN KEY (log_id, login_member) REFERENCES Loginlog(log_id, login_member),
FOREIGN KEY (product_id) REFERENCES Product(product_id),
FOREIGN KEY (product_id19) REFERENCES Product19(product_id19)
);
```

## 사용 흐름(예)

**1.** 직원 로그인 → 근무 시작

**2.** 상품 등록/검색, 입고 → 재고 확인

**3.** 장바구니 → 결제(현금/카드) → 잔고 반영

**4.** 매출 조회(일/기간)

**5.** 근무 종료 → 근무시간 × 시급 = 일급 정산

## 성인 판별 로직(예)
```
boolean isAdult(LocalDate birth, LocalDate today) {
    return Period.between(birth, today).getYears() >= 19;
}
```
## 로드맵

 **ㆍ**단위/통합 테스트(JUnit)

 **ㆍ**매출 통계(베스트/시간대)

 **ㆍ**영수증 출력(파일/프린터)

 **ㆍ**CSV/Excel 입출력

 **ㆍ**(선택) 간단한 관리자 메뉴/권한

## 기여

이슈·PR 환영합니다. 버그/개선 제안은 Issues에 등록해 주세요.

## 라이선스 & 연락처

**ㆍ**🔧 라이선스: MIT 등 원하는 라이선스를 LICENSE로 추가

**ㆍ**🔧 Maintainer: Cheonui Kim / kimcjsdml@gmail.com

**ㆍ**저장소: (https://github.com/ui2030/POSPJ)
