package pos.controller;

import pos.util.DBConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class SalesService {

    public static void viewSalesByDate() {
        Scanner sc = new Scanner(System.in);
        System.out.print("매출을 조회할 날짜를 입력하세요 (예: 2025-07-06): ");
        String date = sc.nextLine();

        String sql = "SELECT SUM(sales) AS total_sales FROM POSSales WHERE money_date = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int totalSales = rs.getInt("total_sales");
                System.out.printf("%s 날짜의 총 매출: %,d원\n", date, totalSales);
            } else {
                System.out.println("해당 날짜의 매출 정보가 없습니다.");
            }

        } catch (SQLException e) {
            System.out.println("매출 조회 중 오류 발생");
            e.printStackTrace();
        }
    }
}