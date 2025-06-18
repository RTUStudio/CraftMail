package com.github.ipecter.rtustudio.craftmail.data;

import com.github.ipecter.rtustudio.craftmail.trigger.Trigger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Mail {

    private final UUID uuid;
    private final UUID receiver;
    private final String title;
    private final String content;
    private final Date date;

    private List<Trigger> triggers;
    private boolean read;

    public void read() {
        this.read = true;
    }

    public Mail(UUID receiver, String title, String content, List<Trigger> triggers) {
        this(UUID.randomUUID(), receiver, title, content, new Date(), triggers, false);
    }

    public Mail(UUID receiver, String title, String content) {
        this(UUID.randomUUID(), receiver, title, content, new Date(), new ArrayList<>(), false);
    }
}
