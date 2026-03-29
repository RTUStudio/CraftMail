package kr.rtustudio.craftmail.handler;

import kr.rtustudio.craftmail.CraftMail;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

@SuppressWarnings("unused")
public class PlayerJoinQuit extends RSListener<CraftMail> {

    public PlayerJoinQuit(CraftMail plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int unread = plugin.getMailManager().getUnreadCount(player.getUniqueId());
        if (unread > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                notifier.announce(player, message.get(player, "notify.unread")
                        .replace("{count}", String.valueOf(unread)));
            }, 20 * 3);
        }
    }
}
