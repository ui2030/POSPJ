package pos.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailSender {

    public static boolean sendCode(String toEmail, String code) {
        Properties prop = new Properties();

        // properties 파일 로딩
        try (InputStream input = EmailSender.class.getClassLoader().getResourceAsStream("properties")) {
            if (input == null) {
                System.out.println("properties 파일을 찾을 수 없습니다.");
                return false;
            }
            prop.load(input);
        } catch (IOException e) {
            System.out.println("설정 파일 로딩 중 오류 발생");
            e.printStackTrace();
            return false;
        }

        // 설정 로딩
        String username = prop.getProperty("aws.ses.username");
        String password = prop.getProperty("aws.ses.password");
        String host = prop.getProperty("aws.ses.host");
        String port = prop.getProperty("aws.ses.port");
        String fromEmail = prop.getProperty("aws.ses.sender.email");

        // SMTP 속성 설정
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // 세션 생성
        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("POS 시스템 인증 코드입니다.");
            message.setText("인증 코드: " + code);

            Transport.send(message);
            System.out.println("이메일 전송 완료");
            return true;

        } catch (MessagingException e) {
            System.out.println("이메일 전송 실패");
            e.printStackTrace();
            return false;
        }
    }
}