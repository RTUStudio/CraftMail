package com.github.ipecter.rtustudio.craftmail.command;

import com.github.ipecter.rtustudio.craftmail.CraftMail;
import com.github.ipecter.rtustudio.craftmail.configuration.IconConfig;
import com.github.ipecter.rtustudio.craftmail.data.Mail;
import com.github.ipecter.rtustudio.craftmail.inventory.MailInventory;
import com.github.ipecter.rtustudio.craftmail.trigger.ExperienceTrigger;
import com.github.ipecter.rtustudio.craftmail.trigger.ItemTrigger;
import com.github.ipecter.rtustudio.craftmail.trigger.Trigger;
import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.command.RSCommandData;
import kr.rtuserver.framework.bukkit.api.configuration.translation.message.MessageTranslation;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MainCommand extends RSCommand<CraftMail> {

    private final IconConfig iconConfig;

    public MainCommand(CraftMail plugin) {
        super(plugin, "mail");
        this.iconConfig = plugin.getIconConfig();
    }

    @Override
    public boolean execute(RSCommandData data) {
        Trigger a = new ItemTrigger(List.of(new ItemStack(Material.APPLE), new ItemStack(Material.ACACIA_BOAT)));
        Trigger b = new ExperienceTrigger(100);
        Mail mail = new Mail(player().getUniqueId(), "제목", "내용", List.of(a, b));
        getPlugin().getMailManager().add(mail);
        player().openInventory(new MailInventory(getPlugin(), player()).getInventory());
        return true;
    }

    public void reload(RSCommandData data) {
        iconConfig.reload();
    }

}
