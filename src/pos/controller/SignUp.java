package pos.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;
import java.util.Scanner;

import pos.util.DBConnect;
import pos.util.EmailSender;

public class SignUp {

    public static void registerUser() {
        Scanner sc = new Scanner(System.in);

        System.out.println("[회원가입을 진행합니다]");
        System.out.print("이름: ");
        String name = sc.nextLine();

        System.out.print("아이디: ");
        String id = sc.nextLine();

        System.out.print("비밀번호: ");
        String pw = sc.nextLine();

        System.out.print("이메일 주소(Gmail): ");
        String email = sc.nextLine();

        // 인증 코드 생성
        String code = generateCode();

        // 이메일 발송
        boolean result = EmailSender.sendCode(email, code);
        if (!result) {
            System.out.println("이메일 발송 실패로 회원가입을 중단합니다.");
            return;
        }

        System.out.print("전송된 인증 코드를 입력하세요: ");
        String userInputCode = sc.nextLine();
        if (!userInputCode.equals(code)) {
            System.out.println("인증 실패. 회원가입을 중단합니다.");
            return;
        }

        // 랜덤 사원코드 생성
        String loginMember = generateMemberCode();
        String todayDate = LocalDate.now().toString();
        String todayTime = LocalTime.now().withNano(0).toString();

        try (Connection conn = DBConnect.getConnection()) {
            String sql = "INSERT INTO POSUser (login_member, login_id, login_password, login_name, todays_date, todays_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, loginMember);
                pstmt.setString(2, id);
                pstmt.setString(3, pw);
                pstmt.setString(4, name);
                pstmt.setString(5, todayDate);
                pstmt.setString(6, todayTime);
                pstmt.executeUpdate();
                System.out.println("회원 가입이 완료되었습니다. 로그인 창으로 돌아갑니다.");
            }
        } catch (SQLException e) {
            System.out.println("회원 정보 저장 중 오류 발생");
            e.printStackTrace();
        }
    }

    private static String generateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static String generateMemberCode() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            sb.append(letters.charAt(rand.nextInt(letters.length())));
        }
        return sb.toString();
    }
}