package pos.controller;

import pos.model.POSUser;
import pos.util.DBConnect;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class LoginService {

    // 로그인 처리 및 로그인 로그 기록
    public static POSUser login() {
        Scanner sc = new Scanner(System.in);

        System.out.print("아이디: ");
        String inputId = sc.nextLine();

        System.out.print("비밀번호: ");
        String inputPw = sc.nextLine();

        String sql = """
            SELECT * FROM POSUser WHERE login_id = ? AND login_password = ?
        """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, inputId);
            pstmt.setString(2, inputPw);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String loginMember = rs.getString("login_member");
                String loginName = rs.getString("login_name");

                System.out.println(loginName + " 사원님 맞으신가요? (Y/N)");
                String confirm = sc.nextLine();

                if (!confirm.equals("Y") && !confirm.equals("y")) {
                    System.out.println("로그인을 취소했습니다.");
                    return null;
                }

                insertLoginLog(conn, loginMember, loginName);

                System.out.println("로그인 성공!");
                return new POSUser(
                        loginMember,
                        inputId,
                        inputPw,
                        loginName,
                        LocalDate.now().toString(),
                        LocalTime.now().withNano(0).toString()
                );
            } else {
                System.out.println("아이디 또는 비밀번호가 틀렸습니다.");
                return null;
            }

        } catch (SQLException e) {
            System.out.println("로그인 중 오류 발생");
            e.printStackTrace();
            return null;
        }
    }

    // 로그인 로그 테이블에 insert
    private static void insertLoginLog(Connection conn, String loginMember, String loginName) {
        String sql = """
            INSERT INTO Loginlog(log_id, ml_date, ml_time, ml_name, login_member)
            VALUES(LOG_SEQ.NEXTVAL, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setString(2, LocalTime.now().withNano(0).toString());
            pstmt.setString(3, loginName);
            pstmt.setString(4, loginMember);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("로그 기록 중 오류 발생");
            e.printStackTrace();
        }
    }

    // 가장 최근 log_id 가져오기
    public static int getLatestLogId(String loginMember) {
        String sql = """
            SELECT MAX(log_id) AS max_log_id FROM Loginlog WHERE login_member = ?
        """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loginMember);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("max_log_id");
            } else {
                System.out.println("로그인 기록이 없습니다.");
                return -1;
            }

        } catch (SQLException e) {
            System.out.println("log_id 조회 중 오류");
            e.printStackTrace();
            return -1;
        }
    }
}