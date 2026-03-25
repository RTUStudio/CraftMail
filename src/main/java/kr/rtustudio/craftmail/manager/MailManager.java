package kr.rtustudio.craftmail.manager;

import kr.rtustudio.craftmail.CraftMail;
import kr.rtustudio.craftmail.bridge.MailBridge;
import kr.rtustudio.craftmail.data.Mail;
import kr.rtustudio.craftmail.event.MailReceiveEvent;
import kr.rtustudio.craftmail.event.MailSendEvent;
import kr.rtustudio.craftmail.trigger.Trigger;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import kr.rtustudio.storage.JSON;
import kr.rtustudio.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MailManager {

    private final CraftMail plugin;
    private final Storage storage;
    private final MessageTranslation message;
    private final Notifier notifier;

    private final Gson gson = new Gson();
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MailManager(CraftMail plugin) {
        this.plugin = plugin;
        this.storage = plugin.getStorage("Mail");
        this.message = plugin.getConfiguration().getMessage();
        this.notifier = Notifier.of(plugin);
    }

    // ==================== 메일 전송 ====================

    public boolean add(Mail mail) {
        MailSendEvent event = new MailSendEvent(mail);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        JsonArray triggerArray = new JsonArray();
        for (Trigger trigger : mail.getTriggers()) {
            triggerArray.add(gson.toJsonTree(trigger, Trigger.class));
        }

        JSON json = JSON.of("uuid", mail.getUuid().toString())
                .append("sender", mail.getSender() != null ? mail.getSender().toString() : "")
                .append("receiver", mail.getReceiver().toString())
                .append("title", mail.getTitle())
                .append("content", mail.getContent())
                .append("trigger", triggerArray.toString())
                .append("date", format.format(mail.getDate()))
                .append("read", mail.isRead());
        storage.add(json).join();

        notifyReceiver(mail);
        return true;
    }

    private void notifyReceiver(Mail mail) {
        Player receiver = Bukkit.getPlayer(mail.getReceiver());
        if (receiver != null) {
            notifier.announce(receiver, message.get(receiver, "mail-arrived"));
            return;
        }
        MailBridge bridge = plugin.getMailBridge();
        if (bridge != null) {
            bridge.notify(mail);
        }
    }

    // ==================== 메일 조회 ====================

    @NotNull
    public List<Mail> get(UUID player) {
        List<JsonObject> list = storage.get(JSON.of("receiver", player.toString())).join();
        if (list.isEmpty()) return List.of();
        List<Mail> result = new ArrayList<>();
        for (JsonObject json : list) {
            UUID uuid = uuid(json, "uuid");
            UUID sender = uuid(json, "sender");
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
            Mail mail = new Mail(uuid, sender, receiver, title, content, date, triggers, read);
            result.add(mail);
        }
        return result;
    }

    public int getUnreadCount(UUID player) {
        JSON json = JSON.of("receiver", player.toString()).append("read", false);
        return storage.get(json).join().size();
    }

    // ==================== 메일 관리 ====================

    public void readAll(UUID receiver) {
        storage.set(JSON.of("receiver", receiver.toString()), JSON.of("read", true));
    }

    public void remove(UUID mail) {
        storage.set(JSON.of("uuid", mail.toString()), null);
    }

    // ==================== 트리거 수령 ====================

    /**
     * 메일의 트리거(보상)를 실행합니다.
     *
     * @return 모든 트리거가 성공적으로 실행되었으면 true
     */
    public boolean claimTriggers(Player player, Mail mail) {
        List<Trigger> claimed = new ArrayList<>();
        mail.getTriggers().removeIf(trigger -> {
            boolean success = trigger.execute(player);
            if (success) claimed.add(trigger);
            return success;
        });

        if (!claimed.isEmpty()) {
            Bukkit.getPluginManager().callEvent(new MailReceiveEvent(player, mail, claimed));
        }

        if (mail.getTriggers().isEmpty()) mail.read();
        interact(mail);
        return mail.getTriggers().isEmpty();
    }

    /**
     * 메일의 읽음/트리거 상태를 저장소에 반영합니다.
     */
    public void interact(Mail mail) {
        JsonArray triggerArray = new JsonArray();
        for (Trigger trigger : mail.getTriggers()) {
            triggerArray.add(gson.toJsonTree(trigger, Trigger.class));
        }
        JSON json = JSON.of("trigger", triggerArray.toString()).append("read", mail.isRead());
        storage.set(JSON.of("uuid", mail.getUuid().toString()), json).join();
    }

    // ==================== JSON 유틸 ====================

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
}
