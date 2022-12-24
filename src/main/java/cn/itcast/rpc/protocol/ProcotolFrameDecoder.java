package cn.itcast.rpc.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * <h2>封装一层帧解码器用于处理粘包、半包</h2>
 */
public class ProcotolFrameDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * 自己提供一个无参构造方法进行封装
     */
    public ProcotolFrameDecoder() {
        this(1024, 12, 4, 0, 0);
    }

    public ProcotolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

}
