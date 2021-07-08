package me.micartey.viro.window.utilities;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class Position implements Serializable {

    private final double x, y;

    /**
     * Compute the distance of two {@link Position positions}
     *
     * @param other {@link Position position}
     * @return relative distance between each other
     */
    public double distance(Position other) {
        double x = this.x - other.x;
        double y = this.y - other.y;
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Compute the {@link Position position} in the middle
     * of two positions.
     *
     * @param other {@link Position position}
     * @return new Position in the middle
     */
    public Position middle(Position other) {
        return new Position(
                (this.x - other.x) / 2,
                (this.y - other.y) / 2
        );
    }

    /**
     * Check if {@link Position position} is inside a square of two positions.
     *
     * @param position First Position
     * @param other    Second Position
     * @return true if Position is in between
     */
    public boolean between(Position position, Position other) {
        Position top = new Position(
                Math.min(position.x, other.x),
                Math.min(position.y, other.y)
        );

        Position bottom = new Position(
                Math.max(position.x, other.x),
                Math.max(position.y, other.y)
        );

        return top.x <= this.x && top.y <= this.y && bottom.x >= this.x && bottom.y >= this.y;
    }

    /**
     * Apply a vector to the current {@link Position position}
     * object
     *
     * @param vector direction
     * @return copy of Position
     */
    public Position translate(Position vector) {
        return new Position(
                this.x + vector.x,
                this.y + vector.y
        );
    }

    /**
     * Apply a vector to the current {@link Position position}
     * object
     *
     * @param vector direction
     * @return copy of Position
     */
    public Position translate(double vector) {
        return new Position(
                this.x + vector,
                this.y + vector
        );
    }

    /**
     * Get the vector between two points
     *
     * @param destination Destination
     * @return copy of Position
     */
    public Position direction(Position destination) {
        return new Position(
                destination.x - this.x,
                destination.y - this.y
        );
    }
}
