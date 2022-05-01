package xyz.xcye.feign.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author qsyyke
 * @date Created in 2022/5/1 13:33
 */

@Configuration
public class FeignAutoConfig {

    @Profile({"dev","test"})
    @Bean
    public Logger.Level logger() {
        return Logger.Level.FULL;
    }
}
