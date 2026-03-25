package kr.rtustudio.craftmail;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.configurate.model.ConfigPath;
import kr.rtustudio.craftmail.bridge.MailBridge;
import kr.rtustudio.craftmail.command.MainCommand;
import kr.rtustudio.craftmail.configuration.MenuConfig;
import kr.rtustudio.craftmail.handler.PlayerJoinQuit;
import kr.rtustudio.craftmail.manager.MailManager;
import kr.rtustudio.craftmail.trigger.ExperienceTrigger;
import kr.rtustudio.craftmail.trigger.ItemTrigger;
import kr.rtustudio.craftmail.trigger.TriggerRegistry;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import lombok.Getter;

import java.util.List;

public class CraftMail extends RSPlugin {

    @Getter
    private static CraftMail instance;
    @Getter
    private BridgeChannel channel;
    @Getter
    private MailManager mailManager;
    @Getter
    private TriggerRegistry triggerRegistry;
    @Getter
    private MailBridge mailBridge;

    @Override
    protected void load() {
        instance = this;
    }

    @Override
    protected void enable() {
        channel = BridgeChannel.of("craftmail", "notify");

        triggerRegistry = new TriggerRegistry();
        triggerRegistry.register(new ItemTrigger(List.of()));
        triggerRegistry.register(new ExperienceTrigger(0));

        registerStorage("Mail");
        registerConfiguration(MenuConfig.class, ConfigPath.of("Menu"));

        mailManager = new MailManager(this);
        mailBridge = new MailBridge(this);

        registerEvent(new PlayerJoinQuit(this));
        registerCommand(new MainCommand(this), true);
    }

    @Override
    protected void disable() {
        if (mailBridge != null) mailBridge.close();
    }
}
