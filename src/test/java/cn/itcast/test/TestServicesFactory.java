package cn.itcast.test;

import cn.itcast.chat.server.service.HelloService;
import cn.itcast.chat.server.service.ServicesFactory;

public class TestServicesFactory {
    public static void main(String[] args) {
        HelloService service = ServicesFactory.getService(HelloService.class);
        System.out.println(service.sayHello("hi"));
    }
}
