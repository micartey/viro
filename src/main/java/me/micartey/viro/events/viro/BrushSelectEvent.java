package me.micartey.viro.events.viro;

import lombok.Data;
import me.micartey.viro.brushes.Brush;

@Data
public class BrushSelectEvent {

    private final Brush previous, current;

}
