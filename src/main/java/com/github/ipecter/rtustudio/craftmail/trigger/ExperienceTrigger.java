package com.github.ipecter.rtustudio.craftmail.trigger;

import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.entity.Player;

import java.io.IOException;

@JsonAdapter(ExperienceTrigger.GsonAdapter.class)
public record ExperienceTrigger(int amount) implements Trigger {

    @Override
    public String type() {
        return "experience";
    }

    @Override
    public boolean execute(Player player) {
        player.giveExp(amount);
        return true;
    }

    public static class GsonAdapter extends TypeAdapter<ExperienceTrigger> {

        @Override
        public void write(JsonWriter out, ExperienceTrigger value) throws IOException {
            out.value(value.amount);
        }

        @Override
        public ExperienceTrigger read(JsonReader in) throws IOException {
            int amount = in.nextInt();
            return new ExperienceTrigger(amount);
        }

    }

}
