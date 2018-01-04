package im.hdy.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

/**
 * Created by hdy on 30/12/2017.
 * 搜索控制器
 */
@Controller
@RequestMapping("search")
public class SearchController {
    @Autowired
    private JedisPool jedisPool;

    /**
     * 用于主页搜索关键词
     */
    @RequestMapping(method = RequestMethod.POST)
    public String search(@RequestParam(required = true) String s) {
        //首先暂时先通过空格进行关键词的搜索
        String[] split = s.split(" ");
        Set<String> searchs = searchFromRedis(split);
        System.out.println(searchs);
        return "search";
    }


    /**
     * 从Redis中获取数据
     *
     * @param spilt 获取的搜索分词信息
     */
    public Set<String> searchFromRedis(String[] spilt) {
        if (spilt == null || spilt.length == 0) {
            return null;
        }
        Jedis jedis = jedisPool.getResource();
        try {
            String name = "";
            for (int i = 0; i < spilt.length; i++) {
                spilt[i] = "idx:" + spilt[i].trim();
                name = name + spilt[i] + "+";
            }
            Set<String> smembers = jedis.smembers(name);
            if (smembers == null || smembers.size() == 0) {
                //判断是否有缓存
                //如果没有
                Long sunionstore = jedis.sunionstore(name, spilt);
                //数据最多只保存五分钟,为了节省内存
                jedis.expire(name, 60 * 5);
                smembers = jedis.smembers(name);
            }
            return smembers;
        } finally {
            jedis.close();
        }
    }
}
