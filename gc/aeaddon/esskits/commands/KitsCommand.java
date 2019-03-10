package gc.aeaddon.esskits.commands;

import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.craftbukkit.InventoryWorkaround;
import gc.aeaddon.esskits.Core;
import gc.aeaddon.esskits.utils.MessageUtil;
import n3kas.ae.api.AEAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Code bits used from https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/com/earth2me/essentials/Kit.java
 *  repository to create essentials-like kits configuration.
 *
 *  Thanks to drtshock and others for maintaining EssentialsX.
 */

public class KitsCommand implements CommandExecutor {

    private String p_exemptPermission = "essentials.kit.exemptdelay";
    private String p_giveOthersPermission = "essentials.kit.others";
    private String p_kitPermission = "essentials.kits.";

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) {
            StringBuilder sb = new StringBuilder();

            Core.getKitsManager().getKits().stream()
                    .filter(perm -> sender.hasPermission(p_kitPermission + perm))
                    .forEach(kit -> sb.append(kit + ", "));

            if (sb.length() == 0) {
                sb.append(ChatColor.DARK_RED + "No kits available.");
            }

            MessageUtil.sendMessage(sender, "messages.kit-list", "%list%;" + sb.toString());
            return true;
        }

        String arg = args[0];
        Player target = args.length > 1 ? Bukkit.getPlayer(args[1]) :
                !(sender instanceof Player) ? null : ((Player) sender);

        if (target == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cError: &4Invalid Player."));
            return true;
        }

        List<String> items = Core.getKitsManager().getKitItems(arg);
        if (items == null || items.isEmpty()) {
            MessageUtil.sendMessage(sender, "messages.kit-not-found");
            return true;
        }

        if (!target.getName().equalsIgnoreCase(sender.getName()) && !sender.hasPermission(p_giveOthersPermission)) {
            MessageUtil.sendMessage(sender, "messages.no-permission-to-give-others");
            return true;
        }

        if(target.getName().equalsIgnoreCase(sender.getName())) {
            if (!target.hasPermission(p_kitPermission + arg)) {
                MessageUtil.sendMessage(sender, "messages.no-permission", "%kit%;" + arg);
                return true;
            }

            if (!target.hasPermission(p_exemptPermission)) {
                if (!Core.getCooldownManager().canUse(target.getUniqueId(), arg)) {
                    MessageUtil.sendMessage(target, "messages.kit-cooldown",
                            "%time%;"+Core.getKitsManager().getCooldownMessage(target.getUniqueId(), arg));
                    return true;
                }
            }
        }

        try {
            boolean spew = false;
            for (String kitItem : items) {
                if (kitItem.startsWith("/")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            kitItem.substring(1).replace("{player}", target.getName()));
                    continue;
                }

                String[] data = kitItem.split(" ");
                HashMap<String, Integer> customEnchants = new HashMap<>();
                for(String item : data) {
                    String[] enchant = item.split(":");
                    if(enchant.length == 1)
                        continue;
                    String ae = enchant[0];
                    if(!isEnchant(ae))
                        continue;

                    customEnchants.put(ae,
                            Integer.parseInt(enchant[1]
                                    .replaceAll("[^0-9]", "")));
                    kitItem = kitItem.replace(item, "");
                }

                final String[] parts = kitItem.split(" +");
                final ItemStack parseStack = Core.getEss().getItemDb().get(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 1);

                if (parseStack.getType() == Material.AIR) {
                    continue;
                }

                MetaItemStack metaStack = new MetaItemStack(parseStack);

                if (parts.length > 2) {
                    metaStack.parseStringMeta(null, true, parts, 2, Core.getEss());
                }
                ItemStack item = metaStack.getItemStack();
                if(!customEnchants.isEmpty()) {
                    for(Map.Entry<String, Integer> enchant : customEnchants.entrySet()) {
                        item = AEAPI.applyEnchant(enchant.getKey(), enchant.getValue(), item);
                    }
                }

                final Map<Integer, ItemStack> overfilled;
                overfilled = InventoryWorkaround.addItems(target.getInventory(), item);

                for (ItemStack itemStack : overfilled.values()) {
                    int spillAmount = itemStack.getAmount();
                    while (spillAmount > 0) {
                        target.getWorld().dropItemNaturally(target.getLocation(), itemStack);
                        spillAmount -= itemStack.getAmount();
                    }

                    spew = true;
                }
            }
            if(spew) {
                MessageUtil.sendMessage(target, "messages.inv-full");
            }

            MessageUtil.sendMessage(target, "messages.kit-received", "%kit%;"+arg);
            Core.getCooldownManager().setCooldown(target.getUniqueId(), arg, System.currentTimeMillis()+Core.getKitsManager().getCooldown(arg));

            target.updateInventory();
        } catch (Exception ev) {
            ev.printStackTrace();
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', "Kit has configuration issues, contact administrator."));
        }


        return true;
    }
    private boolean isEnchant(String ench) {
        return AEAPI.getAllEnchantments().contains(ench.toLowerCase());
    }
}
