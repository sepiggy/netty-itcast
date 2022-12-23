package cn.itcast.im.server.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h2>基于内存的登录接口实现</h2>
 */
public class UserServiceMemoryImpl implements UserService {

    private Map<String, String> allUserMap = new ConcurrentHashMap<>();

    {
        allUserMap.put("zhangsan", "123");
        allUserMap.put("lisi", "123");
        allUserMap.put("wangwu", "123");
        allUserMap.put("zhaoliu", "123");
        allUserMap.put("qianqi", "123");
    }

    @Override
    public boolean login(String username, String password) {
        String pass = allUserMap.get(username);
        if (pass == null) {
            return false;
        }
        return pass.equals(password);
    }

}
