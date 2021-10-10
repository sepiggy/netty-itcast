package cn.itcast.nio.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client1 {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));
        SocketAddress address = sc.getLocalAddress();
        sc.write(Charset.defaultCharset().encode("01234\n56789abcdef3333\n"));
        sc.write(Charset.defaultCharset().encode("0123456789zxcvbnabcdef3333\n"));
        System.in.read();
    }
}
