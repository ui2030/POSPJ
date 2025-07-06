package pos.model;

public class POSUser {
    private String loginMember;
    private String loginId;
    private String loginPassword;
    private String loginName;
    private String todaysDate;
    private String todaysTime;

    // 생성자
    public POSUser(String loginMember, String loginId, String loginPassword,
                   String loginName, String todaysDate, String todaysTime) {
        this.loginMember = loginMember;
        this.loginId = loginId;
        this.loginPassword = loginPassword;
        this.loginName = loginName;
        this.todaysDate = todaysDate;
        this.todaysTime = todaysTime;
    }

    // 필요한 Getter 추가
    public String getLoginMember() {
        return loginMember;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public String getTodaysDate() {
        return todaysDate;
    }

    public String getTodaysTime() {
        return todaysTime;
    }
}
