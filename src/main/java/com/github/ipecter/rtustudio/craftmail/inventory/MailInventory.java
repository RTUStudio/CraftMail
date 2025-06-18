package com.github.ipecter.rtustudio.craftmail.inventory;

import com.github.ipecter.rtustudio.craftmail.CraftMail;
import com.github.ipecter.rtustudio.craftmail.configuration.IconConfig;
import com.github.ipecter.rtustudio.craftmail.data.Mail;
import com.github.ipecter.rtustudio.craftmail.manager.MailManager;
import kr.rtuserver.framework.bukkit.api.format.ComponentFormatter;
import kr.rtuserver.framework.bukkit.api.inventory.RSInventory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.apache.commons.collections4.ListUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public class MailInventory extends RSInventory<CraftMail> {

    private final NamespacedKey key;
    private final SimpleDateFormat format = new SimpleDateFormat("M월 d일 | a h시 m분");
    private final IconConfig iconConfig;

    private final Player player;
    private final MailManager manager;

    private final List<Mail> data = new ArrayList<>();

    @Getter
    private final Inventory inventory;

    private int page = 0;
    private int maxPage = 0;

    public MailInventory(CraftMail plugin, Player player) {
        super(plugin);
        this.key = new NamespacedKey(plugin, "uuid");
        this.iconConfig = plugin.getIconConfig();
        this.manager = plugin.getMailManager();
        this.player = player;
        Component title = ComponentFormatter.mini(message().get(player, "title"));
        this.inventory = createInventory(54, title);

        data.addAll(manager.get(player.getUniqueId()));
        this.maxPage = Math.max(partition().size() - 1, 0);
        loadPage(0);
    }

    private List<List<Mail>> partition() {
        if (data.isEmpty()) return new ArrayList<>();
        return ListUtils.partition(data, 45);
    }

    public void refresh() {
        this.data.clear();
        this.data.addAll(manager.get(player.getUniqueId()));
        this.maxPage = Math.max(partition().size() - 1, 0);
        this.page = Math.min(page, maxPage);
        loadPage(page);
    }

    private List<Mail> page(int page) {
        List<List<Mail>> partition = partition();
        if (partition.isEmpty()) return new ArrayList<>();
        else return partition.get(page);
    }

    protected void loadPage(int page) {
        this.page = page;
        inventory.clear();
        inventory.setItem(45, pageIcon(Navigation.FIRST));
        inventory.setItem(46, pageIcon(Navigation.PREVIOUS));
        inventory.setItem(52, pageIcon(Navigation.NEXT));
        inventory.setItem(53, pageIcon(Navigation.LAST));
        for (Mail mail : page(page)) inventory.addItem(item(mail));
    }

    private ItemStack pageIcon(Navigation navigation) {
        String name = navigation.name().toLowerCase();
        String available = navigation.check(page, maxPage) ? "available" : "unavailable";
        String display = message().get(player, "icon.menu.pagination." + name + "." + available);
        display = display.replace("[current]", String.valueOf(page + 1)).replace("[max]", String.valueOf(maxPage + 1));
        return iconConfig.get("menu.pagination." + name + "." + available, display);
    }

    protected void loadPage(Navigation navigation) {
        if (!navigation.check(page, maxPage)) return;
        switch (navigation) {
            case FIRST -> loadPage(0);
            case PREVIOUS -> loadPage(page - 1);
            case NEXT -> loadPage(page + 1);
            case LAST -> loadPage(maxPage);
        }
    }

    private ItemStack item(Mail mail) {
        ItemStack itemStack = iconConfig.get("mail." + (mail.getTriggers().isEmpty() ? (mail.isRead() ? "read" : "unread") : "reward"));
        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, mail.getUuid().toString());
        meta.displayName(ComponentFormatter.mini("<!italic><white>" + mail.getTitle()));
        List<Component> lore = new ArrayList<>(toComponents(mail.getContent()));
        lore.add(Component.empty());
        String icon;
        if (mail.getTriggers().isEmpty()) {
            if (mail.isRead()) icon = "remove";
            else icon = "unread";
        } else icon = "reward";
        lore.add(ComponentFormatter.mini("<!italic><white>" + message().get(player, "icon.mail.click") + message().get(player, "icon.mail." + icon)));
        lore.add(Component.empty());
        lore.add(ComponentFormatter.mini("<!italic><gray>" + format.format(mail.getDate())));
        meta.lore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private List<Component> toComponents(String message) {
        List<Component> result = new ArrayList<>();
        String[] split = message.split("\n");
        for (String str : split) result.add(ComponentFormatter.mini("<!italic><white>" + str));
        return result;
    }

    @Override
    public boolean onClick(Event<InventoryClickEvent> event, Click click) {
        if (event.isInventory()) return false;
        int slot = click.slot();
        switch (slot) {
            case 45 -> loadPage(Navigation.FIRST);
            case 46 -> loadPage(Navigation.PREVIOUS);
            case 49 -> manager.readAll(player.getUniqueId());
            case 52 -> loadPage(Navigation.NEXT);
            case 53 -> loadPage(Navigation.LAST);
            default -> {
                List<Mail> list = page(page);
                if (list.size() <= slot) return false;
                Mail mail = list.get(slot);
                if (mail.getTriggers().isEmpty()) {
                    if (mail.isRead()) {
                        manager.remove(mail.getUuid());
                        list.remove(mail);
                        inventory.setItem(slot, null);
                    } else manager.interact(mail);
                } else {
                    mail.getTriggers().removeIf(trigger -> trigger.execute(player));
                    if (mail.getTriggers().isEmpty()) mail.read();
                    else chat().announce(message().get(player, ""));
                    manager.interact(mail);
                }
                loadPage(Math.min(page, maxPage));
            }
        }
        return false;
    }

    @Override
    public boolean onDrag(Event<InventoryDragEvent> event, Drag drag) {
        return true;
    }

    @Override
    public void onClose(Event<InventoryCloseEvent> event) {

    }

    @RequiredArgsConstructor
    protected enum Navigation {
        FIRST((page, maxPage) -> page != 0),
        PREVIOUS((page, maxPage) -> page > 0),
        NEXT((page, maxPage) -> page < maxPage),
        LAST((page, maxPage) -> !Objects.equals(page, maxPage));

        private final BiPredicate<Integer, Integer> condition;

        public boolean check(int page, int maxPage) {
            return condition.test(page, maxPage);
        }
    }


}
