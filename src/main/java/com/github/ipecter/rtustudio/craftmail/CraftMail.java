package com.github.ipecter.rtustudio.craftmail;

import com.github.ipecter.rtustudio.craftmail.command.MainCommand;
import com.github.ipecter.rtustudio.craftmail.command.TestCommand;
import com.github.ipecter.rtustudio.craftmail.configuration.IconConfig;
import com.github.ipecter.rtustudio.craftmail.data.Mail;
import com.github.ipecter.rtustudio.craftmail.inventory.MailInventory;
import com.github.ipecter.rtustudio.craftmail.manager.MailManager;
import com.github.ipecter.rtustudio.craftmail.protocol.MailPacket;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.protoweaver.api.callback.HandlerCallback;
import kr.rtuserver.protoweaver.api.netty.ProtoConnection;
import kr.rtuserver.protoweaver.api.protocol.Packet;
import kr.rtuserver.protoweaver.api.protocol.internal.CustomPacket;
import kr.rtuserver.protoweaver.api.protocol.serializer.CustomPacketSerializer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class CraftMail extends RSPlugin {

    @Getter
    private static CraftMail instance;

    @Getter
    private IconConfig iconConfig;

    @Getter
    private MailManager mailManager;

    @Getter
    private ProtoConnection protoConnection;

    @Override
    public void load() {
        instance = this;
    }

    @Override
    public void enable() {
        getConfigurations().getStorage().init("Mail");

        iconConfig = new IconConfig(this);

        mailManager = new MailManager(this);

        registerCommand(new MainCommand(this), true);
        registerCommand(new TestCommand(this));

        HandlerCallback callback = new HandlerCallback(ready -> protoConnection = ready.protoConnection(), this::receivePacket);
        registerProtocol("craftmail", "refresh", Packet.of(MailPacket.class, CustomPacketSerializer.class), null, callback);
    }

    private void receivePacket(HandlerCallback.Packet packet) {
        System.out.println(packet.packet());
        if (packet.packet() instanceof MailPacket(UUID receiver)) {
            refreshInventory(receiver);
        }
    }

    public void newMail(UUID receiver) {
        System.out.println(protoConnection);
        if (protoConnection == null) {
            refreshInventory(receiver);
        } else protoConnection.send((new MailPacket(receiver)));
    }

    private void refreshInventory(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (inventory.getHolder() instanceof MailInventory inv) inv.refresh();
    }
}
