package kr.rtustudio.craftmail.trigger;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

import java.util.Map;

/**
 * 트리거 타입 레지스트리
 * <p>CraftMail 메인 클래스에서 인스턴스를 생성하며,
 * 외부 플러그인에서는 {@link kr.rtustudio.craftmail.MailAPI#registerTrigger(Trigger)}를 통해 등록합니다.</p>
 */
public class TriggerRegistry {

    @Getter
    private static TriggerRegistry instance;

    private final Map<String, Class<? extends Trigger>> registry = new Object2ObjectOpenHashMap<>();

    public TriggerRegistry() {
        instance = this;
    }

    /**
     * 트리거를 등록합니다.
     * 샘플 인스턴스의 {@link Trigger#type()}에서 타입 식별자를 추출합니다.
     *
     * @param sample 트리거 샘플 인스턴스 (type 추출용)
     */
    public void register(Trigger sample) {
        registry.put(sample.type().toLowerCase(), sample.getClass());
    }

    /**
     * 타입 식별자로 트리거 클래스를 조회합니다.
     *
     * @return 등록되지 않은 타입이면 null
     */
    public Class<? extends Trigger> resolve(String type) {
        return registry.get(type.toLowerCase());
    }
}
