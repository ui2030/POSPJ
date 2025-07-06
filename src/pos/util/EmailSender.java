package pos.util;

import com.sendgrid.*;
import java.io.IOException;

public class EmailSender {

    public static boolean sendCode(String toEmail, String code) {
        String apiKey = PropertiesLoader.get("sendgrid.api.key");
        String fromEmail = PropertiesLoader.get("sendgrid.sender.email");

        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        String subject = "POS 시스템 인증 코드입니다.";
        Content content = new Content("text/plain", "인증 코드: " + code);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("응답 코드: " + response.getStatusCode());
            return response.getStatusCode() == 202;

        } catch (IOException e) {
            System.out.println("이메일 전송 중 오류 발생");
            e.printStackTrace();
            return false;
        }
    }
}