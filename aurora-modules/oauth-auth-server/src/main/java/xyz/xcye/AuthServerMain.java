package xyz.xcye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author qsyyke
 * @date Created in 2022/5/4 09:49
 */

@EnableFeignClients
@SpringBootApplication
public class AuthServerMain {
    public static void main(String[] args) {
        SpringApplication.run(AuthServerMain.class, args);
    }
}