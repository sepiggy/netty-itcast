package cn.itcast.im.message;

public class PongMessage extends Message {

    @Override
    public int getMessageType() {
        return PongMessage;
    }

}
