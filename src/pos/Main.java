package pos;

import pos.controller.SignUp;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("===== POS 시스템 =====");
            System.out.println("1. 로그인");
            System.out.println("2. 회원가입");
            System.out.println("3. 종료");
            System.out.print("선택: ");
            String input = sc.nextLine();

            switch (input) {
                case "1":
                    // 로그인 구현 예정
                    break;
                case "2":
                    SignUp.registerUser();
                    break;
                case "3":
                    System.out.println("시스템 종료");
                    running = false;
                    break;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
        sc.close();
    }
}
