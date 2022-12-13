package cn.itcast.netty_basic.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * 通过 Promise 对象，我们可以自主设置执行结果，更加灵活
 * Promise 对象可以看作一个存放执行结果的容器
 * Promise 同 Netty 中的 Future， 既可以同步接收结果，也可以异步接收结果
 */
@Slf4j
public class TestNettyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 1. 准备 EventLoop 对象
        EventLoop eventLoop = new NioEventLoopGroup().next();

        // 2. 可以主动创建 promise, 结果容器
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);
        new Thread(() -> {
            // 3. 任意一个线程执行计算，计算完毕后向 promise 填充结果
            log.debug("开始计算...");
            try {
                int i = 1 / 0;
                Thread.sleep(3000);
                promise.setSuccess(80); // 主动设置成功结果
            } catch (Exception e) {
                e.printStackTrace();
                promise.setFailure(e); // 主动设置失败结果
            }
        }).start();

        // 4. 接收结果的线程
        log.debug("等待结果...");
        log.debug("结果是: {}", promise.get()); // 阻塞
    }

}
