package cn.itcast.nio.files;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 两个FileChannel之间传输数据
 * <p></p>
 * FileChannel#transferTo方法演示
 */
public class TestFileChannelTransferTo {

    public static void main(String[] args) {
        // from只可以读数据
        // to只可以写数据
        try (FileChannel from = new FileInputStream("data.txt").getChannel();
             FileChannel to = new FileOutputStream("to.txt").getChannel()) {

            long size = from.size();
            // left变量代表还剩余多少字节
            for (long left = size; left > 0; ) {
                System.out.println("position:" + (size - left) + ", left:" + left);
                // 效率高，底层会利用操作系统的零拷贝进行优化
                // 方法名中带“transfer”的方法一般底层使用了“零拷贝”技术
                // 坑: 每次最多传输2g数据! (采用多次传输的方法可以解决上述问题)
                left -= from.transferTo((size - left), left, to);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
