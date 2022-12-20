package cn.itcast.netty_basic.future_promise;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * <h2>演示Netty中的Future</h2>
 * <pre>
 * 区别于JDK中的Future, Netty中的Future不仅可以以"同步"的方式获取结果，而且可以以"异步"的方式获取结果
 * <p>
 * 1) 同步的方式获取结果: future#get
 * 在同步的方式中调用 future#get 方法的线程和接收结果的线程都是一个线程：main 线程
 * 见日志：
 * 11:12:36 [DEBUG] [nioEventLoopGroup-2-1] c.i.n.c.TestNettyFuture - 执行计算
 * 11:12:36 [DEBUG] [main] c.i.n.c.TestNettyFuture - 等待结果
 * 11:12:39 [DEBUG] [main] c.i.n.c.TestNettyFuture - 结果是 70
 * <p>
 * 2) 异步的方式获取结果: future#addListener
 * 在异步的方式中调用 future#addListener 方法的线程和接收结果的线程不是一个线程: 一个 main 线程，一个 NioEventLoop 线程
 * 见日志：
 * 11:16:23 [DEBUG] [nioEventLoopGroup-2-1] c.i.n.c.TestNettyFuture - 执行计算
 * 11:16:26 [DEBUG] [nioEventLoopGroup-2-1] c.i.n.c.TestNettyFuture - 接收结果:70
 * </pre>
 */
@Slf4j
public class TestNettyFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 1. 事件循环组，对比JDK中的线程池
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next(); // 每个EventLoop对象里只有一个线程

        // 2. 提交任务
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(3000);
                return 70;
            }
        });

        // 方法1. 同步方式接收结果
//        log.debug("等待结果");
//        log.debug("结果是 {}", future.get()); // 阻塞

        // 方法2. 异步方式接收结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("接收结果:{}", future.getNow()); // 这里使用getNow立刻获取结果，不阻塞
            }
        });
    }

}
