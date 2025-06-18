package com.github.ipecter.rtustudio.craftmail.manager;

import com.github.ipecter.rtustudio.craftmail.CraftMail;
import com.github.ipecter.rtustudio.craftmail.data.Mail;
import com.github.ipecter.rtustudio.craftmail.protocol.MailPacket;
import com.github.ipecter.rtustudio.craftmail.trigger.Trigger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.rtuserver.framework.bukkit.api.platform.JSON;
import kr.rtuserver.framework.bukkit.api.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class MailManager {

    private final CraftMail plugin;

    private final Gson gson = new Gson();
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void readAll(UUID receiver) {
        Storage storage = plugin.getStorage();
        storage.set("Mail", JSON.of("receiver", receiver.toString()).get(), JSON.of("read", true).get());
    }

    @NotNull
    public List<Mail> get(UUID player) {
        Storage storage = plugin.getStorage();
        List<JsonObject> list = storage.get("Mail", JSON.of("receiver", player.toString()).get()).join();
        if (list.isEmpty()) return List.of();
        List<Mail> result = new ArrayList<>();
        for (JsonObject json : list) {
            UUID uuid = uuid(json, "uuid");
            UUID receiver = uuid(json, "receiver");
            String title = string(json, "title");
            String content = string(json, "content");
            Date date = date(json, "date");

            List<Trigger> triggers = new ArrayList<>();
            String triggerStr = string(json, "trigger");
            if (triggerStr != null) {
                JsonElement triggerElement = gson.fromJson(triggerStr, JsonElement.class);
                if (triggerElement instanceof JsonArray array) {
                    for (JsonElement element : array) {
                        JsonObject object = element.getAsJsonObject();
                        Trigger trigger = gson.fromJson(object, Trigger.class);
                        if (trigger != null) triggers.add(trigger);
                    }
                } else continue;
            }
            boolean read = bool(json, "read");
            Mail mail = new Mail(uuid, receiver, title, content, date, triggers, read);
            result.add(mail);
        }
        return result;
    }

    private boolean bool(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null) return false;
        return element.getAsBoolean();
    }

    private UUID uuid(JsonObject json, String key) {
        String text = string(json, key);
        if (text == null) return null;
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Date date(JsonObject json, String key) {
        String text = string(json, key);
        try {
            return format.parse(text);
        } catch (ParseException e) {
            return null;
        }
    }

    private String string(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null) return null;
        return element.getAsString();
    }

    private Integer integer(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null) return null;
        return element.getAsInt();
    }

    public void remove(UUID mail) {
        Storage storage = plugin.getStorage();
        storage.set("Mail", JSON.of("uuid", mail.toString()).get(), null).join();
    }

    public void add(Mail mail) {
        JsonArray triggerArray = new JsonArray();
        for (Trigger trigger : mail.getTriggers()) {
            triggerArray.add(gson.toJsonTree(trigger, Trigger.class));
        }

        JSON json = JSON.of("uuid", mail.getUuid().toString())
                .append("receiver", mail.getReceiver().toString())
                .append("title", mail.getTitle())
                .append("content", mail.getContent())
                .append("trigger", triggerArray.toString())
                .append("date", format.format(mail.getDate()))
                .append("read", mail.isRead());
        Storage storage = plugin.getStorage();
        storage.add("Mail", json.get()).join();
        plugin.newMail(mail.getReceiver());
    }

    public void interact(Mail mail) {
        JsonArray triggerArray = new JsonArray();
        for (Trigger trigger : mail.getTriggers()) {
            triggerArray.add(gson.toJsonTree(trigger, Trigger.class));
        }
        JSON json = JSON.of("trigger", triggerArray.toString()).append("read", mail.isRead());
        Storage storage = plugin.getStorage();
        storage.set("Mail", JSON.of("uuid", mail.getUuid().toString()).get(), json.get()).join();
    }

    public List<JsonObject> unreadMail(UUID player) {
        JSON json = JSON.of("receiver", player.toString()).append("read", false);
        Storage storage = plugin.getStorage();
        return storage.get("Mail", json).join();
    }
}