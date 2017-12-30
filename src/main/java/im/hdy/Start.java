package im.hdy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by hdy on 30/12/2017.
 * Spring boot 的启动类
 */
@SpringBootApplication
@ComponentScan
public class Start {

    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }
}
