package com.github.ipecter.rtustudio.craftmail;

import com.github.ipecter.rtustudio.craftmail.data.Mail;
import org.bukkit.entity.Player;

public class MailAPI {

    private static CraftMail plugin;

    private static CraftMail plugin() {
        if (plugin == null) plugin = CraftMail.getInstance();
        return plugin;
    }


}
