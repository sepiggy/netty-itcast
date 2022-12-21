package cn.itcast.netty_basic.future_promise;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * <h2>演示JDK中的Future</h2>
 * <pre>
 * JDK中的Future只能以"同步"的方式获取结果:
 * 调用Future#get方法以同步的方式获取结果
 * 注意：在同步的方式中，线程自己去主动获取结果
 * 注意：获取结果的线程是main线程，而执行任务的线程是线程池中的线程
 * 注意：Future可以看作在线程间传递结果的容器
 * <p></p>
 * 运行结果：
 * 11:01:12 [DEBUG] [main] c.i.n.c.TestJdkFuture - 等待结果
 * 11:01:12 [DEBUG] [pool-1-thread-1] c.i.n.c.TestJdkFuture - 执行计算
 * 11:01:15 [DEBUG] [main] c.i.n.c.TestJdkFuture - 结果是50
 * </pre>
 */
@Slf4j
public class TestJdkFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1. 线程池
        ExecutorService service = Executors.newFixedThreadPool(2);
        // 2. 提交任务
        // future可以看作是main线程和线程池中的线程通信的桥梁
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(3000); // 模拟计算花费的时间
                return 50;
            }
        });
        // 3. 主线程通过future#get来获取结果
        log.debug("等待结果");
        log.debug("结果是{}", future.get()); // 阻塞在此，main线程去主动获取结果
    }

}
