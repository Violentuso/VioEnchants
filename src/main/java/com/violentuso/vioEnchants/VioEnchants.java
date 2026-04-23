package com.violentuso.vioEnchants;

import com.violentuso.vioEnchants.commands.VioCommand;
import com.violentuso.vioEnchants.listeners.*;
import com.violentuso.vioEnchants.main.CustomEnchant;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class VioEnchants extends JavaPlugin {

    public static VioEnchants INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        checkAndUpdateConfig();

        CustomEnchant.updateConfigValues();
        getServer().getPluginManager().registerEvents(new BulldozerListener(), this);
        getServer().getPluginManager().registerEvents(new WebListener(), this);
        getServer().getPluginManager().registerEvents(new AutoSmeltListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantTableListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosiveListener(), this);
        getServer().getPluginManager().registerEvents(new ExperienceListener(), this);
        getServer().getPluginManager().registerEvents(new SniperListener(), this);
        getServer().getPluginManager().registerEvents(new AttractionListener(), this);
        getServer().getPluginManager().registerEvents(new ScoutListener(), this);
        getServer().getPluginManager().registerEvents(new MagnetListener(), this);
        getServer().getPluginManager().registerEvents(new PingerListener(), this);
        getServer().getPluginManager().registerEvents(new UnstableListener(), this);
        getServer().getPluginManager().registerEvents(new HeavyListener(), this);
        getServer().getPluginManager().registerEvents(new LavaWalkerListener(), this);
        getServer().getPluginManager().registerEvents(new GrindstoneListener(), this);

        VioCommand cmd = new VioCommand();
        getCommand("vioenchant").setExecutor(cmd);
        getCommand("vioenchant").setTabCompleter(cmd);

        getServer().getScheduler().runTaskTimer(this, () -> {
            if (!CustomEnchant.JUMPER.ENABLED) return;

            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                org.bukkit.inventory.ItemStack boots = player.getInventory().getBoots();

                if (boots != null && boots.hasItemMeta()) {
                    org.bukkit.persistence.PersistentDataContainer pdc = boots.getItemMeta().getPersistentDataContainer();

                    if (pdc.has(CustomEnchant.JUMPER.KEY, org.bukkit.persistence.PersistentDataType.INTEGER)) {
                        if (com.violentuso.vioEnchants.util.RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) continue;
                        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.JUMP,
                                40,
                                CustomEnchant.JUMPER.JUMPER_AMPLIFIER,
                                true,
                                false,
                                true
                        ));
                    }
                }
            }
        }, 0L, 20L);
    }

    @Override
    public void onDisable() {
    }

    public void checkAndUpdateConfig() {
        FileConfiguration config = getConfig();
        InputStream defConfigStream = getResource("config.yml");

        if (defConfigStream == null) return;
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
        boolean changesMade = false;
        for (String key : defConfig.getKeys(true)) {
            if (!config.contains(key)) {
                config.set(key, defConfig.get(key));
                changesMade = true;
            }
        }
        if (changesMade) {
            saveConfig();
            getLogger().info("Конфигурация была обновлена новыми значениями!");
        }
    }
}