package kr.rtustudio.craftmail.event;

import kr.rtustudio.craftmail.data.Mail;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * 메일이 전송될 때 발생하는 이벤트
 * <p>취소 시 메일이 저장되지 않습니다.</p>
 */
@Getter
public class MailSendEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Mail mail;
    @Setter
    private boolean cancelled;

    public MailSendEvent(Mail mail) {
        this.mail = mail;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
