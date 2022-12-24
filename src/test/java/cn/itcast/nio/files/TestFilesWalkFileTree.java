package cn.itcast.nio.files;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用Files#walkFileTree方法遍历目录
 */
public class TestFilesWalkFileTree {

    public static void main(String[] args) throws IOException {
//        m1();
//        m2();
        m3();
    }

    /**
     * 删除多级目录
     *
     * @throws IOException
     */
    private static void m3() throws IOException {

        // 直接删除会抛出 DirectoryNotEmptyException 异常
//        Files.delete(Paths.get("/home/jms/tmp/jdk8u352-full/"));

        // 若文件夹不为空，需要使用walkFileTree方法删除目录
        Files.walkFileTree(Paths.get("/home/jms/tmp/jdk8u352-full/"), new SimpleFileVisitor<Path>() {
            // 先删除文件
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                Files.delete(filePath);
                return super.visitFile(filePath, attrs);
            }

            // 最后删除文件夹: 只有文件夹为空才可以对其进行删除操作
            @Override
            public FileVisitResult postVisitDirectory(Path dirpath, IOException exc) throws IOException {
                Files.delete(dirpath);
                return super.postVisitDirectory(dirpath, exc);
            }
        });
    }

    /**
     * 统计jdk目录下有多少.jar文件
     *
     * @throws IOException
     */
    private static void m2() throws IOException {

        AtomicInteger jarCount = new AtomicInteger();

        Files.walkFileTree(Paths.get("/home/jms/sdk/jdk/jdk8u352-full"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                if (filePath.toString().endsWith(".jar")) {
                    System.out.println(filePath);
                    jarCount.incrementAndGet();
                }
                return super.visitFile(filePath, attrs);
            }
        });
        System.out.println("jar count: " + jarCount);
    }

    /**
     * 遍历文件夹并统计文件夹和文件的数量
     * <p></p>
     * 相关设计模式：访问者模式
     *
     * @throws IOException
     */
    private static void m1() throws IOException {

        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();

        Files.walkFileTree(Paths.get("/home/jms/sdk/jdk/jdk8u352-full"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attrs) throws IOException {
                System.out.println("====>" + dirPath);
                dirCount.incrementAndGet();
                // 调用父类的对应方法
                return super.preVisitDirectory(dirPath, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                System.out.println(filePath);
                fileCount.incrementAndGet();
                // 调用父类的对应方法
                return super.visitFile(filePath, attrs);
            }
        });

        System.out.println("dir count: " + dirCount);
        System.out.println("file count: " + fileCount);
    }

}
