package cn.itcast.nio.io_model;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static cn.itcast.nio.bytebuffer.ByteBufferUtil.debugAll;

/**
 * 使用异步IO读取文件
 */
@Slf4j
public class AioFileChannel {

    public static void main(String[] args) throws IOException {
        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("data.txt"), StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(16);
            log.debug("read begin...");
            // 参数1 ByteBuffer
            // 参数2 读取的起始位置
            // 参数3 附件(一次读不完需要ByteBuffer接着读)
            // 参数4 回调对象CompletionHandler(调用此对象方法的线程是另一个线程)
            channel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override // read 成功
                public void completed(Integer result, ByteBuffer attachment) {
                    log.debug("read completed...{}", result);
                    attachment.flip();
                    debugAll(attachment);
                }

                @Override // read 失败
                public void failed(Throwable exc, ByteBuffer attachment) {
                    log.debug("read failed...");
                    exc.printStackTrace();
                }
            });
            log.debug("read end...");

            System.in.read(); // 阻塞在此
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
