package cn.itcast.nio.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 拷贝多级目录
 */
public class TestFilesCopy {

    public static void main(String[] args) throws IOException {

        long start = System.currentTimeMillis();

        String source = "/home/jms/sdk/jdk/jdk8u352-full/";
        String target = "/home/jms/tmp/jdk8u352-full/";

        Files.walk(Paths.get(source)).forEach(path -> {
            try {
                String targetName = path.toString().replace(source, target);

                // 是目录
                if (Files.isDirectory(path) && !Files.exists(path)) {
                    Files.createDirectory(Paths.get(targetName));
                }
                // 是普通文件
                else if (Files.isRegularFile(path)) {
                    Files.copy(path, Paths.get(targetName));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        long end = System.currentTimeMillis();

        System.out.printf("耗时：%s秒\n", (end - start) / 1000);
    }

}
