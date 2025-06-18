package com.github.ipecter.rtustudio.craftmail.command;

import com.github.ipecter.rtustudio.craftmail.CraftMail;
import com.github.ipecter.rtustudio.craftmail.configuration.IconConfig;
import com.google.gson.JsonObject;
import kr.rtuserver.framework.bukkit.api.command.RSCommand;
import kr.rtuserver.framework.bukkit.api.command.RSCommandData;

import java.util.List;

public class TestCommand extends RSCommand<CraftMail> {

    private final IconConfig iconConfig;

    public TestCommand(CraftMail plugin) {
        super(plugin, "test");
        this.iconConfig = plugin.getIconConfig();
    }

    @Override
    public boolean execute(RSCommandData data) {
        List<JsonObject> list = getPlugin().getMailManager().unreadMail(player().getUniqueId());
        chat().send(String.join(", ", list.stream().map(JsonObject::toString).toList()));
        return true;
    }

    public void reload(RSCommandData data) {
        iconConfig.reload();
    }

}
