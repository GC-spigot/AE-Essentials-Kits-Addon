package gc.aeaddon.esskits.handlers;

import gc.aeaddon.esskits.Core;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class KitsManager {

    private List<String> kits = new ArrayList<>();

    public KitsManager(JavaPlugin instance) {
        kits.addAll(instance.getConfig().getConfigurationSection("kits").getKeys(false));
        instance.getLogger().info("Loaded "+kits.size()+ " kits.");

    }

    public List<String> getKits() {
        return kits;
    }

    public List<String> getKitItems(String kit) {
        return Core.getInstance().getConfig().getStringList("kits."+kit+".items");
    }

    public long getCooldown(String kit) {
        return Core.getInstance().getConfig().getInt("kits."+kit+".delay")*1000L;
    }

    public String getCooldownMessage(UUID u, String kit) {
        long next_use = Core.getCooldownManager().getCooldown(u, kit);
        long next = (next_use - System.currentTimeMillis()) / 1000L;
        if(next < 0) {
            return "Available now.";
        }

        String format;
        if (next > 86400) {
            format = "dd'd' HH'h' mm'm' ss's'";
        } else if (next > 3600) {
            format = "HH'h' mm'm' ss's'";
        } else if (next > 60) {
            format = "mm'm' ss's'";
        } else {
            format = "ss's'";
        }
        return DurationFormatUtils.formatDuration(next * 1000L, format, false);
    }
}
