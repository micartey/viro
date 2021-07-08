package me.micartey.viro.events.viro;

import lombok.Data;
import me.micartey.viro.settings.Settings;

@Data
public class SettingUpdateEvent  {

    private final Settings settings;

}
