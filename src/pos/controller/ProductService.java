package pos.controller;

import pos.util.DBConnect;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class ProductService {

    // 1. 제품 등록
    public static void insertProduct(String loginMember, int logId) {
        Scanner sc = new Scanner(System.in);

        System.out.println("제품명을 입력하세요:");
        String name = sc.nextLine();

        System.out.println("제조회사를 입력하세요:");
        String manufacturer = sc.nextLine();

        System.out.println("유통기한 날짜를 입력하세요 (예: 2025-12-31):");
        String bestBefore = sc.nextLine();

        System.out.println("유통기한 시간을 입력하세요 (예: 12:00:00):");
        String bestBeforeTime = sc.nextLine();

        System.out.println("가격을 입력하세요:");
        int price = Integer.parseInt(sc.nextLine());

        System.out.println("개수를 입력하세요:");
        int count = Integer.parseInt(sc.nextLine());

        System.out.println("19금 제품인가요? (Y/N):");
        String isAdult = sc.nextLine();

        boolean is19 = isAdult.equalsIgnoreCase("Y");

        String sql = is19
                ? """
                    INSERT INTO Product19(product_id19, product_name19, product_count19, manu_facturer19,
                        best_before19, best_beforetime19, price19, log_id, login_member)
                    VALUES (product_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)
                  """
                : """
                    INSERT INTO Product(product_id, product_name, product_count, manu_facturer,
                        best_before, best_beforetime, price, log_id, login_member)
                    VALUES (product_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)
                  """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, count);
            pstmt.setString(3, manufacturer);
            pstmt.setString(4, bestBefore);
            pstmt.setString(5, bestBeforeTime);
            pstmt.setInt(6, price);
            pstmt.setInt(7, logId);
            pstmt.setString(8, loginMember);

            int row = pstmt.executeUpdate();
            System.out.println(row + "개 제품이 등록되었습니다.");

        } catch (SQLException e) {
            System.out.println("제품 등록 중 오류 발생");
            e.printStackTrace();
        }
    }

    // 2. 제품 목록 확인
    public static void showProducts() {
        try (Connection conn = DBConnect.getConnection()) {

            System.out.println("\n일반 제품 목록:");
            String sql = "SELECT product_name, product_count FROM Product";

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("product_name");
                    int count = rs.getInt("product_count");
                    String stars = "*".repeat(Math.max(0, count));
                    System.out.println(name + " : " + stars + " (" + count + "개)");
                }
            }

            System.out.println("\n19금 제품 목록:");
            String sql19 = "SELECT product_name19, product_count19 FROM Product19";

            try (PreparedStatement pstmt = conn.prepareStatement(sql19);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("product_name19");
                    int count = rs.getInt("product_count19");
                    String stars = "*".repeat(Math.max(0, count));
                    System.out.println(name + " : " + stars + " (" + count + "개)");
                }
            }

        } catch (SQLException e) {
            System.out.println("제품 목록 조회 중 오류");
            e.printStackTrace();
        }
    }

    // 3. 물품 입고
    public static void receiveStock() {
        Random rand = new Random();

        try (Connection conn = DBConnect.getConnection()) {
            boolean isAdult = rand.nextBoolean();

            String selectSql = isAdult
                    ? "SELECT product_id19, product_name19, product_count19 FROM Product19 ORDER BY DBMS_RANDOM.VALUE"
                    : "SELECT product_id, product_name, product_count FROM Product ORDER BY DBMS_RANDOM.VALUE";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                 ResultSet rs = selectStmt.executeQuery()) {

                if (rs.next()) {
                    int productId = rs.getInt(1);
                    String name = rs.getString(2);
                    int currentCount = rs.getInt(3);

                    int addedCount = rand.nextInt(10) + 1; // 1~10개 입고
                    int newCount = currentCount + addedCount;

                    String updateSql = isAdult
                            ? "UPDATE Product19 SET product_count19 = ? WHERE product_id19 = ?"
                            : "UPDATE Product SET product_count = ? WHERE product_id = ?";

                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, newCount);
                        updateStmt.setInt(2, productId);
                        int row = updateStmt.executeUpdate();

                        if (row > 0) {
                            System.out.printf("[%s] %d개 입고 완료 (총 재고: %d개)\n", name, addedCount, newCount);
                        } else {
                            System.out.println("입고 실패: 제품 ID 업데이트 실패");
                        }
                    }
                } else {
                    System.out.println("입고할 제품이 없습니다.");
                }
            }

        } catch (SQLException e) {
            System.out.println("입고 처리 중 오류 발생");
            e.printStackTrace();
        }
    }

    // 4. 제품 검색
    public static void searchProduct() {
        Scanner sc = new Scanner(System.in);
        System.out.print("검색할 제품명을 입력하세요: ");
        String keyword = sc.nextLine();

        String likeKeyword = "%'" + keyword + "'%";

        try (Connection conn = DBConnect.getConnection()) {
            System.out.println("\n일반 제품 검색 결과:");
            String sql = "SELECT product_name, product_count FROM Product WHERE product_name LIKE ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, "%" + keyword + "%");
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String name = rs.getString("product_name");
                    int count = rs.getInt("product_count");
                    System.out.printf("- %s (%d개)\n", name, count);
                }
            }

            System.out.println("\n19금 제품 검색 결과:");
            String sql19 = "SELECT product_name19, product_count19 FROM Product19 WHERE product_name19 LIKE ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql19)) {
                pstmt.setString(1, "%" + keyword + "%");
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String name = rs.getString("product_name19");
                    int count = rs.getInt("product_count19");
                    System.out.printf("- %s (%d개)\n", name, count);
                }
            }

        } catch (SQLException e) {
            System.out.println("제품 검색 중 오류 발생");
            e.printStackTrace();
        }
    }
}