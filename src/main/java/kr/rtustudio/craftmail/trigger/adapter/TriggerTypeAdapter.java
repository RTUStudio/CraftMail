package kr.rtustudio.craftmail.trigger.adapter;

import kr.rtustudio.craftmail.trigger.Trigger;
import kr.rtustudio.craftmail.trigger.TriggerRegistry;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class TriggerTypeAdapter extends TypeAdapter<Trigger> {

    private final Gson gson = new Gson();

    @Override
    public void write(JsonWriter out, Trigger value) throws IOException {
        out.beginObject();
        out.name("type");
        out.value(value.type());
        out.name("value");
        JsonElement element = gson.toJsonTree(value, value.getClass());
        Streams.write(element, out);
        out.endObject();
    }

    @Override
    public Trigger read(JsonReader in) {
        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        String value = jsonObject.get("value").toString();

        Class<? extends Trigger> clazz = TriggerRegistry.getInstance().resolve(type);
        if (clazz == null) return null;
        return gson.fromJson(value, clazz);
    }
}
