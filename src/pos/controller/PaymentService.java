package pos.controller;

import pos.util.DBConnect;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.InputMismatchException;
import java.util.Scanner;

public class PaymentService {

    public static void processPayment(String loginMember, int logId) {
        Scanner sc = new Scanner(System.in);

        System.out.println("일반 제품(1) 또는 19금 제품(2)을 구매하시겠습니까?");
        String choice = sc.nextLine();

        boolean isAdult = choice.equals("2");
        String selectSql = isAdult ?
                "SELECT * FROM Product19" :
                "SELECT * FROM Product";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(selectSql);
             ResultSet rs = preparedStatement.executeQuery()) {

            System.out.println("\n구매 가능한 제품 목록:");
            int index = 1;
            int[] ids = new int[100];

            while (rs.next()) {
                int id = rs.getInt(isAdult ? "product_id19" : "product_id");
                String name = rs.getString(isAdult ? "product_name19" : "product_name");
                int count = rs.getInt(isAdult ? "product_count19" : "product_count");
                String date = rs.getString(isAdult ? "best_before19" : "best_before");
                String time = rs.getString(isAdult ? "best_beforetime19" : "best_beforetime");
                int price = rs.getInt(isAdult ? "price19" : "price");

                System.out.printf("%d. %s - %d원 (%d개 재고)", index, name, price, count);

                String nowDate = LocalDate.now().toString();
                if (nowDate.compareTo(date) > 0) {
                    System.out.print(" 유통기한 지남!");
                }
                System.out.println();

                ids[index] = id;
                index++;
            }

            if (index == 1) {
                System.out.println("제품이 없습니다.");
                return;
            }

            int selected = -1;
            while (true) {
                try {
                    System.out.print("구매할 제품 번호 선택: ");
                    selected = Integer.parseInt(sc.nextLine());
                    if (selected <= 0 || selected >= index) {
                        System.out.println("유효하지 않은 번호입니다. 다시 입력해주세요.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("숫자로 입력해주세요.");
                }
            }

            int selectedId = ids[selected];

            System.out.print("몇 개를 구매하시겠습니까?: ");
            int buyCount = Integer.parseInt(sc.nextLine());
            if (buyCount <= 0) {
                System.out.println("구매 수량은 1개 이상이어야 합니다.");
                return;
            }

            if (isAdult) {
                System.out.print("주민등록번호를 입력하세요 (예: 010101-3000000): ");
                String ssn = sc.nextLine();
                if (ssn.length() < 14 || ssn.charAt(6) != '-') {
                    System.out.println("잘못된 주민등록번호 형식입니다.");
                    return;
                }

                String birthPrefix = ssn.substring(0, 2);
                int birthYear = Integer.parseInt(birthPrefix);
                int fullYear = (birthYear >= 0 && birthYear <= LocalDate.now().getYear() % 100)
                        ? 2000 + birthYear : 1900 + birthYear;

                int age = LocalDate.now().getYear() - fullYear;
                if (age < 19) {
                    System.out.println("미성년자는 구매할 수 없습니다.");
                    return;
                }
            }

            System.out.print("결제 방법을 선택하세요 (1. 카드 2. 현금): ");
            String method = sc.nextLine();

            String getPriceSql = isAdult ?
                    "SELECT price19, product_count19, best_before19 FROM Product19 WHERE product_id19 = ?" :
                    "SELECT price, product_count, best_before FROM Product WHERE product_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(getPriceSql)) {
                ps.setInt(1, selectedId);
                ResultSet priceRs = ps.executeQuery();

                if (priceRs.next()) {
                    int price = priceRs.getInt(1);
                    int count = priceRs.getInt(2);
                    String expireDate = priceRs.getString(3);

                    if (count < buyCount) {
                        System.out.println("재고가 부족합니다.");
                        return;
                    }

                    if (LocalDate.now().toString().compareTo(expireDate) > 0) {
                        System.out.println("해당 제품은 유통기한이 지나 구매할 수 없습니다.");
                        return;
                    }

                    int totalPrice = price * buyCount;
                    int balance = getCurrentBalance(conn);

                    if (method.equals("1")) {
                        balance -= totalPrice;
                        System.out.println("카드 결제 완료. " + totalPrice + "원 차감");
                    } else if (method.equals("2")) {
                        System.out.print("현금 입력: ");
                        int cash = Integer.parseInt(sc.nextLine());
                        if (cash < totalPrice) {
                            System.out.println("금액이 부족합니다.");
                            return;
                        }
                        int change = cash - totalPrice;
                        balance += totalPrice;
                        System.out.println("현금 결제 완료. 거스름돈: " + change + "원");
                    } else {
                        System.out.println("올바른 결제 방법이 아닙니다.");
                        return;
                    }

                    String insertSales = """
                        INSERT INTO POSSales(pos_id, balance, sales, money_date, money_time,
                                             product_id, product_id19, log_id, login_member)
                        VALUES (possales_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                    try (PreparedStatement is = conn.prepareStatement(insertSales)) {
                        is.setInt(1, balance);
                        is.setInt(2, totalPrice);
                        is.setString(3, LocalDate.now().toString());
                        is.setString(4, LocalTime.now().withNano(0).toString());
                        if (isAdult) {
                            is.setNull(5, Types.INTEGER);
                            is.setInt(6, selectedId);
                        } else {
                            is.setInt(5, selectedId);
                            is.setNull(6, Types.INTEGER);
                        }
                        is.setInt(7, logId);
                        is.setString(8, loginMember);
                        is.executeUpdate();
                    }

                    String updateSql = isAdult ?
                            "UPDATE Product19 SET product_count19 = product_count19 - ? WHERE product_id19 = ?" :
                            "UPDATE Product SET product_count = product_count - ? WHERE product_id = ?";
                    try (PreparedStatement ups = conn.prepareStatement(updateSql)) {
                        ups.setInt(1, buyCount);
                        ups.setInt(2, selectedId);
                        ups.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("결제 처리 중 오류 발생");
            e.printStackTrace();
        }
    }

    private static int getCurrentBalance(Connection conn) throws SQLException {
        String sql = "SELECT balance FROM POSSales ORDER BY pos_id DESC FETCH FIRST 1 ROWS ONLY";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("balance");
            }
        }
        return 1234000;
    }
}