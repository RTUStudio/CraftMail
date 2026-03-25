package kr.rtustudio.craftmail.command;

import kr.rtustudio.craftmail.CraftMail;
import kr.rtustudio.craftmail.data.Mail;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class SendCommand extends RSCommand<CraftMail> {

    public SendCommand(CraftMail plugin) {
        super(plugin, "send", PermissionDefault.OP);
    }

    @Override
    protected Result execute(CommandArgs data) {
        if (!data.length(3)) return Result.WRONG_USAGE;
        Player sender = player();
        if (sender == null) return Result.ONLY_PLAYER;

        String[] args = data.args();
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
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

        Mail mail = new Mail(sender.getUniqueId(), target.getUniqueId(), title, content);
        if (plugin.getMailManager().add(mail)) {
            notifier.announce(sender, message.get(sender, "send.success")
                    .replace("{player}", target.getName())
                    .replace("{title}", title));
            return Result.SUCCESS;
        }
        return Result.FAILURE;
    }

    @Override
    protected List<String> tabComplete(CommandArgs data) {
        String[] args = data.args();
        return switch (args.length) {
            case 2 -> null;
            case 3 -> List.of("<제목>");
            default -> List.of("<내용>");
        };
    }
}
