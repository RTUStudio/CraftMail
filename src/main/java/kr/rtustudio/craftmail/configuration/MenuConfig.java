package kr.rtustudio.craftmail.configuration;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import kr.rtustudio.configurate.model.ConfigurationPart;
import kr.rtustudio.configurate.objectmapping.ConfigSerializable;
import kr.rtustudio.configurate.objectmapping.meta.PostProcess;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j(topic = "CraftMail")
@Getter
@SuppressWarnings({"unused", "CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "InnerClassMayBeStatic"})
public class MenuConfig extends ConfigurationPart {

    private int line = 6;

    private MailIcon mail;

    private List<Icon> icons = List.of(
            new Icon(Icon.State.FIRST_PAGE_AVAILABLE, 45, "minecraft:nether_star"),
            new Icon(Icon.State.FIRST_PAGE_UNAVAILABLE, 45, "minecraft:red_wool"),
            new Icon(Icon.State.PREVIOUS_PAGE_AVAILABLE, 46, "minecraft:nether_star"),
            new Icon(Icon.State.PREVIOUS_PAGE_UNAVAILABLE, 46, "minecraft:red_wool"),
            new Icon(Icon.State.NEXT_PAGE_AVAILABLE, 52, "minecraft:nether_star"),
            new Icon(Icon.State.NEXT_PAGE_UNAVAILABLE, 52, "minecraft:red_wool"),
            new Icon(Icon.State.LAST_PAGE_AVAILABLE, 53, "minecraft:nether_star"),
            new Icon(Icon.State.LAST_PAGE_UNAVAILABLE, 53, "minecraft:red_wool")
    );

    private List<Action> actions = List.of(
            new Action(Action.State.FIRST_PAGE, 45, List.of(ClickType.LEFT)),
            new Action(Action.State.PREVIOUS_PAGE, 46, List.of(ClickType.LEFT)),
            new Action(Action.State.READ_ALL, 49, List.of(ClickType.LEFT)),
            new Action(Action.State.NEXT_PAGE, 52, List.of(ClickType.LEFT)),
            new Action(Action.State.LAST_PAGE, 53, List.of(ClickType.LEFT))
    );

    private transient Set<Integer> disabledSlots = new ObjectOpenHashSet<>();

    @PostProcess
    public void check() {
        disabledSlots.clear();
        Map<Integer, Map<ClickType, Action.State>> slotClickMap = new Object2ObjectOpenHashMap<>();

        for (Action action : actions) {
            int slot = action.slot();
            Map<ClickType, Action.State> clickMap = slotClickMap.computeIfAbsent(slot, k -> new Object2ObjectOpenHashMap<>());

            for (ClickType click : action.clickTypes()) {
                if (clickMap.containsKey(click)) {
                    log.warn("[MenuConfig] Duplicate slot/clickType: slot={}, click={} (states: {}, {}). Disabling slot.",
                            slot, click, clickMap.get(click), action.state());
                    disabledSlots.add(slot);
                } else {
                    clickMap.put(click, action.state());
                }
            }
        }
    }

    @Nullable
    public Action getAction(int slot, ClickType clickType) {
        if (disabledSlots.contains(slot)) return null;
        for (Action action : actions) {
            if (action.slot() == slot && (action.clickTypes().isEmpty() || action.clickTypes().contains(clickType))) {
                return action;
            }
        }
        return null;
    }

    @ConfigSerializable
    public class MailIcon {
        private String reward = "minecraft:cyan_stained_glass";
        private String unread = "minecraft:white_stained_glass";
        private String read = "minecraft:gray_stained_glass";

        public String get(Type type) {
            return switch (type) {
                case REWARD -> reward;
                case UNREAD -> unread;
                case READ -> read;
            };
        }

        public enum Type {
            REWARD, UNREAD, READ
        }
    }

    @ConfigSerializable
    public record Action(@NotNull State state, int slot, @NotNull List<ClickType> clickTypes) {
        public enum State {
            FIRST_PAGE,
            PREVIOUS_PAGE,
            NEXT_PAGE,
            LAST_PAGE,
            READ_ALL;

            public String getKey() {
                return name().toLowerCase().replace('_', '-');
            }
        }
    }

    @ConfigSerializable
    public record Icon(@NotNull State state, int slot, @Nullable String item) {
        public enum State {
            FIRST_PAGE_AVAILABLE,
            FIRST_PAGE_UNAVAILABLE,
            NEXT_PAGE_AVAILABLE,
            NEXT_PAGE_UNAVAILABLE,
            PREVIOUS_PAGE_AVAILABLE,
            PREVIOUS_PAGE_UNAVAILABLE,
            LAST_PAGE_AVAILABLE,
            LAST_PAGE_UNAVAILABLE;

            public String getKey() {
                return name().toLowerCase().replace('_', '-');
            }
        }
    }
}
