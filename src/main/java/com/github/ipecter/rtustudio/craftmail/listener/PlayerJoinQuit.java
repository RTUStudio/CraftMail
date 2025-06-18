package com.github.ipecter.rtustudio.craftmail.listener;

import com.github.ipecter.rtustudio.craftmail.CraftMail;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinQuit extends RSListener<CraftMail> {

    public PlayerJoinQuit(CraftMail plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onJoin(PlayerJoinEvent e) {

    }

}
