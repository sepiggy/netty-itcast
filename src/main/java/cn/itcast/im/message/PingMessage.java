package cn.itcast.im.message;

/**
 * <h2>心跳消息</h2>
 */
public class PingMessage extends Message {

    @Override
    public int getMessageType() {
        return PingMessage;
    }

}
