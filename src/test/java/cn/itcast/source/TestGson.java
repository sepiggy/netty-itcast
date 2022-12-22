package cn.itcast.source;

import cn.itcast.chat.protocol.Serializer;
import com.google.gson.*;

public class TestGson {
    public static void main(String[] args) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Serializer.ClassCodec()).create();
        System.out.println(gson.toJson(String.class));
    }
}
