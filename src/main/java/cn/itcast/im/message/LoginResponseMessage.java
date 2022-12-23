package cn.itcast.im.message;

import lombok.Data;
import lombok.ToString;

/**
 * 登录响应消息
 */
@Data
@ToString(callSuper = true)
public class LoginResponseMessage extends AbstractResponseMessage {

    public LoginResponseMessage(boolean success, String reason) {
        super(success, reason);
    }

    @Override
    public int getMessageType() {
        return LoginResponseMessage;
    }

}
