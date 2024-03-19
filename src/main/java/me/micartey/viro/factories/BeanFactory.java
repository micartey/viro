package me.micartey.viro.factories;

import lombok.NonNull;
import me.micartey.jation.JationObserver;
import me.micartey.jation.interfaces.JationEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanFactory {

    @Bean
    public JationObserver getJationObserver(ApplicationContext context) {
        return new JationObserver("me.micartey.viro") {
            @Override
            public <T extends JationEvent<T>> void publish(@NonNull JationEvent<T> event, Object... additional) {
                super.publish(event, additional);
                context.publishEvent(event);
            }
        };
    }

}