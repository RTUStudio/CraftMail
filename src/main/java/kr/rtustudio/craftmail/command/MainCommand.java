package kr.rtustudio.craftmail.command;

import kr.rtustudio.craftmail.CraftMail;
import kr.rtustudio.craftmail.configuration.MenuConfig;
import kr.rtustudio.craftmail.inventory.MailInventory;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.entity.Player;

public class MainCommand extends RSCommand<CraftMail> {

    public MainCommand(CraftMail plugin) {
        super(plugin, "mail");
        registerCommand(new SendCommand(plugin));
    }

    @Override
    public Result execute(CommandArgs data) {
        Player player = player();
        if (player == null) return Result.ONLY_PLAYER;
        player.openInventory(new MailInventory(plugin, player).getInventory());
        return Result.SUCCESS;
    }

    @Override
    public void reload(CommandArgs data) {
        plugin.reloadConfiguration(MenuConfig.class);
    }

}
