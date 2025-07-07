package pos.controller;

import pos.util.DBConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class SalesService {

    // 1. 날짜별 매출 조회
    public static void querySalesByDate() {
        Scanner sc = new Scanner(System.in);
        System.out.print("조회할 날짜를 입력하세요 (예: 2025-07-07): ");
        String date = sc.nextLine();

        String sql = "SELECT SUM(sales) AS total_sales FROM POSSales WHERE money_date = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total_sales");
                System.out.printf("%s 날짜의 총 매출은 %,d원입니다.%n", date, total);
            } else {
                System.out.println("해당 날짜의 매출 정보가 없습니다.");
            }

        } catch (SQLException e) {
            System.out.println("매출 조회 중 오류 발생");
            e.printStackTrace();
        }
    }

    // 2. 일당 차감
    public static boolean deductWage(String loginMember, int wage) {
        String sql = "UPDATE POSSales SET balance = balance - ? WHERE pos_id = (SELECT MAX(pos_id) FROM POSSales WHERE login_member = ?)";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, wage);
            pstmt.setString(2, loginMember);

            int row = pstmt.executeUpdate();
            return row > 0;

        } catch (SQLException e) {
            System.out.println("일당 차감 중 오류 발생");
            e.printStackTrace();
            return false;
        }
    }
}