package cn.itcast.rpc.service;

public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String msg) {
        int i = 1 / 0; // 异常结果
        return "你好, " + msg; // 正常结果
    }

}