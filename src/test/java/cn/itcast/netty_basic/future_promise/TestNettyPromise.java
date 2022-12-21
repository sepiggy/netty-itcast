package cn.itcast.netty_basic.future_promise;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * <h2>演示Netty中的Promise</h2>
 * <pre>
 * 1) 通过Promise对象，我们可以自主设置执行结果，更加灵活
 * 2) Netty中的 {@link io.netty.util.concurrent.Promise} 继承自Netty中的 {@link io.netty.util.concurrent.Future} 继承自JDK中的 {@link java.util.concurrent.Future}
 * 3) Promise对象不同于Future对象，可以自己new出来
 * 4) Promise对象可以看作一个存放执行结果的容器
 * 5) Promise同Netty中的Future，既可以同步接收结果，也可以异步接收结果
 * </pre>
 */
@Slf4j
public class TestNettyPromise {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 1. 准备EventLoop对象
        EventLoop eventLoop = new NioEventLoopGroup().next();

        // 2. 可以主动创建Promise对象作为结果容器
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread(() -> {
            // 3. 任意一个线程执行计算，计算完毕后向Promise填充结果
            log.debug("开始计算...");
            try {
//                int i = 1 / 0;
                Thread.sleep(3000);
                promise.setSuccess(80); // 主动设置成功结果，区别与Future
            } catch (Exception e) {
                e.printStackTrace();
                promise.setFailure(e); // 主动设置失败结果，区别于Future
            }
        }).start();

        // 4.1 同步接收结果
        // log.debug("等待结果...");
        // log.debug("结果是: {}", promise.get()); // 阻塞

        // 4.2 异步接收结果
        promise.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("接收结果:{}", future.getNow()); // 这里使用getNow立刻获取结果，不阻塞
            }
        });
    }

}
