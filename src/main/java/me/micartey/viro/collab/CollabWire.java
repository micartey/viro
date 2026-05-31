package me.micartey.viro.collab;

import com.google.gson.*;
import javafx.scene.paint.Color;
import me.micartey.viro.shapes.*;
import me.micartey.viro.shapes.utilities.Position;

import java.lang.reflect.Type;
import java.util.*;

public final class CollabWire {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorAdapter())
            .registerTypeAdapter(Shape.class, new ShapeAdapter())
            .registerTypeAdapter(Position.class, (InstanceCreator<Position>) type -> new Position(0, 0))
            .create();

    public record CollabShape(int id, String origin, Shape shape) {}

    public static String toJson(int id, String origin, Shape shape) {
        JsonObject obj = GSON.toJsonTree(shape, Shape.class).getAsJsonObject();
        obj.addProperty("id", id);
        obj.addProperty("origin", origin);
        return obj.toString();
    }

    public static CollabShape fromJson(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        int id = obj.get("id").getAsInt();
        String origin = obj.has("origin") ? obj.get("origin").getAsString() : "unknown";
        Shape shape = GSON.fromJson(obj, Shape.class);
        return new CollabShape(id, origin, shape);
    }

    public static String contentHash(Shape shape) {
        JsonObject obj = GSON.toJsonTree(shape, Shape.class).getAsJsonObject();
        // Remove unstable fields to compute a stable content fingerprint
        obj.remove("id");
        obj.remove("origin");
        return String.valueOf(obj.toString().hashCode());
    }

    private static class ColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
        @Override
        public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("red", src.getRed());
            obj.addProperty("green", src.getGreen());
            obj.addProperty("blue", src.getBlue());
            obj.addProperty("opacity", src.getOpacity());
            return obj;
        }

        @Override
        public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            return new Color(
                    obj.get("red").getAsDouble(),
                    obj.get("green").getAsDouble(),
                    obj.get("blue").getAsDouble(),
                    obj.get("opacity").getAsDouble()
            );
        }
    }

    private static class ShapeAdapter implements JsonSerializer<Shape>, JsonDeserializer<Shape> {
        @Override
        public JsonElement serialize(Shape src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("color", context.serialize(src.getColor()));
            obj.addProperty("width", src.getWidth());

            if (src instanceof Path path) {
                obj.addProperty("type", "Path");
                JsonArray points = new JsonArray();
                for (Map.Entry<Position, Integer> entry : path.getPositionWidthMap().entrySet()) {
                    JsonObject point = new JsonObject();
                    point.addProperty("x", entry.getKey().getX());
                    point.addProperty("y", entry.getKey().getY());
                    point.addProperty("width", entry.getValue());
                    points.add(point);
                }
                obj.add("points", points);
            } else if (src instanceof Rectangle rect) {
                obj.addProperty("type", "Rectangle");
                obj.add("position", context.serialize(rect.getRectPosition()));
                obj.add("size", context.serialize(rect.getRectSize()));
            } else if (src instanceof Polygon poly) {
                obj.addProperty("type", "Polygon");
                JsonArray pts = new JsonArray();
                for (Position p : poly.getPoints()) {
                    pts.add(context.serialize(p));
                }
                obj.add("points", pts);
            }

            return obj;
        }

        @Override
        public Shape deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            String type = obj.get("type").getAsString();
            Color color = context.deserialize(obj.get("color"), Color.class);
            int width = obj.get("width").getAsInt();

            return switch (type) {
                case "Path" -> deserializePath(obj, color, width);
                case "Rectangle" -> {
                    Position pos = context.deserialize(obj.get("position"), Position.class);
                    Position size = context.deserialize(obj.get("size"), Position.class);
                    yield new Rectangle(color, width, pos, size);
                }
                case "Polygon" -> {
                    JsonArray pts = obj.getAsJsonArray("points");
                    List<Position> positions = new ArrayList<>();
                    for (JsonElement elem : pts) {
                        positions.add(context.deserialize(elem, Position.class));
                    }
                    yield new Polygon(color, width, positions.toArray(new Position[0]));
                }
                default -> throw new JsonParseException("Unknown shape type: " + type);
            };
        }

        private Shape deserializePath(JsonObject obj, Color color, int width) {
            Map<Position, Integer> positions = new LinkedHashMap<>();
            JsonArray points = obj.getAsJsonArray("points");
            for (JsonElement elem : points) {
                JsonObject pt = elem.getAsJsonObject();
                positions.put(
                        new Position(pt.get("x").getAsDouble(), pt.get("y").getAsDouble()),
                        pt.get("width").getAsInt()
                );
            }
            return new Path(positions, color, width);
        }
    }
}
