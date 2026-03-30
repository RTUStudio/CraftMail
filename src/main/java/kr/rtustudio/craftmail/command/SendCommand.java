package kr.rtustudio.craftmail.command;

import kr.rtustudio.craftmail.CraftMail;
import kr.rtustudio.craftmail.data.Mail;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.core.provider.ProviderRegistry;
import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import java.util.List;
import java.util.UUID;

public class SendCommand extends RSCommand<CraftMail> {

    private final ProviderRegistry providerRegistry;

    public SendCommand(CraftMail plugin) {
        super(plugin, "send", PermissionDefault.OP);
        this.providerRegistry = framework.getProviderRegistry();
    }

    @Override
    protected Result execute(CommandArgs data) {
        if (data.length() < 3) return Result.WRONG_USAGE;
        Player sender = player();
        if (sender == null) return Result.ONLY_PLAYER;

        String[] args = data.args();
        String targetName = args[1];

        NameProvider nameProvider = providerRegistry.get(NameProvider.class);
        UUID targetId = null;
        String resolvedName = targetName;

        if (nameProvider != null) {
            targetId = nameProvider.getUniqueId(targetName);
            if (targetId != null) {
                String actualName = nameProvider.getName(targetId);
                if (actualName != null) resolvedName = actualName;
            }
        }

        if (targetId == null) {
            Player onlineTarget = Bukkit.getPlayer(targetName);
            if (onlineTarget != null) {
                targetId = onlineTarget.getUniqueId();
                resolvedName = onlineTarget.getName();
            }
        }

        if (targetId == null) {
            notifier.announce(sender, message.get(sender, "send.not-found")
                    .replace("{player}", targetName));
            return Result.FAILURE;
        }

        String title = args[2];
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            if (i > 3) contentBuilder.append(" ");
            contentBuilder.append(args[i]);
        }
        String content = contentBuilder.toString();

        Mail mail = new Mail(sender.getUniqueId(), targetId, title, content);
        if (plugin.getMailManager().add(mail)) {
            notifier.announce(sender, message.get(sender, "send.success")
                    .replace("{player}", resolvedName)
                    .replace("{title}", title));
            return Result.SUCCESS;
        }
        return Result.FAILURE;
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        String[] args = data.args();
        NameProvider provider = providerRegistry.get(NameProvider.class);
        return switch (args.length) {
            case 2 -> provider != null ? provider.names(NameProvider.Scope.GLOBAL) : List.of();
            case 3 -> List.of("<제목>");
            default -> List.of("<내용>");
        };
    }
}
