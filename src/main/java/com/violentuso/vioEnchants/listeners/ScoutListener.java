package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class ScoutListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player shooter)) return;
        if (RegionUtil.isEnchantBlocked(shooter.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;

        ItemStack item = trident.getItem();
        if (item == null || !item.hasItemMeta()) return;

        CustomEnchant enchant = CustomEnchant.SCOUT;
        if (!enchant.ENABLED) return;

        var pdc = item.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(enchant.KEY, PersistentDataType.INTEGER)) return;

        int level = pdc.get(enchant.KEY, PersistentDataType.INTEGER);
        if (level <= 0) return;

        Vector targetLoc = trident.getLocation().toVector();
        Vector playerLoc = shooter.getLocation().toVector();
        Vector direction = targetLoc.subtract(playerLoc);
        direction.normalize();

        double force = enchant.SCOUT_STRENGTH_BASE + (enchant.SCOUT_STRENGTH_PER_LEVEL * level);
        direction.multiply(force);
        direction.setY(direction.getY() + enchant.SCOUT_VERTICAL_BONUS);

        if (direction.getY() > 2.0) direction.setY(2.0);
        shooter.setVelocity(shooter.getVelocity().add(direction));
        shooter.setFallDistance(0);

        if (enchant.ACTIVATION_SOUND != null) {
            shooter.getWorld().playSound(shooter.getLocation(), enchant.ACTIVATION_SOUND, enchant.ACTIVATION_SOUND_VOLUME, enchant.ACTIVATION_SOUND_PITCH);
        } else {
            shooter.getWorld().playSound(shooter.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 1.0f, 1.0f);
        }
    }
}