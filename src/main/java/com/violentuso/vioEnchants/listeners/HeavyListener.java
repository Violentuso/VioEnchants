package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HeavyListener implements Listener {

    private void applyHeavyEffect(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
        if (item.getItemMeta().getPersistentDataContainer().has(CustomEnchant.HEAVY.KEY, PersistentDataType.INTEGER)) {
            int level = item.getItemMeta().getPersistentDataContainer().get(CustomEnchant.HEAVY.KEY, PersistentDataType.INTEGER);
            CustomEnchant enchant = CustomEnchant.HEAVY;

            int duration = enchant.HEAVY_DURATION_BASE + (enchant.HEAVY_DURATION_PER_LEVEL * level);
            int amplifier = enchant.HEAVY_AMPLIFIER;

            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, duration, amplifier));

            if (enchant.ACTIVATION_SOUND != null) {
                player.playSound(player.getLocation(), enchant.ACTIVATION_SOUND, enchant.ACTIVATION_SOUND_VOLUME, enchant.ACTIVATION_SOUND_PITCH);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        applyHeavyEffect(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            applyHeavyEffect(player, player.getInventory().getItemInMainHand());
        }
    }
}