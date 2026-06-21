package me.micartey.viro.collab;

import com.google.gson.*;
import javafx.scene.paint.Color;
import me.micartey.viro.shapes.Path;
import me.micartey.viro.shapes.Polygon;
import me.micartey.viro.shapes.Rectangle;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.shapes.utilities.Position;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CollabWire {

    private CollabWire() {
    }

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorAdapter())
            .registerTypeAdapter(Shape.class, new ShapeAdapter())
            .registerTypeAdapter(Position.class, new PositionAdapter())
            .create();

    public record CollabShape(CollabShapeId id, Shape shape) {}

    public record CollabCursor(String origin, Position position, long updatedAt) {}

    public static String toJson(CollabShapeEntry entry) {
        return toJson(entry.id(), entry.shape());
    }

    public static String toJson(CollabShapeId id, Shape shape) {
        JsonObject obj = GSON.toJsonTree(shape, Shape.class).getAsJsonObject();
        obj.addProperty("id", id.sequence());
        obj.addProperty("origin", id.origin());
        return GSON.toJson(obj);
    }

    public static String toJsonArray(Collection<CollabShapeEntry> entries) {
        JsonArray array = new JsonArray();
        for (CollabShapeEntry entry : entries) {
            array.add(JsonParser.parseString(toJson(entry)));
        }
        return GSON.toJson(array);
    }

    public static CollabShape fromJson(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        CollabShapeId id = readShapeId(obj);
        Shape shape = GSON.fromJson(obj, Shape.class);
        return new CollabShape(id, shape);
    }

    public static String cursorJson(CollabCursor cursor) {
        JsonObject obj = new JsonObject();
        obj.addProperty("origin", cursor.origin());
        obj.add("position", GSON.toJsonTree(cursor.position(), Position.class));
        obj.addProperty("updatedAt", cursor.updatedAt());
        return GSON.toJson(obj);
    }

    public static CollabCursor cursorFromJson(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        String origin = obj.get("origin").getAsString();
        Position position = GSON.fromJson(obj.get("position"), Position.class);
        long updatedAt = obj.has("updatedAt") ? obj.get("updatedAt").getAsLong() : System.currentTimeMillis();
        return new CollabCursor(origin, position, updatedAt);
    }

    public static String deleteJson(CollabShapeId id) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id.sequence());
        obj.addProperty("origin", id.origin());
        return GSON.toJson(obj);
    }

    public static CollabShapeId deleteId(String json) {
        return readShapeId(JsonParser.parseString(json).getAsJsonObject());
    }

    public static String contentHash(Shape shape) {
        JsonObject obj = GSON.toJsonTree(shape, Shape.class).getAsJsonObject();
        return String.valueOf(obj.toString().hashCode());
    }

    public static String stringBody(String body) {
        if (body == null) {
            return "";
        }

        String trimmed = body.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        try {
            JsonElement element = JsonParser.parseString(trimmed);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                return element.getAsString().trim();
            }
        } catch (JsonParseException ignored) {
            // Plain text request bodies are valid for the simple peer endpoint.
        }

        return trimmed;
    }

    private static CollabShapeId readShapeId(JsonObject obj) {
        int sequence = obj.get("id").getAsInt();
        String origin = obj.has("origin") ? obj.get("origin").getAsString() : "unknown";
        return new CollabShapeId(origin, sequence);
    }

    private static class PositionAdapter implements JsonSerializer<Position>, JsonDeserializer<Position> {
        @Override
        public JsonElement serialize(Position src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", src.getX());
            obj.addProperty("y", src.getY());
            return obj;
        }

        @Override
        public Position deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            return new Position(obj.get("x").getAsDouble(), obj.get("y").getAsDouble());
        }
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
                poly.getPoints().stream()
                        .sorted(Comparator.comparingDouble(Position::getX).thenComparingDouble(Position::getY))
                        .map(context::serialize)
                        .forEach(pts::add);
                obj.add("points", pts);
            } else {
                throw new JsonParseException("Unsupported shape type: " + src.getClass().getName());
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
