package im.hdy.Controller;

import com.sun.java.swing.plaf.motif.MotifLookAndFeel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by hdy on 05/01/2018.
 */
@Controller
@RequestMapping(value = "show")
public class ShowController {
    @Autowired
    JedisPool jedisPool;

    /**
     * 用于浏览界面的跳转
     *
     * @param title 标题 (标题是唯一的主键)
     */
    @RequestMapping
    public String show(@RequestParam(required = true) String title, @RequestParam(required = true) String search_text, Model model) {
        Jedis jedis = jedisPool.getResource();
        try {
            model.addAttribute("title", title);
            model.addAttribute("search_text", search_text);
            String page = jedis.get("title:" + title);
            model.addAttribute("page", page);
        } finally {
            jedis.close();
        }
        return "show";
    }
}
