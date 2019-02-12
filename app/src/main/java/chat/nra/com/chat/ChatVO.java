package chat.nra.com.chat;

public class ChatVO {

    private String userName;
    private String message;
    private String time;

    public ChatVO(){
    }
    public ChatVO(String userName,String message, String time){
        this.userName = userName;
        this.message = message;
        this.time = time;
    }
    public String getMessage() {
        return message;
    }
    public String getUserName() {
        return userName;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
}
