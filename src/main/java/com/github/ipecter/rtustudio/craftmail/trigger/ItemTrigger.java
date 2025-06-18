package com.github.ipecter.rtustudio.craftmail.trigger;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import kr.rtuserver.framework.bukkit.api.registry.CustomItems;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonAdapter(ItemTrigger.GsonAdapter.class)
public record ItemTrigger(List<ItemStack> items) implements Trigger {

    public ItemTrigger(List<ItemStack> items) {
        this.items = new ArrayList<>(items);
    }

    @Override
    public String type() {
        return "item";
    }

    @Override
    public boolean execute(Player player) {
        items.removeIf(item -> giveItem(player, item));
        return items.isEmpty();
    }

    private boolean giveItem(Player player, ItemStack itemStack) {
        int maxStackSize = itemStack.getMaxStackSize();
        PlayerInventory inventory = player.getInventory();
        int slot = inventory.first(itemStack);
        if (inventory.firstEmpty() == -1 && slot != -1) {
            ItemStack item = inventory.getItem(slot);
            int all = itemStack.getAmount() + item.getAmount();
            if (all > maxStackSize) return false;
        }
        inventory.addItem(itemStack);
        return true;
    }

    public static class GsonAdapter extends TypeAdapter<ItemTrigger> {
        @Override
        public void write(JsonWriter out, ItemTrigger value) throws IOException {
            out.value(CustomItems.serializeArray(value.items.toArray(new ItemStack[0])));
        }

        @Override
        public ItemTrigger read(JsonReader in) throws IOException {
            String value = in.nextString();
            ItemStack[] items = CustomItems.deserializeArray(value);
            return new ItemTrigger(List.of(items));
        }
    }
}
