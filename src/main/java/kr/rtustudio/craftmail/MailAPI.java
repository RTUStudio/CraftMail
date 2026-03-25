package kr.rtustudio.craftmail;

import kr.rtustudio.craftmail.data.Mail;
import kr.rtustudio.craftmail.trigger.Trigger;
import kr.rtustudio.craftmail.trigger.TriggerRegistry;

import java.util.List;
import java.util.UUID;

/**
 * 외부 플러그인에서 CraftMail 기능에 접근하기 위한 API
 * <pre>
 * // 커스텀 트리거 등록
 * MailAPI.registerTrigger(new MoneyTrigger(0));
 *
 * // 트리거 포함 메일 전송
 * MailAPI.sendMail(uuid, "보상", "내용", List.of(new MoneyTrigger(1000)));
 *
 * // 일반 메일 전송
 * MailAPI.sendMail(uuid, "공지", "서버 점검 안내");
 * </pre>
 */
public class MailAPI {

    private static CraftMail plugin;

    private static CraftMail plugin() {
        if (plugin == null) plugin = CraftMail.getInstance();
        return plugin;
    }

    // ==================== 트리거 등록 ====================

    /**
     * 커스텀 트리거 타입을 등록합니다.
     * <p>샘플 인스턴스의 {@link Trigger#type()}에서 타입 식별자를 자동 추출합니다.
     * 구현 클래스는 {@link Trigger} 인터페이스를 구현하고,
     * Gson {@code @JsonAdapter}를 통해 직렬화/역직렬화가 가능해야 합니다.</p>
     *
     * @param sample 트리거 샘플 인스턴스 (type 추출용, 더미 값 사용 가능)
     */
    public static void registerTrigger(Trigger sample) {
        TriggerRegistry.getInstance().register(sample);
    }

    // ==================== 메일 전송 ====================

    /**
     * 일반 메일을 전송합니다.
     */
    public static void sendMail(UUID receiver, String title, String content) {
        Mail mail = new Mail(receiver, title, content);
        plugin().getMailManager().add(mail);
    }

    /**
     * 트리거(보상) 포함 메일을 전송합니다.
     */
    public static void sendMail(UUID receiver, String title, String content, List<Trigger> triggers) {
        Mail mail = new Mail(receiver, title, content, triggers);
        plugin().getMailManager().add(mail);
    }

    // ==================== 메일 조회 ====================

    /**
     * 특정 플레이어의 모든 메일을 조회합니다.
     */
    public static List<Mail> getMails(UUID player) {
        return plugin().getMailManager().get(player);
    }

    /**
     * 읽지 않은 메일 수를 반환합니다.
     */
    public static int getUnreadCount(UUID player) {
        return plugin().getMailManager().getUnreadCount(player);
    }

    // ==================== 메일 관리 ====================

    /**
     * 특정 메일을 삭제합니다.
     */
    public static void removeMail(UUID uuid) {
        plugin().getMailManager().remove(uuid);
    }

    /**
     * 특정 플레이어의 모든 메일을 읽음 처리합니다.
     */
    public static void markAllRead(UUID player) {
        plugin().getMailManager().readAll(player);
    }

}
