package net.laboulangerie.townychat.commands.towny;

import net.laboulangerie.townychat.TownyChat;
import net.laboulangerie.townychat.player.ChatPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadTownyChatCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
                             @NotNull String[] args) {

        sender.sendMessage("§bReloading plugin");
        TownyChat.PLUGIN.reloadConfig();

        ChatPlayerManager chatPlayerManager = TownyChat.PLUGIN.getChatPlayerManager();
        Bukkit.getOnlinePlayers().forEach(
                p -> {
                    chatPlayerManager.unloadChatPlayer(p);
                    chatPlayerManager.loadChatPlayer(p);
                });

        sender.sendMessage("§aReload complete!");
        return false;
    }

}