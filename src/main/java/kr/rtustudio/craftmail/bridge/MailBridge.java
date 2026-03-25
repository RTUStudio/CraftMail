package kr.rtustudio.craftmail.bridge;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.craftmail.CraftMail;
import kr.rtustudio.craftmail.data.Mail;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MailBridge {

    private static final BridgeChannel CHANNEL = BridgeChannel.of("craftmail", "notify");

    private final CraftMail plugin;
    private final Proxium proxium;
    private final MessageTranslation message;
    private final Notifier notifier;

    public MailBridge(CraftMail plugin) {
        this.plugin = plugin;
        this.proxium = plugin.getBridge(Proxium.class);
        this.message = plugin.getConfiguration().getMessage();
        this.notifier = Notifier.of(plugin);

        proxium.subscribe(CHANNEL, NotifyPacket.class, packet ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Player player = Bukkit.getPlayer(packet.receiver());
                    if (player != null) {
                        notifier.announce(player, message.get(player, "mail-arrived"));
                    }
                })
        );
    }

    public void notify(Mail mail) {
        proxium.publish(CHANNEL, new NotifyPacket(mail.getReceiver()));
    }

    public void close() {
        proxium.unsubscribe(CHANNEL);
    }
}
