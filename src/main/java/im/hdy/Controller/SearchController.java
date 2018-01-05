package im.hdy.Controller;

import im.hdy.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
    public String search(@RequestParam(required = true) String s, Model model) {
        //首先暂时先通过空格进行关键词的搜索
        long start = System.currentTimeMillis();
        String[] split = s.toLowerCase().trim().split(" ");
        ArrayList<Page> pages = (ArrayList<Page>) searchFromRedis(split, s)[0];
        ArrayList<String> recommend = (ArrayList<String>) searchFromRedis(split, s)[1];
//        System.out.println(pages);
        model.addAttribute("pages", pages);
        model.addAttribute("recommend", recommend);
        model.addAttribute("search_text", s);
        //About 695,000,000 results (0.48 seconds)
        model.addAttribute("result", "About " + pages.size() + " results (" + ((System.currentTimeMillis() - start) / 1000) + " seconds)");
        return "search";
    }


    /**
     * 从Redis中获取数据
     *
     * @param spilt 获取的搜索分词信息
     */
    public Object[] searchFromRedis(String[] spilt, String s) {
        if (spilt == null || spilt.length == 0) {
            return null;
        }
        Jedis jedis = jedisPool.getResource();
        try {
            String name = "";
            //准备事务流
            Pipeline pipeline = jedis.pipelined();
            pipeline.multi();
            //准备数组存放临时的推荐搜索关键词
            String[] spiltKey = new String[spilt.length];
            for (int i = 0; i < spilt.length; i++) {
                String trim = spilt[i].trim();
                pipeline.sadd("search:" + trim, s);
                spiltKey[i] = "search:" + trim;
                spilt[i] = "idx:" + spilt[i].trim();
                name = name + spilt[i] + "+";
            }
            //执行事务流
            pipeline.exec();
            try {
                pipeline.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //获取推荐搜索内容
            Set<String> keys = jedis.sinter(spiltKey);
            System.out.println(keys);
            Iterator<String> keyIterator = keys.iterator();
            //用于存放获取到的推荐搜索内容
            ArrayList<String> keyArrays = new ArrayList<String>();
            int temp = 0;
            while (keyIterator.hasNext() && temp <= 8) {
                String next = keyIterator.next();
                keyArrays.add(next);
//                System.out.println("aaaa");
                temp++;
            }

            Set<String> smembers = jedis.smembers(name);
            if (smembers == null || smembers.size() == 0) {
                //判断是否有缓存
                //如果没有
                Long sunionstore = jedis.sinterstore(name, spilt);
                //数据最多只保存五分钟,为了节省内存
                jedis.expire(name, 60 * 5);
                smembers = jedis.smembers(name);
            }
            Iterator<String> iterator = smembers.iterator();
            //准备事务流
            ArrayList<Page> pages = new ArrayList<Page>();
            while (iterator.hasNext()) {
                String next = iterator.next();
                String text = jedis.get("title:" + next).substring(30);
                Page page = new Page(next, text.substring(0, 200) + "...");
                pages.add(page);
            }
            System.out.println(keyArrays);
            Object[] objs = new Object[]{pages, keyArrays};

            return objs;
        } finally {
            jedis.close();
        }
    }
}
