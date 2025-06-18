package com.github.ipecter.rtustudio.craftmail.configuration;

import com.github.ipecter.rtustudio.craftmail.CraftMail;
import kr.rtuserver.framework.bukkit.api.configuration.RSConfiguration;
import kr.rtuserver.framework.bukkit.api.format.ComponentFormatter;
import kr.rtuserver.framework.bukkit.api.registry.CustomItems;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

@Getter
public class IconConfig extends RSConfiguration<CraftMail> {

    private final Map<String, ItemStack> cache = new HashMap<>();

    public IconConfig(CraftMail plugin) {
        super(plugin, "Icon.yml", null);
        setup(this);
    }

    public ItemStack get(String id) {
        return cache.getOrDefault(id, new ItemStack(Material.BARRIER));
    }

    public ItemStack get(String id, String lore) {
        ItemStack item = get(id);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ComponentFormatter.mini("<!italic><white>" + lore));
        item.setItemMeta(meta);
        return item;
    }

    private void init() {
        cache.clear();
        for (String key : getConfig().getKeys(true)) {
            String value = getConfig().getString(key);
            if (value.isEmpty()) continue;
            ItemStack itemStack = CustomItems.from(value);
            if (itemStack == null) continue;
            cache.put(key, itemStack);
        }
    }

}
