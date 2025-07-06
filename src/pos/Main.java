package pos;

import pos.controller.LoginService;
import pos.controller.PaymentService;
import pos.controller.ProductService;
import pos.controller.SalesService;
import pos.controller.SignUp;
import pos.model.POSUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Scanner;

import pos.util.DBConnect;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        POSUser loggedInUser = null;
        int logId = -1;
        LocalDateTime loginTime = null;

        while (running) {
            System.out.println("===== POS 시스템 =====");
            System.out.println("1. 로그인");
            System.out.println("2. 회원가입");
            System.out.println("3. 종료");
            System.out.print("선택: ");
            String input = sc.nextLine();

            switch (input) {
                case "1":
                    loggedInUser = LoginService.login();
                    if (loggedInUser != null) {
                        logId = LoginService.getLatestLogId(loggedInUser.getLoginMember());
                        loginTime = LocalDateTime.now();

                        boolean menuRunning = true;
                        while (menuRunning) {
                            System.out.println("\n========== 메인 메뉴 ==========");
                            System.out.println("사원: " + loggedInUser.getLoginName() + " 안녕하세요.");
                            System.out.println("1. 제품 등록");
                            System.out.println("2. 제품 확인");
                            System.out.println("3. 물품 입고");
                            System.out.println("4. 제품 검색");
                            System.out.println("5. 계산 메뉴");
                            System.out.println("6. 날짜별 매출 조회");
                            System.out.println("0. 로그아웃");
                            System.out.print("메뉴 선택: ");
                            String menu = sc.nextLine();

                            switch (menu) {
                                case "1":
                                    ProductService.insertProduct(loggedInUser.getLoginMember(), logId);
                                    break;
                                case "2":
                                    ProductService.showProducts();
                                    break;
                                case "3":
                                    ProductService.receiveStock();
                                    break;
                                case "4":
                                    ProductService.searchProduct();
                                    break;
                                case "5":
                                    PaymentService.processPayment(loggedInUser.getLoginMember(), logId);
                                    break;
                                case "6":
                                    SalesService.viewSalesByDate();
                                    break;
                                case "0":
                                    System.out.println("로그아웃 되었습니다.");
                                    loggedInUser = null;
                                    logId = -1;
                                    menuRunning = false;
                                    break;
                                default:
                                    System.out.println("잘못된 입력입니다.");
                            }
                        }

                        if (loginTime != null) {
                            LocalDateTime logoutTime = LocalDateTime.now();
                            Duration duration = Duration.between(loginTime, logoutTime);
                            long minutes = duration.toMinutes();
                            int wage = 11000; // 시급
                            long earned = minutes * (wage / 60);
                            System.out.println("\n근무 시간: " + minutes + "분");
                            System.out.println("오늘 일당: " + earned + "원");

                            // POS 잔고 차감 및 기록 저장
                            try (Connection conn = DBConnect.getConnection()) {
                                // 가장 최근 잔고 확인
                                int currentBalance = 1234000;
                                String balanceSql = "SELECT balance FROM POSSales ORDER BY pos_id DESC FETCH FIRST 1 ROWS ONLY";
                                try (PreparedStatement bs = conn.prepareStatement(balanceSql);
                                     ResultSet rs = bs.executeQuery()) {
                                    if (rs.next()) {
                                        currentBalance = rs.getInt("balance");
                                    }
                                }

                                int newBalance = (int)(currentBalance - earned);
                                String insertSql = """
                                    INSERT INTO POSSales(pos_id, balance, sales, money_date, money_time,
                                                         product_id, product_id19, log_id, login_member)
                                    VALUES (possales_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)
                                """;
                                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                                    pstmt.setInt(1, newBalance);
                                    pstmt.setInt(2, (int)(-earned)); // sales는 음수로 기록
                                    pstmt.setString(3, LocalDate.now().toString());
                                    pstmt.setString(4, LocalTime.now().withNano(0).toString());
                                    pstmt.setNull(5, Types.INTEGER); // product_id
                                    pstmt.setNull(6, Types.INTEGER); // product_id19
                                    pstmt.setInt(7, logId);
                                    pstmt.setString(8, loggedInUser.getLoginMember());
                                    pstmt.executeUpdate();
                                }
                                System.out.println("POS 잔고에서 일당 차감 완료.");
                            } catch (Exception e) {
                                System.out.println("POS 잔고 차감 중 오류 발생");
                                e.printStackTrace();
                            }
                        }
                        System.out.println("사원: KarL 빠이.");
                    }
                    break;

                case "2":
                    SignUp.registerUser();
                    break;

                case "3":
                    System.out.println("시스템 종료");
                    running = false;
                    break;

                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
        sc.close();
    }
}