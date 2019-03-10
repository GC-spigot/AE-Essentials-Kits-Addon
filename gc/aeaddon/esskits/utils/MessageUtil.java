package gc.aeaddon.esskits.utils;

import gc.aeaddon.esskits.Core;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {

    public static void sendMessage(CommandSender p, String path, String... replaceables) {
        for(String line : Core.getInstance().getConfig().getStringList(path)) {
            line = line
                    .replace("%player%", p.getName());

            for(String s : replaceables) {
                String[] data = s.split(";");
                line = line.replace(data[0], data[1]);
            }

            p.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }
}
