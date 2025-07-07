package pos.controller;

import pos.model.POSUser;
import pos.util.DBConnect;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;

public class LoginService {

    public static POSUser login() {
        Scanner sc = new Scanner(System.in);

        System.out.print("아이디: ");
        String inputId = sc.nextLine();

        System.out.print("비밀번호: ");
        String inputPw = sc.nextLine();

        String sql = "SELECT * FROM POSUser WHERE login_id = ? AND login_password = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, inputId);
            pstmt.setString(2, inputPw);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String loginMember = rs.getString("login_member");
                String loginName = rs.getString("login_name");

                System.out.printf("%s 사원님 맞으신가요? (Y/N): ", loginName);
                String confirm = sc.nextLine();
                if (!confirm.equalsIgnoreCase("Y")) {
                    System.out.println("로그인 취소되었습니다.");
                    return null;
                }

                // 로그인 로그 기록
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
                System.out.println("아이디 또는 비밀번호가 틀리셨습니다. 회원등록을 하시겠습니까? (1.회원가입 0.뒤로가기)");
                String choice = sc.nextLine();
                if (choice.equals("1")) {
                    SignUp.registerUser();
                }
            }
        } catch (SQLException e) {
            System.out.println("로그인 중 오류 발생");
            e.printStackTrace();
        }
        return null;
    }

    public static void insertLoginLog(Connection conn, String loginMember, String loginName) {
        String insertLog = "INSERT INTO Loginlog(log_id, ml_date, ml_time, ml_name, login_member) " +
                "VALUES (loginlog_seq.NEXTVAL, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertLog)) {
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

    public static int getLastLogId(String loginMember) {
        String sql = "SELECT MAX(log_id) AS log_id FROM Loginlog WHERE login_member = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loginMember);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("log_id");
            }
        } catch (SQLException e) {
            System.out.println("최근 로그인 ID 조회 중 오류 발생");
            e.printStackTrace();
        }
        return -1;
    }
}