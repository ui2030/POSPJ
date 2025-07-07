package pos;

import pos.controller.*;
import pos.model.POSUser;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        POSUser loggedInUser = null;
        LocalDateTime loginTime = null;
        int logId = -1;

        while (true) {
            System.out.println("===== POS 시스템 =====");
            System.out.println("환영합니다. 도와드릴 업무를 선택해주세요.");
            System.out.println("1.로그인, 2.회원가입, 3.종료");
            System.out.print("선택: ");
            String input = sc.nextLine();

            switch (input) {
                case "1" -> {
                    loggedInUser = LoginService.login();
                    if (loggedInUser != null) {
                        loginTime = LocalDateTime.now();
                        logId = LoginService.getLastLogId(loggedInUser.getLoginMember());
                        runMainMenu(sc, loggedInUser, loginTime, logId);
                    }
                }
                case "2" -> SignUp.registerUser();
                case "3" -> {
                    System.out.println("프로그램을 종료합니다.");
                    return;
                }
                default -> System.out.println("잘못된 입력입니다.");
            }
        }
    }

    private static void runMainMenu(Scanner sc, POSUser loggedInUser, LocalDateTime loginTime, int logId) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n========== 메인 메뉴 ==========");
            System.out.printf("사원: %s 안녕하세요.%n", loggedInUser.getLoginName());
            System.out.println("1.제품 등록, 2.제품 확인, 3.물품 입고, 4.제품 검색, 5.계산 메뉴, 6.날짜별 매출 조회, 0.로그아웃");
            System.out.print("메뉴 선택: ");
            String menu = sc.nextLine();

            switch (menu) {
                case "1" -> ProductService.insertProduct(loggedInUser.getLoginMember(), logId);
                case "2" -> ProductService.showProducts();
                case "3" -> ProductService.receiveStock();
                case "4" -> ProductService.searchProduct();
                case "5" -> PaymentService.processPayment(loggedInUser.getLoginMember(), logId);
                case "6" -> SalesService.querySalesByDate();
                case "0" -> {
                    if (loginTime != null) {
                        Duration workDuration = Duration.between(loginTime, LocalDateTime.now());
                        long minutes = workDuration.toMinutes();
                        int wage = (int) (minutes * 11000 / 60.0);
                        System.out.printf("근무 시간: %d분%n", minutes);
                        System.out.printf("오늘 일당: %,d원%n", wage);
                        boolean success = SalesService.deductWage(loggedInUser.getLoginMember(), wage);
                        if (!success) {
                            System.out.println("POS 잔고 차감 중 오류 발생");
                        }
                    }
                    System.out.printf("사원: %s 빠이.%n", loggedInUser.getLoginName());
                    loggedIn = false;
                }
                default -> System.out.println("잘못된 메뉴입니다.");
            }
        }
    }
}