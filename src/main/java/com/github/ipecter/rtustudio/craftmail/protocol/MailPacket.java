package com.github.ipecter.rtustudio.craftmail.protocol;

import kr.rtuserver.protoweaver.api.protocol.internal.GlobalPacket;

import java.util.UUID;

public record MailPacket(UUID receiver) implements GlobalPacket {
}
