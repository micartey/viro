package me.micartey.viro.window;

import com.sun.javafx.application.PlatformImpl;
import lombok.SneakyThrows;
import me.micartey.viro.Application;
import me.micartey.viro.settings.Settings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class Tray {

    @SneakyThrows
    public Tray(Settings settings, @Value("${application.title}") String title, @Value("${application.icon}") String icon) {
//        SystemTray systemTray = SystemTray.getSystemTray();
//
//        TrayIcon trayIcon = new TrayIcon(
//                Application.toolkit.getImage(Tray.class.getResource(icon)),
//                title
//        );
//
//        trayIcon.setImageAutoSize(true);
//
//        systemTray.add(trayIcon);
//
//        trayIcon.addActionListener(event -> {
//            PlatformImpl.runLater(settings::show);
//        });
    }
}
