# CraftMail

다중 서버 환경을 지원하는 보상형 메일(우편함) 플러그인. 일반 메시지뿐만 아니라 아이템, 경험치 등 다양한 커스텀 보상(Trigger)을 포함하여 메일을 전송할 수 있으며, `RSFramework`의 Bridge 기능을 통해 서버 간 알림을 동기화합니다.

> **의존성**: RSFramework

---

## 사용자 가이드

### 명령어

기본 명령어: `/mail` (별칭: `/우편`)

| 명령어 | 설명 | 권한 |
|---|---|---|
| `/mail` | 내 우편함을 엽니다 | 기본 |
| `/mail send <대상> <제목> <내용>` | 대상 플레이어에게 메일을 전송합니다 (사용 불가 시 `_`를 띄어쓰기로 사용) | `craftmail.admin` |

### 우편함 인터페이스

GUI 형태로 제공되며 페이지네이션과 일괄 읽기 기능을 지원합니다.
- 메일에 **아이템/경험치 보상**이 포함된 경우 보상 아이콘으로 표시되며 클릭 시 보상을 수령합니다.
- 일반 알림 메일은 **클릭 시 읽음 처리** 및 즉시 삭제되거나 상태가 변경됩니다.
- 한 번에 **페이지 단위**로 메일을 확인하고 관리할 수 있습니다.

### 설정 파일

#### Menu.yml — GUI 설정

| 항목 | 설명 |
|---|---|
| `line` | 우편함 GUI의 줄 수를 설정합니다 (기본 `6`) |
| `mail.reward` | 보상이 포함된 메일의 아이콘 (기본 `cyan_stained_glass`) |
| `mail.unread` | 읽지 않은 메일의 아이콘 (기본 `white_stained_glass`) |
| `mail.read` | 읽은 메일의 아이콘 (기본 `gray_stained_glass`) |

```yaml
icons: # 메뉴 하단 페이지네이션 및 작업 아이콘
actions: # 각 슬롯의 클릭 액션 (FIRST_PAGE, NEXT_PAGE, READ_ALL 등)
```

---

## 개발자 가이드

### 의존성 추가

```kotlin
repositories {
    maven {
        name = "RTUStudio"
        url = uri("https://repo.codemc.io/repository/rtustudio/")
    }
}

dependencies {
    compileOnly("kr.rtustudio:craftmail:1.0.0")
}
```

`plugin.yml`에 의존성을 추가하세요:
```yaml
depend:
  - CraftMail
```

### API 사용법

모든 API는 `MailAPI` 클래스의 static 메소드로 제공되며, 우편을 전송하거나 고유 보상 로직을 구성할 때 유용합니다.

#### 커스텀 트리거(보상) 등록

새로운 형태의 보상(예: 돈, 포인트 등)을 지급하는 커스텀 트리거를 등록할 수 있습니다.
클래스는 `Trigger` 인터페이스를 구현하고 Gson 직렬화가 가능해야 합니다.

```java
// 예시: MoneyTrigger 등록 (더미 값으로 type() 추출용 샘플 전달)
MailAPI.registerTrigger(new MoneyTrigger(0));
```

#### 메일 전송

```java
// 일반 메일 (텍스트 메시지만) 전송
MailAPI.sendMail(playerUuid, "공지", "서버 점검 안내입니다.");

// 트리거(보상)가 포함된 메일 전송
List<Trigger> rewards = List.of(
    new ItemTrigger(List.of(new ItemStack(Material.DIAMOND, 5))),
    new ExperienceTrigger(1000)
);
MailAPI.sendMail(playerUuid, "이벤트 보상", "이벤트 참여 보상입니다!", rewards);
```

#### 메일 조회 및 관리

```java
// 특정 플레이어의 읽지 않은 메일 개수 조회
int unreadCount = MailAPI.getUnreadCount(playerUuid);

// 플레이어의 모든 메일 목록 조회
List<Mail> mails = MailAPI.getMails(playerUuid);

// 모든 메일을 읽음 처리
MailAPI.markAllRead(playerUuid);

// 특정 메일 삭제
MailAPI.removeMail(mailUuid);
```

### 내부 아키텍처 (MailBridge)

`CraftMail`은 `RSFramework`의 `BridgeChannel`을 이용해 네트워크 내 모든 서버에서 우편 도착 알림(`unread-notify`, `mail-arrived`)을 송수신합니다.
- `MailBridge`: `craftmail:notify` 채널을 통해 메일 송신 시 로그인 중인 서버가 아닌 다른 서버에 접속해 있더라도 즉각적인 알림 메시지가 플레이어에게 전달됩니다.
- `TriggerRegistry`: JSON (Gson) 형태로 등록/역직렬화되므로 서버 재시작 시에도 커스텀 보상 유형이 유실되지 않습니다.
