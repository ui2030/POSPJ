package pos.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DBConnect {
    private static String driver;
    private static String url;
    private static String username;
    private static String password;

    static {
        try (InputStream input = DBConnect.class.getClassLoader().getResourceAsStream("properties")) {
            Properties prop = new Properties();
            prop.load(input);

            driver = prop.getProperty("db.driver");
            url = prop.getProperty("db.url");
            username = prop.getProperty("db.username");
            password = prop.getProperty("db.password");

            Class.forName(driver);
        } catch (IOException e) {
            System.out.println("DB 설정 파일 로딩 실패");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC 드라이버 로딩 실패");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            try {
                if (r != null) r.close();
            } catch (Exception e) {
                System.out.println("자원 해제 오류");
            }
        }
    }
}
