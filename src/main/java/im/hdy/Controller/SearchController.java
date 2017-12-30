package im.hdy.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.JedisPool;

/**
 * Created by hdy on 30/12/2017.
 * 搜索控制器
 */
@Controller
@RequestMapping("search")
public class SearchController {
    @Autowired
    private JedisPool jedisPool;

    
}
