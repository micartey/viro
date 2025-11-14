package me.micartey.viro.events.spring;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event that is being used for animations.
 *
 * Will be executed every 20ms {@see me.micartey.viro.Application}
 */
@Getter
@AllArgsConstructor
public class SpringTickEvent {

    private final long timestamp;
}
