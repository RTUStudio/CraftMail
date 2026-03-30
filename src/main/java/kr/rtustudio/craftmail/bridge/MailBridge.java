package kr.rtustudio.craftmail.bridge;

import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.craftmail.CraftMail;
import kr.rtustudio.craftmail.data.Mail;
import kr.rtustudio.craftmail.inventory.MailInventory;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MailBridge {

    private final Proxium proxium;
    private final CraftMail plugin;

    public MailBridge(CraftMail plugin) {
        this.plugin = plugin;
        this.proxium = plugin.getBridge(Proxium.class);

        MessageTranslation message = plugin.getConfiguration().getMessage();
        Notifier notifier = Notifier.of(plugin);

        proxium.subscribe(plugin.getChannel(), NotifyPacket.class, packet ->
                CraftScheduler.sync(plugin, () -> {
                    Player player = Bukkit.getPlayer(packet.receiver());
                    if (player != null) {
                        notifier.announce(player, message.get(player, "notify.arrived"));
                        Inventory top = player.getOpenInventory().getTopInventory();
                        if (top.getHolder() instanceof MailInventory mailInv) {
                            mailInv.refresh();
                        }
                    }
                })
        );
    }

    public void notify(Mail mail) {
        proxium.publish(plugin.getChannel(), new NotifyPacket(mail.getReceiver()));
    }

    public void close() {
        proxium.unsubscribe(plugin.getChannel());
    }
}
