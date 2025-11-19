package me.micartey.viro.factories;

import lombok.NonNull;
import me.micartey.jation.JationObserver;
import me.micartey.jation.interfaces.JationEvent;
import me.micartey.viro.mcp.ShapeTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class BeanFactory {

    @Bean
    public JationObserver getJationObserver(ApplicationContext context) {
        return new JationObserver() {
            @Override
            public <T extends JationEvent<T>> void publish(@NonNull JationEvent<T> event, Object... additional) {
                super.publish(event, additional);
                context.publishEvent(event);
            }
        };
    }

    @Bean
    @Lazy
    public ToolCallbackProvider shapeTools(ShapeTool shapeTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(shapeTool)
                .build();
    }
}