package net.laboulangerie.townychat.commands;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Translatable;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.laboulangerie.townychat.TownyChat;
import net.laboulangerie.townychat.player.ChatPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpyCommand implements CommandExecutor {

    public SpyCommand() {
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
                             @NotNull String[] args) {
        FileConfiguration config = TownyChat.PLUGIN.getConfig();

        if (!(sender instanceof Player player)) {
            String errorMessage = config.getString("lang.err_sender_not_player");
            sender.sendMessage(MiniMessage.miniMessage().deserialize(errorMessage));
            return true;
        }

        if (!(player.hasPermission("townychat.spy"))) {
            TownyMessaging.sendErrorMsg(sender, Translatable.of("msg_err_command_disable"));
        }

        ChatPlayer chatPlayer = TownyChat.PLUGIN.getChatPlayerManager().getChatPlayer(player);
        chatPlayer.setSpying(!(chatPlayer.isSpying()));

        String enabled = config.getString("lang.enabled");
        String disabled = config.getString("lang.disabled");
        String toggledSpyMessage = config.getString("lang.spying_toggled");

        TextComponent messageComponent = (TextComponent) MiniMessage.miniMessage().deserialize(toggledSpyMessage,
                Placeholder.unparsed("status", chatPlayer.isSpying() ? enabled : disabled));

        TownyMessaging.sendMsg(player, messageComponent.content());

        return true;
    }
}
