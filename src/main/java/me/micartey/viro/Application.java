package me.micartey.viro;

import javafx.stage.Stage;
import me.micartey.viro.events.spring.SpringTickEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application extends javafx.application.Application {

    private static ApplicationContext context;

    public static void main(String[] arguments) {
        launch(arguments);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void setup() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            context.publishEvent(new SpringTickEvent(
                    System.currentTimeMillis()
            ));
        }, 2000, 20, TimeUnit.MILLISECONDS);
    }

    @Override
    public void start(Stage primaryStage) {
        context = SpringApplication.run(Application.class);
    }
}
