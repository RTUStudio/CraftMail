package kr.rtustudio.craftmail.inventory;

import kr.rtustudio.craftmail.CraftMail;
import kr.rtustudio.craftmail.configuration.MenuConfig;
import kr.rtustudio.craftmail.configuration.MenuConfig.Action;
import kr.rtustudio.craftmail.configuration.MenuConfig.Icon;
import kr.rtustudio.craftmail.data.Mail;
import kr.rtustudio.craftmail.manager.MailManager;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.inventory.RSInventory;
import kr.rtustudio.framework.bukkit.api.registry.CustomItems;
import lombok.Getter;
import net.kyori.adventure.text.Component;
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

public class MailInventory extends RSInventory<CraftMail> {

    private final NamespacedKey key;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final MenuConfig config;

    private final Player player;
    private final MailManager manager;

    private final List<List<Mail>> partition = new ArrayList<>();

    @Getter
    private final Inventory inventory;

    private int page = 0;
    private int maxPage = 0;

    public MailInventory(CraftMail plugin, Player player) {
        super(plugin);
        this.key = new NamespacedKey(plugin, "uuid");
        this.config = plugin.getConfiguration(MenuConfig.class);
        this.manager = plugin.getMailManager();
        this.player = player;
        Component title = ComponentFormatter.mini(message.get(player, "gui.title"));
        int slots = config.getLine() * 9;
        this.inventory = createInventory(slots, title);
        loadMails();
        loadPage(0);
    }

    private void loadMails() {
        partition.clear();
        int itemSlots = (config.getLine() - 1) * 9;
        List<Mail> mails = new ArrayList<>(manager.get(player.getUniqueId()));
        for (int i = 0; i < mails.size(); i += itemSlots) {
            partition.add(mails.subList(i, Math.min(i + itemSlots, mails.size())));
        }
        maxPage = Math.max(partition.size() - 1, 0);
    }

    public void refresh() {
        loadMails();
        page = Math.min(page, maxPage);
        loadPage(page);
    }

    private List<Mail> page(int page) {
        if (partition.isEmpty()) return new ArrayList<>();
        return partition.get(page);
    }

    protected void loadPage(int page) {
        this.page = page;
        inventory.clear();
        loadIcons();
        for (Mail mail : page(page)) inventory.addItem(item(mail));
    }

    private void loadIcons() {
        for (Icon icon : config.getIcons()) {
            if (!shouldShowIcon(icon.state())) continue;
            if (icon.item() == null) continue;

            ItemStack item = CustomItems.from(icon.item());
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String baseKey = "menu.mail.placeholder." + icon.state().getKey().toUpperCase().replace("-", "_");
                String display = message.get(player, baseKey + ".title")
                        .replace("{current}", String.valueOf(page + 1))
                        .replace("{max}", String.valueOf(maxPage + 1));
                meta.displayName(ComponentFormatter.mini("<!italic>" + display));

                List<String> descList = message.getList(player, baseKey + ".description");
                if (descList != null && !descList.isEmpty()) {
                    List<Component> lore = new ArrayList<>();
                    for (String line : descList) {
                        String replaced = line
                                .replace("{current}", String.valueOf(page + 1))
                                .replace("{max}", String.valueOf(maxPage + 1));
                        lore.add(ComponentFormatter.mini("<!italic>" + replaced));
                    }
                    meta.lore(lore);
                }
                item.setItemMeta(meta);
            }
            inventory.setItem(icon.slot(), item);
        }
    }

    private boolean shouldShowIcon(Icon.State state) {
        return switch (state) {
            case FIRST_PAGE_AVAILABLE, PREVIOUS_PAGE_AVAILABLE -> page > 0;
            case FIRST_PAGE_UNAVAILABLE, PREVIOUS_PAGE_UNAVAILABLE -> page <= 0;
            case NEXT_PAGE_AVAILABLE, LAST_PAGE_AVAILABLE -> page < maxPage;
            case NEXT_PAGE_UNAVAILABLE, LAST_PAGE_UNAVAILABLE -> page >= maxPage;
        };
    }

    private ItemStack item(Mail mail) {
        MenuConfig.MailIcon.Type type;
        if (!mail.getTriggers().isEmpty()) type = MenuConfig.MailIcon.Type.REWARD;
        else if (mail.isRead()) type = MenuConfig.MailIcon.Type.READ;
        else type = MenuConfig.MailIcon.Type.UNREAD;

        ItemStack itemStack = CustomItems.from(config.getMail().get(type));
        if (itemStack == null) return null;
        itemStack = itemStack.clone();

        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, mail.getUuid().toString());
        String actionStr;
        if (mail.getTriggers().isEmpty()) {
            actionStr = message.get(player, "action." + (mail.isRead() ? "remove" : "unread"));
        } else {
            actionStr = message.get(player, "action.reward");
        }

        List<Component> lore = new ArrayList<>();
        List<String> formatList = message.getList(player, "format.mail");
        if (formatList != null && !formatList.isEmpty()) {
            String titleStr = formatList.get(0)
                    .replace("{title}", mail.getTitle())
                    .replace("{action}", actionStr)
                    .replace("{time}", format.format(mail.getDate()));
            meta.displayName(ComponentFormatter.mini("<!italic>" + titleStr));

            for (int i = 1; i < formatList.size(); i++) {
                String line = formatList.get(i);
                if (line.contains("{content}")) {
                    for (String part : mail.getContent().split("\n")) {
                        lore.add(ComponentFormatter.mini("<!italic>" + line.replace("{content}", part)));
                    }
                } else {
                    lore.add(ComponentFormatter.mini("<!italic>" + line
                            .replace("{title}", mail.getTitle())
                            .replace("{action}", actionStr)
                            .replace("{time}", format.format(mail.getDate()))));
                }
            }
        }
        meta.lore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }


    @Override
    public boolean onClick(Event<InventoryClickEvent> event, Click click) {
        if (event.isInventory()) return false;
        int slot = click.slot();
        int itemSlots = (config.getLine() - 1) * 9;

        Action action = config.getAction(slot, click.type());
        if (action != null) {
            handleAction(action.state());
            return false;
        }

        List<Mail> list = page(page);
        if (slot >= itemSlots || slot >= list.size()) return false;
        Mail mail = list.get(slot);

        if (mail.getTriggers().isEmpty()) {
            if (mail.isRead()) {
                manager.remove(mail.getUuid());
                list.remove(mail);
                inventory.setItem(slot, null);
            } else {
                mail.read();
                manager.interact(mail);
            }
        } else {
            if (!manager.claimTriggers(player, mail)) {
                notifier.announce(player, message.get(player, "gui.full"));
            }
        }
        loadPage(Math.min(page, maxPage));
        return false;
    }

    private void handleAction(Action.State state) {
        switch (state) {
            case FIRST_PAGE -> {
                if (page > 0) loadPage(0);
            }
            case PREVIOUS_PAGE -> {
                if (page > 0) loadPage(page - 1);
            }
            case NEXT_PAGE -> {
                if (page < maxPage) loadPage(page + 1);
            }
            case LAST_PAGE -> {
                if (page < maxPage) loadPage(maxPage);
            }
            case READ_ALL -> manager.readAll(player.getUniqueId());
        }
    }

    @Override
    public boolean onDrag(Event<InventoryDragEvent> event, Drag drag) {
        return false;
    }

    @Override
    public void onClose(Event<InventoryCloseEvent> event) {

    }
}
