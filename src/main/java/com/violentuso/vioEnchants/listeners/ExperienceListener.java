package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ExperienceListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getExpToDrop() <= 0) return;

        Player player = event.getPlayer();
        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        int level = getExpEnchantLevel(tool);
        if (level <= 0) return;

        int oldExp = event.getExpToDrop();
        int newExp = calculateNewExp(oldExp, level);
        event.setExpToDrop(newExp);

        if (newExp > oldExp) {
            if (CustomEnchant.EXPERIENCE.ACTIVATION_SOUND != null) {
                player.playSound(player.getLocation(), CustomEnchant.EXPERIENCE.ACTIVATION_SOUND, CustomEnchant.EXPERIENCE.ACTIVATION_SOUND_VOLUME, CustomEnchant.EXPERIENCE.ACTIVATION_SOUND_PITCH);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getDroppedExp() <= 0) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (RegionUtil.isEnchantBlocked(killer.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        int level = getExpEnchantLevel(weapon);
        if (level <= 0) return;

        int oldExp = event.getDroppedExp();
        int newExp = calculateNewExp(oldExp, level);

        event.setDroppedExp(newExp);

        if (newExp > oldExp) {
            if (CustomEnchant.EXPERIENCE.ACTIVATION_SOUND != null) {
                killer.playSound(killer.getLocation(), CustomEnchant.EXPERIENCE.ACTIVATION_SOUND, CustomEnchant.EXPERIENCE.ACTIVATION_SOUND_VOLUME, CustomEnchant.EXPERIENCE.ACTIVATION_SOUND_PITCH);
            } else {
                killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
            }
        }
    }

    private int getExpEnchantLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        CustomEnchant enchant = CustomEnchant.EXPERIENCE;
        if (!enchant.ENABLED) return 0;

        var pdc = item.getItemMeta().getPersistentDataContainer();
        if (pdc.has(enchant.KEY, PersistentDataType.INTEGER)) {
            return pdc.get(enchant.KEY, PersistentDataType.INTEGER);
        }
        return 0;
    }

    private int calculateNewExp(int originalExp, int level) {
        CustomEnchant enchant = CustomEnchant.EXPERIENCE;
        double multiplier = enchant.EXP_MULTI_BASE + (enchant.EXP_MULTI_PER_LEVEL * level);
        return originalExp + (int) Math.round(originalExp * multiplier);
    }
}