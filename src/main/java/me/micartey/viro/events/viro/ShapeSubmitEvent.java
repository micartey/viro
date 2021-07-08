package me.micartey.viro.events.viro;

import lombok.Data;
import me.micartey.viro.shapes.Shape;

@Data
public class ShapeSubmitEvent {

    private final Shape shape;

}
