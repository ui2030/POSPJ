package pos.controller;

import com.sendgrid.*;
import pos.util.DBConnect;
import pos.util.EmailSender;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;
import java.util.Scanner;

public class SignUp {

    public static void registerUser() {
        Scanner sc = new Scanner(System.in);

        System.out.print("이름: ");
        String name = sc.nextLine();

        System.out.print("아이디: ");
        String id = sc.nextLine();

        System.out.print("비밀번호: ");
        String pw = sc.nextLine();

        System.out.print("구글 이메일 입력 (인증용): ");
        String email = sc.nextLine();

        // 인증 코드 생성
        String code = generateCode();

        // 이메일 전송
        boolean sent = sendCodeToEmail(email, code);
        if (!sent) {
            System.out.println("이메일 전송 실패. 회원가입 중단.");
            return;
        }

        System.out.print("이메일로 전송된 코드 입력: ");
        String inputCode = sc.nextLine();
        if (!inputCode.equals(code)) {
            System.out.println("인증 실패. 코드 불일치.");
            return;
        }

        // 랜덤 회원 식별코드 생성
        String memberCode = generateMemberCode();

        String date = LocalDate.now().toString();
        String time = LocalTime.now().withNano(0).toString();

        String sql = """
            INSERT INTO POSUser(login_member, login_id, login_password, login_name, todays_date, todays_time)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, memberCode);
            pstmt.setString(2, id);
            pstmt.setString(3, pw);
            pstmt.setString(4, name);
            pstmt.setString(5, date);
            pstmt.setString(6, time);

            int row = pstmt.executeUpdate();
            System.out.printf("회원가입 성공! 등록된 사용자 수: %d명\n", row);

        } catch (SQLException e) {
            System.out.println("회원가입 중 오류 발생");
            e.printStackTrace();
        }
    }

    // 인증 코드 생성 (영문 + 숫자 혼합 5자리)
    private static String generateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    // 회원 고유 식별자 생성 (영문 5자리)
    private static String generateMemberCode() {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    // SendGrid로 이메일 전송
    private static boolean sendCodeToEmail(String toEmail, String code) {
        String apiKey = PropertiesLoader.get("sendgrid.api.key");
        String fromEmail = PropertiesLoader.get("sendgrid.sender.email");

        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        String subject = "POS 시스템 인증 코드";
        Content content = new Content("text/plain", "인증 코드: " + code);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            return response.getStatusCode() == 202;

        } catch (IOException e) {
            System.out.println("이메일 전송 중 오류 발생");
            e.printStackTrace();
            return false;
        }
    }
}