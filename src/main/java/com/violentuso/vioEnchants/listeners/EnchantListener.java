package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Random;

public class EnchantListener implements Listener {
    private final Random random = new Random();
    @EventHandler
    public void onTridentHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player player)) return;
        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
        ItemStack item = trident.getItem();
        if (!item.hasItemMeta()) return;
        if (!item.containsEnchantment(Enchantment.LOYALTY)) return;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if (pdc.has(CustomEnchant.RECALL.KEY, PersistentDataType.INTEGER)) {

            int level = pdc.get(CustomEnchant.RECALL.KEY, PersistentDataType.INTEGER);
            CustomEnchant enchant = CustomEnchant.RECALL;

            double chance = enchant.COMBAT_CHANCE_BASE + (enchant.COMBAT_CHANCE_PER_LEVEL * level);
            if (random.nextDouble() <= chance) {
                trident.remove();
                HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(item);
                if (!leftOver.isEmpty()) {
                    player.getWorld().dropItem(player.getLocation(), item);
                }
                player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.0f, 1.0f);
                player.getWorld().spawnParticle(org.bukkit.Particle.PORTAL, player.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.1);
            }
        }
    }
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        Player attacker = null;

        ItemStack weapon = null;
        if (event.getDamager() instanceof Player player) {
            attacker = player;
            weapon = player.getInventory().getItemInMainHand();
        }
        else if (event.getDamager() instanceof Trident trident) {
            if (trident.getShooter() instanceof Player player) {
                attacker = player;
                weapon = trident.getItem();
            }
        }
        if (RegionUtil.isEnchantBlocked(attacker.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
        if (attacker == null || weapon == null || !weapon.hasItemMeta()) return;

        var pdc = weapon.getItemMeta().getPersistentDataContainer();
        if (pdc.has(CustomEnchant.VAMPIRISM.KEY, PersistentDataType.INTEGER)) {
            int level = pdc.get(CustomEnchant.VAMPIRISM.KEY, PersistentDataType.INTEGER);
            double chance = 0.15 * level;
            if (random.nextDouble() <= chance) {
                int duration = 40 + (level * 10);
                int amplifier = Math.max(0, (level / 3));
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, amplifier));
            }
        }
        if (pdc.has(CustomEnchant.POISON.KEY, PersistentDataType.INTEGER)) {
            int level = pdc.get(CustomEnchant.POISON.KEY, PersistentDataType.INTEGER);
            double chance = 0.10 + (0.05 * level);
            if (random.nextDouble() <= chance) {
                int duration = 60 + (20 * level);
                int amplifier = Math.max(0, (level / 4));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier));
            }
        }
        if (pdc.has(CustomEnchant.OXIDATION.KEY, PersistentDataType.INTEGER)) {
            int level = pdc.get(CustomEnchant.OXIDATION.KEY, PersistentDataType.INTEGER);
            CustomEnchant enchant = CustomEnchant.OXIDATION;

            double chance = enchant.COMBAT_CHANCE_BASE + (enchant.COMBAT_CHANCE_PER_LEVEL * level);
            boolean alwaysProcs = (enchant.COMBAT_CHANCE_BASE == 0 && enchant.COMBAT_CHANCE_PER_LEVEL == 0);

            if (alwaysProcs || random.nextDouble() <= chance) {
                int damageAmount = enchant.OXIDATION_DAMAGE_BASE + (enchant.OXIDATION_DAMAGE_PER_LEVEL * level);

                if (victim.getEquipment() != null) {
                    boolean armorDamaged = false;
                    for (ItemStack armor : victim.getEquipment().getArmorContents()) {
                        if (armor != null && armor.getType() != Material.AIR && armor.hasItemMeta()) {
                            ItemMeta meta = armor.getItemMeta();

                            // ✅ Пропускаем броню с атрибутом "Неразрушимый"
                            if (meta.isUnbreakable()) continue;

                            if (meta instanceof Damageable damageable) {
                                int currentDamage = damageable.getDamage();
                                int maxDurability = armor.getType().getMaxDurability();

                                if (maxDurability <= 0 || currentDamage >= maxDurability) continue;

                                int newDamage = Math.min(maxDurability, currentDamage + damageAmount);
                                damageable.setDamage(newDamage);
                                armor.setItemMeta(meta);

                                if (newDamage >= maxDurability) {
                                    armor.setAmount(0);
                                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                                }
                                armorDamaged = true;
                            }
                        }
                    }
                    if (armorDamaged) {
                        victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 0.5f, 1.5f);
                    }
                }
            }
        }
        if (pdc.has(CustomEnchant.STUPOR.KEY, PersistentDataType.INTEGER)) {
            if (event.getDamager() instanceof Trident) {
                int level = pdc.get(CustomEnchant.STUPOR.KEY, PersistentDataType.INTEGER);
                CustomEnchant enchant = CustomEnchant.STUPOR;

                double chance = enchant.COMBAT_CHANCE_BASE + (enchant.COMBAT_CHANCE_PER_LEVEL * level);

                if (random.nextDouble() <= chance) {
                    int duration = enchant.EFFECT_DURATION_BASE + (enchant.EFFECT_DURATION_PER_LEVEL * level);
                    int amplifier = Math.max(0, level / Math.max(1, enchant.EFFECT_AMPLIFIER_DIVISOR) - 1);
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, amplifier));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, amplifier));
                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.5f);
                }
            }
        }
        if (pdc.has(CustomEnchant.DETECTION.KEY, PersistentDataType.INTEGER)) {
            int level = pdc.get(CustomEnchant.DETECTION.KEY, PersistentDataType.INTEGER);
            CustomEnchant enchant = CustomEnchant.DETECTION;

            double chance = enchant.COMBAT_CHANCE_BASE + (enchant.COMBAT_CHANCE_PER_LEVEL * level);

            if (random.nextDouble() <= chance) {
                int duration = enchant.EFFECT_DURATION_BASE + (enchant.EFFECT_DURATION_PER_LEVEL * level);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0));
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.5f, 2.0f);
            }
        }
    }
}