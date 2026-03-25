package kr.rtustudio.craftmail.event;

import kr.rtustudio.craftmail.data.Mail;
import kr.rtustudio.craftmail.trigger.Trigger;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 플레이어가 메일의 트리거(보상)를 수령할 때 발생하는 이벤트
 */
@Getter
public class MailReceiveEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Mail mail;
    private final List<Trigger> claimedTriggers;

    public MailReceiveEvent(Player player, Mail mail, List<Trigger> claimedTriggers) {
        this.player = player;
        this.mail = mail;
        this.claimedTriggers = claimedTriggers;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
