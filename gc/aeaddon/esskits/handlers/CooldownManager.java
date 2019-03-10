package gc.aeaddon.esskits.handlers;

import gc.aeaddon.esskits.Core;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CooldownManager {

    private File file;
    private FileConfiguration fileConfiguration = new YamlConfiguration();

    private long autosaveTime = 20 * 60 * 15;

    public CooldownManager(JavaPlugin instance) {
        file = new File(instance.getDataFolder(), "cooldown.yml");

        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ev) {
                ev.printStackTrace();
            }
        }

        try {
            fileConfiguration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                save();
            }
        }.runTaskTimerAsynchronously(Core.getInstance(), autosaveTime, autosaveTime);
    }

    public void save() {
        try {
            fileConfiguration.save(file);
        } catch (IOException ev) {
            ev.printStackTrace();
        }
    }

    public void setCooldown(UUID u, String kit, long cooldown) {
        String path = u.toString() +"." + kit + ".cooldown";
        fileConfiguration.set(path, cooldown);
    }

    public boolean canUse(UUID u, String kit) {
        String path = u.toString() +"." + kit + ".cooldown";
        if(!fileConfiguration.contains(path))
            return true;

        long cooldown = fileConfiguration.getLong(path);
        return System.currentTimeMillis() - cooldown > 0;
    }

    public long getCooldown(UUID u, String kit) {
        String path = u.toString() +"." + kit + ".cooldown";
        return fileConfiguration.getLong(path);
    }
}
