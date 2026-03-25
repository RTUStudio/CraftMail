package kr.rtustudio.craftmail.trigger;

import kr.rtustudio.craftmail.trigger.adapter.TriggerTypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import org.bukkit.entity.Player;

@JsonAdapter(TriggerTypeAdapter.class)
public interface Trigger {

    String type();
    boolean execute(Player player);

}
