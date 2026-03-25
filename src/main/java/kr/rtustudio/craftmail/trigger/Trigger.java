package kr.rtustudio.craftmail.trigger;

import com.google.gson.annotations.JsonAdapter;
import kr.rtustudio.craftmail.trigger.adapter.TriggerTypeAdapter;
import org.bukkit.entity.Player;

@JsonAdapter(TriggerTypeAdapter.class)
public interface Trigger {

    String type();

    boolean execute(Player player);

}
