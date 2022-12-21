package cn.itcast.netty_basic.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;

/**
 * <h2>ByteBuf的创建与扩容</h2>
 * <pre>
 * 1) 可以通过"ByteBufAllocator.DEFAULT.buffer()"方法创建ByteBuf，可以指定初始化容量，默认是256B
 * 2) ByteBuf的容量支持动态扩缩容
 * </pre>
 */
public class TestByteBuf_0 {

    public static void main(String[] args) {

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        ByteBufUtil.log(buf);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append("a");
        }
        // 向ByteBuf中写入300B数据，ByteBuf会扩容
        buf.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8));

        ByteBufUtil.log(buf);
    }

}
