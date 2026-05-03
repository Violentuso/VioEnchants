package com.violentuso.vioEnchants.main;

import com.violentuso.vioEnchants.VioEnchants;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum CustomEnchant {
    VAMPIRISM("vampirism", EnchantmentTarget.WEAPON),
    POISON("poison", EnchantmentTarget.WEAPON),
    EXPERIENCE("experience", EnchantmentTarget.TOOL),
    EXPLOSIVE("explosive", EnchantmentTarget.BOW),
    OXIDATION("oxidation", EnchantmentTarget.WEAPON),
    DETECTION("detection", EnchantmentTarget.WEAPON),
    SNIPER("sniper", EnchantmentTarget.BOW),
    JUMPER("jumper", EnchantmentTarget.ARMOR_FEET),
    SCOUT("scout", EnchantmentTarget.TRIDENT),
    STUPOR("stupor", EnchantmentTarget.TRIDENT),
    RECALL("recall", EnchantmentTarget.TRIDENT),
    LAVA_WALKER("lava_walker", EnchantmentTarget.ARMOR_FEET),
    BULLDOZER("bulldozer", EnchantmentTarget.TOOL),
    LUMBERJACK("lumberjack", EnchantmentTarget.TOOL),
    MEGA_BULLDOZER("mega_bulldozer", EnchantmentTarget.TOOL),
    UNSTABLE("unstable", EnchantmentTarget.BREAKABLE),
    WEB("web", EnchantmentTarget.TOOL),
    PINGER("pinger", EnchantmentTarget.TOOL),
    MAGNET("magnet", EnchantmentTarget.TOOL),
    ATTRACTION("attraction", EnchantmentTarget.TRIDENT),
    HEAVY("heavy", EnchantmentTarget.BREAKABLE),
    AUTOSMELT("autosmelt", EnchantmentTarget.TOOL);

    public final NamespacedKey KEY;
    public final EnchantmentTarget TARGET;
    public Set<Material> ALLOWED_MATERIALS = new HashSet<>();
    public boolean ENABLED;
    public String NAME;
    public int MAX_LEVEL;
    public int ANVIL_COST;
    public String PINGER_SOUND_NAME;
    public int PINGER_THRESHOLD;
    public int HEAVY_DURATION_BASE;
    public int HEAVY_DURATION_PER_LEVEL;
    public int HEAVY_AMPLIFIER;
    public boolean TABLE_ENABLED;
    public double TABLE_CHANCE_BASE;
    public double TABLE_CHANCE_PER_EXP;
    public Map<Integer, Double> LEVEL_CHANCES = new HashMap<>();
    public Map<Integer, Integer> LEVEL_THRESHOLDS = new HashMap<>();
    public double COMBAT_CHANCE_BASE;
    public double COMBAT_CHANCE_PER_LEVEL;
    public int EFFECT_DURATION_BASE;
    public int EFFECT_DURATION_PER_LEVEL;
    public int EFFECT_AMPLIFIER_DIVISOR;
    public Map<Integer, Integer> LAVA_WALKER_W = new HashMap<>();
    public Map<Integer, Integer> LAVA_WALKER_H = new HashMap<>();
    public Map<Integer, Integer> LAVA_WALKER_L = new HashMap<>();
    public int WEB_LIMIT;
    public List<String> WEB_WHITELIST;
    public String COLOR = "§7";
    public double EXP_MULTI_BASE;
    public double EXP_MULTI_PER_LEVEL;
    public double SCOUT_STRENGTH_BASE;
    public int JUMPER_AMPLIFIER;
    public double SCOUT_STRENGTH_PER_LEVEL;
    public double SCOUT_VERTICAL_BONUS;
    public double UNSTABLE_CHANCE_BASE;
    public double UNSTABLE_CHANCE_PER_LEVEL;
    public int UNSTABLE_EXTRA_DAMAGE_BASE;
    public int UNSTABLE_EXTRA_DAMAGE_PER_LEVEL;
    public double EXPLOSIVE_CHANCE_BASE;
    public double EXPLOSIVE_CHANCE_PER_LEVEL;
    public double EXPLOSIVE_RADIUS;
    public double EXPLOSIVE_DAMAGE_BASE;
    public double EXPLOSIVE_DAMAGE_PER_LEVEL;
    public double EXPLOSIVE_KNOCKBACK_BASE;
    public double EXPLOSIVE_KNOCKBACK_PER_LEVEL;
    public boolean EXPLOSIVE_FIRE;
    public boolean EXPLOSIVE_BLOCK_DAMAGE;
    public boolean EXPLOSIVE_PARTICLES_ENABLED;
    public Particle EXPLOSIVE_PARTICLE_TYPE;
    public int EXPLOSIVE_PARTICLE_COUNT;
    public int OXIDATION_DAMAGE_BASE;
    public int OXIDATION_DAMAGE_PER_LEVEL;
    public int BULLDOZER_RADIUS_BASE;
    public int BULLDOZER_RADIUS_PER_LEVEL;
    public int BULLDOZER_DEPTH_BASE;
    public int BULLDOZER_DEPTH_PER_LEVEL;
    public List<String> MEGA_BULLDOZER_BLOCKED_REGIONS = new ArrayList<>();
    public double SNIPER_VELOCITY_MULTI_BASE;
    public double SNIPER_VELOCITY_MULTI_PER_LEVEL;
    public boolean SNIPER_PARTICLES_ENABLED;
    public Particle SNIPER_PARTICLE_TYPE;
    public int SNIPER_PARTICLE_COUNT;
    public double ATTRACTION_STRENGTH_BASE;
    public double ATTRACTION_STRENGTH_PER_LEVEL;
    public double ATTRACTION_VERTICAL_BONUS;

    public Sound ACTIVATION_SOUND;
    public float ACTIVATION_SOUND_VOLUME;
    public float ACTIVATION_SOUND_PITCH;

    public static List<String> GLOBAL_BLOCKED_REGIONS = new ArrayList<>();
    public static List<String> GLOBAL_BLOCKED_FLAGS = new ArrayList<>();

    CustomEnchant(String key, EnchantmentTarget target) {
        this.KEY = new NamespacedKey(VioEnchants.INSTANCE, key);
        this.TARGET = target;
    }

    public static void updateConfigValues() {
        FileConfiguration config = VioEnchants.INSTANCE.getConfig();
        GLOBAL_BLOCKED_REGIONS = config.getStringList("global.worldguard.blocked-regions");
        GLOBAL_BLOCKED_FLAGS = config.getStringList("global.worldguard.blocked-flags");
        for (CustomEnchant enchant : values()) {
            String path = "enchants." + enchant.KEY.getKey();

            enchant.ENABLED = config.getBoolean(path + ".general.enabled", true);
            enchant.NAME = config.getString(path + ".general.name", enchant.name());
            enchant.MAX_LEVEL = config.getInt(path + ".general.max-level", 3);

            enchant.ALLOWED_MATERIALS.clear();
            List<String> matNames = config.getStringList(path + ".general.allowed-items");
            for (String matName : matNames) {
                Material mat = Material.matchMaterial(matName);
                if (mat != null) enchant.ALLOWED_MATERIALS.add(mat);
            }

            enchant.ANVIL_COST = config.getInt(path + ".anvil.cost-per-level", 1);
            enchant.TABLE_ENABLED = config.getBoolean(path + ".table.enabled", true);
            enchant.TABLE_CHANCE_BASE = config.getDouble(path + ".table.base-chance", 0.05);
            enchant.TABLE_CHANCE_PER_EXP = config.getDouble(path + ".table.chance-per-exp", 0.001);

            enchant.LEVEL_CHANCES.clear();
            enchant.LEVEL_THRESHOLDS.clear();
            ConfigurationSection levelsSection = config.getConfigurationSection(path + ".table.levels");
            if (levelsSection != null) {
                for (String key : levelsSection.getKeys(false)) {
                    try {
                        int lvl = Integer.parseInt(key);
                        enchant.LEVEL_CHANCES.put(lvl, levelsSection.getDouble(key + ".chance"));
                        enchant.LEVEL_THRESHOLDS.put(lvl, levelsSection.getInt(key + ".threshold"));
                    } catch (Exception ignored) {}
                }
            }
            if (enchant == BULLDOZER || enchant == LUMBERJACK) {
                enchant.BULLDOZER_RADIUS_BASE = config.getInt(path + ".mining.radius-base", 1);
                enchant.BULLDOZER_RADIUS_PER_LEVEL = config.getInt(path + ".mining.radius-per-level", 0);
                enchant.BULLDOZER_DEPTH_BASE = config.getInt(path + ".mining.depth-base", 1);
                enchant.BULLDOZER_DEPTH_PER_LEVEL = config.getInt(path + ".mining.depth-per-level", 0);
            }

            if (enchant == MEGA_BULLDOZER) {
                enchant.MEGA_BULLDOZER_BLOCKED_REGIONS = config.getStringList(path + ".restrictions.blocked-regions");
            }
            enchant.COMBAT_CHANCE_BASE = config.getDouble(path + ".combat.chance-base", 0.0);
            enchant.COMBAT_CHANCE_PER_LEVEL = config.getDouble(path + ".combat.chance-per-level", 0.0);
            enchant.EFFECT_DURATION_BASE = config.getInt(path + ".combat.effect.duration-base", 20);
            enchant.EFFECT_DURATION_PER_LEVEL = config.getInt(path + ".combat.effect.duration-per-level", 10);
            enchant.EFFECT_AMPLIFIER_DIVISOR = config.getInt(path + ".combat.effect.amplifier-divisor", 1);

            enchant.HEAVY_DURATION_BASE = config.getInt(path + ".effects.duration-base", 40);
            enchant.HEAVY_DURATION_PER_LEVEL = config.getInt(path + ".effects.duration-per-level", 20);
            enchant.HEAVY_AMPLIFIER = config.getInt(path + ".effects.amplifier", 0);

            enchant.EXP_MULTI_BASE = config.getDouble(path + ".bonus.multiplier-base", 0.0);
            enchant.EXP_MULTI_PER_LEVEL = config.getDouble(path + ".bonus.multiplier-per-level", 0.0);

            enchant.PINGER_SOUND_NAME = config.getString(path + ".pinger.sound", "BLOCK_NOTE_BLOCK_PLING");
            enchant.PINGER_THRESHOLD = config.getInt(path + ".pinger.threshold", 15);

            enchant.JUMPER_AMPLIFIER = config.getInt(path + ".effects.amplifier", 1);

            enchant.EXPLOSIVE_CHANCE_BASE = config.getDouble(path + ".explosion.chance-base", 0.3);
            enchant.EXPLOSIVE_CHANCE_PER_LEVEL = config.getDouble(path + ".explosion.chance-per-level", 0.1);
            enchant.EXPLOSIVE_RADIUS = config.getDouble(path + ".explosion.radius", 5.0);
            enchant.EXPLOSIVE_DAMAGE_BASE = config.getDouble(path + ".explosion.damage-base", 4.0);
            enchant.EXPLOSIVE_DAMAGE_PER_LEVEL = config.getDouble(path + ".explosion.damage-per-level", 2.0);
            enchant.EXPLOSIVE_KNOCKBACK_BASE = config.getDouble(path + ".explosion.knockback-base", 0.8);
            enchant.EXPLOSIVE_KNOCKBACK_PER_LEVEL = config.getDouble(path + ".explosion.knockback-per-level", 0.4);
            enchant.EXPLOSIVE_FIRE = config.getBoolean(path + ".explosion.fire", false);
            enchant.EXPLOSIVE_BLOCK_DAMAGE = config.getBoolean(path + ".explosion.break-blocks", false);
            enchant.EXPLOSIVE_PARTICLES_ENABLED = config.getBoolean(path + ".explosion.particles.enabled", true);
            if (enchant == LAVA_WALKER) {
                ConfigurationSection rangesSection = config.getConfigurationSection(path + ".ranges");
                if (rangesSection != null) {
                    for (String lvlKey : rangesSection.getKeys(false)) {
                        try {
                            int lvl = Integer.parseInt(lvlKey);
                            enchant.LAVA_WALKER_W.put(lvl, rangesSection.getInt(lvlKey + ".w", 3));
                            enchant.LAVA_WALKER_H.put(lvl, rangesSection.getInt(lvlKey + ".h", 1));
                            enchant.LAVA_WALKER_L.put(lvl, rangesSection.getInt(lvlKey + ".length", 1));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            String expPart = config.getString(path + ".explosion.particles.type", "EXPLOSION_LARGE");
            try {
                enchant.EXPLOSIVE_PARTICLE_TYPE = Particle.valueOf(expPart.toUpperCase());
            } catch (Exception e) { enchant.EXPLOSIVE_PARTICLE_TYPE = Particle.EXPLOSION_LARGE; }
            enchant.EXPLOSIVE_PARTICLE_COUNT = config.getInt(path + ".explosion.particles.count", 15);

            enchant.OXIDATION_DAMAGE_BASE = config.getInt(path + ".oxidation.damage-base", 2);
            enchant.OXIDATION_DAMAGE_PER_LEVEL = config.getInt(path + ".oxidation.damage-per-level", 2);
            enchant.COLOR = org.bukkit.ChatColor.translateAlternateColorCodes('&', config.getString(path + ".general.color", "&7"));
            enchant.WEB_LIMIT = config.getInt(path + ".vein-mine.limit", 64);
            enchant.WEB_WHITELIST = config.getStringList(path + ".vein-mine.whitelist");

            enchant.UNSTABLE_CHANCE_BASE = config.getDouble(path + ".durability.chance-base", 0.5);
            enchant.UNSTABLE_CHANCE_PER_LEVEL = config.getDouble(path + ".durability.chance-per-level", 0.1);
            enchant.UNSTABLE_EXTRA_DAMAGE_BASE = config.getInt(path + ".durability.extra-damage-base", 1);
            enchant.UNSTABLE_EXTRA_DAMAGE_PER_LEVEL = config.getInt(path + ".durability.extra-damage-per-level", 0);

            enchant.SNIPER_VELOCITY_MULTI_BASE = config.getDouble(path + ".velocity.multiplier-base", 1.3);
            enchant.SNIPER_VELOCITY_MULTI_PER_LEVEL = config.getDouble(path + ".velocity.multiplier-per-level", 0.2);
            enchant.SNIPER_PARTICLES_ENABLED = config.getBoolean(path + ".particles.enabled", true);
            String snipePart = config.getString(path + ".particles.type", "CRIT");
            try {
                enchant.SNIPER_PARTICLE_TYPE = Particle.valueOf(snipePart.toUpperCase());
            } catch (Exception e) { enchant.SNIPER_PARTICLE_TYPE = Particle.CRIT; }
            enchant.SNIPER_PARTICLE_COUNT = config.getInt(path + ".particles.count", 1);

            enchant.ATTRACTION_STRENGTH_BASE = config.getDouble(path + ".pull.strength-base", 0.5);
            enchant.ATTRACTION_STRENGTH_PER_LEVEL = config.getDouble(path + ".pull.strength-per-level", 0.3);
            enchant.ATTRACTION_VERTICAL_BONUS = config.getDouble(path + ".pull.vertical-bonus", 0.5);

            enchant.SCOUT_STRENGTH_BASE = config.getDouble(path + ".pull.strength-base", 1.0);
            enchant.SCOUT_STRENGTH_PER_LEVEL = config.getDouble(path + ".pull.strength-per-level", 0.5);
            enchant.SCOUT_VERTICAL_BONUS = config.getDouble(path + ".pull.vertical-bonus", 0.5);

            String soundStr = config.getString(path + ".sound.type", "");
            if (!soundStr.isEmpty()) {
                try {
                    enchant.ACTIVATION_SOUND = Sound.valueOf(soundStr.toUpperCase());
                } catch (Exception ignored) {}
            }
            enchant.ACTIVATION_SOUND_VOLUME = (float) config.getDouble(path + ".sound.volume", 1.0);
            enchant.ACTIVATION_SOUND_PITCH = (float) config.getDouble(path + ".sound.pitch", 1.0);
        }
    }

    public boolean canEnchant(ItemStack item) {
        if (item == null) return false;
        if (ALLOWED_MATERIALS.isEmpty()) {
            return TARGET.includes(item);
        }
        return ALLOWED_MATERIALS.contains(item.getType());
    }

    public String getLoreLine(int level) {
        if (this.MAX_LEVEL == 1) {
            return COLOR + NAME;
        }
        return COLOR + NAME + " " + toRoman(level);
    }

    public static String toRoman(int level) {
        if (level <= 0) return String.valueOf(level);
        String[] romans = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (level <= romans.length) ? romans[level - 1] : String.valueOf(level);
    }

    public static void applyGlow(ItemMeta meta) {
        if (!meta.hasEnchants()) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }
}