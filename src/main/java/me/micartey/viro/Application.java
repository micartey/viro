package me.micartey.viro;

import javafx.application.Platform;
import javafx.stage.Stage;
import me.micartey.viro.events.spring.SpringTickEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
public class Application extends javafx.application.Application {

    private static ApplicationContext context;

    public static Toolkit toolkit;
    public static Robot robot;

    public static void main(String[] arguments) throws AWTException {
        toolkit = Toolkit.getDefaultToolkit();
        robot = new Robot();

        launch(arguments);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void setup() {
        Platform.setImplicitExit(false);

        //TODO: REMOVE
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                context.publishEvent(new SpringTickEvent(
                        System.currentTimeMillis()
                ));
            }
        }, 2000, 20);
    }

    @Override
    public void start(Stage primaryStage) {
        context = SpringApplication.run(Application.class);
    }
}
