package im.hdy.Controller;

import im.hdy.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

/**
 * Created by hdy on 30/12/2017.
 */
@Controller
@RequestMapping("page")
public class AddPageController {
    @Autowired
    private JedisPool jedisPool;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String addPage(@RequestParam(required = true) String title, @RequestParam(required = true) String text) {
        Jedis jedis =
                jedisPool.getResource();
        //根据title获取文章内容.这里要求文章标题必须是唯一的
        //因为没有数据库.暂时用title作为主键
        try {
            if (jedis.exists(title)) {
                //说明标题重复,返回重复
                return "false";
            } else {
                jedis.set("title:" + title, text);
                String[] words = StringUtils.getWord();
                //移除一些标点符号,并变成小写
                String temp = text.replace("\"", "").replace("(", "").replace(")", "").replace("'", "").replace(".", "").replace(",", "").replace(":", "").toLowerCase().replace("/", "");
                System.out.println(temp);
                String[] splits = temp.split(" ");
                Pipeline pipeline = jedis.pipelined();
                pipeline.multi();
                //设置为事务
                for (String split : splits) {
                    if (split.equals(" ")) {
                        continue;
                    }
                    for (String word : words) {
                        if (word.equals(split.trim())) {
                            continue;
                        }
                    }
                    //当前的单词和标题进行绑定
                    System.out.println(split.trim());
                    //用于将输入的英文字符进行相应的解析生成相应的索引
                    pipeline.sadd("idx:" + split.trim(), title);
                }
                //提交事务
                pipeline.exec();
            }
        } finally {
            jedis.close();
        }
        return "success";
    }

}
