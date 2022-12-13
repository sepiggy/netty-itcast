package cn.itcast.netty_basic.c3;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 演示 JDK 中的 future
 * ATTN 执行任务的线程和获取结果的线程不是同一个线程
 * future 可以看作在线程间传递结果的容器
 * 11:01:12 [DEBUG] [main] c.i.n.c.TestJdkFuture - 等待结果
 * 11:01:12 [DEBUG] [pool-1-thread-1] c.i.n.c.TestJdkFuture - 执行计算
 * 11:01:15 [DEBUG] [main] c.i.n.c.TestJdkFuture - 结果是 50
 */
@Slf4j
public class TestJdkFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 线程池
        ExecutorService service = Executors.newFixedThreadPool(2);
        // 2. 提交任务
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(3000);
                return 50;
            }
        });
        // 3. 主线程通过 future 来获取结果
        log.debug("等待结果");
        log.debug("结果是 {}", future.get()); // 阻塞
    }
}
