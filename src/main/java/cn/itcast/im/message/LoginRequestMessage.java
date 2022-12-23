package cn.itcast.im.message;

import lombok.Data;
import lombok.ToString;

/**
 * 登录请求消息
 */
@Data
@ToString(callSuper = true)
public class LoginRequestMessage extends Message {

    private String username;
    private String password;

    public LoginRequestMessage() {
    }

    public LoginRequestMessage(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public int getMessageType() {
        return LoginRequestMessage;
    }

}
