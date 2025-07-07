package pos.controller;

import pos.util.DBConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class SalesService {

    // 날짜별 매출 조회
    public static void querySalesByDate() {
        try (Connection conn = DBConnect.getConnection()) {
            String sql = "SELECT money_date, SUM(sales) AS total_sales FROM POSSales GROUP BY money_date ORDER BY money_date";

            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                System.out.println("\n===== 날짜별 매출 정보 =====");
                while (rs.next()) {
                    String date = rs.getString("money_date");
                    int total = rs.getInt("total_sales");
                    System.out.printf("%s : %,d원\n", date, total);
                }
            }
        } catch (SQLException e) {
            System.out.println("매출 조회 중 오류 발생");
            e.printStackTrace();
        }
    }

    // 근무 시간 후 시급 계산 및 POS 잔고 차감
    public static boolean deductWage(String loginMember, int wage) {
        try (Connection conn = DBConnect.getConnection()) {
            int balance = getCurrentBalance(conn);
            int newBalance = balance - wage;

            String sql = "INSERT INTO POSSales(pos_id, balance, sales, money_date, money_time, product_id, product_id19, log_id, login_member)\n" +
                    "VALUES (possales_seq.NEXTVAL, ?, ?, ?, ?, NULL, NULL, NULL, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, newBalance);
                pstmt.setInt(2, -wage); // sales에 음수로 저장 (지출)
                pstmt.setString(3, LocalDate.now().toString());
                pstmt.setString(4, LocalTime.now().withNano(0).toString());
                pstmt.setString(5, loginMember);

                int row = pstmt.executeUpdate();
                return row > 0;
            }
        } catch (SQLException e) {
            System.out.println("POS 잔고 차감 처리 중 오류 발생");
            e.printStackTrace();
        }
        return false;
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