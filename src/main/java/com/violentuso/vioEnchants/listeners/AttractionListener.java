package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class AttractionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof LivingEntity victim)) return;
        if (victim.equals(shooter)) return;

        ItemStack item = trident.getItem();
        if (!item.hasItemMeta()) return;

        CustomEnchant enchant = CustomEnchant.ATTRACTION;
        if (!enchant.ENABLED) return;
        if (RegionUtil.isEnchantBlocked(victim.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(enchant.KEY, PersistentDataType.INTEGER)) return;

        int level = pdc.get(enchant.KEY, PersistentDataType.INTEGER);
        if (level <= 0) return;
        Vector direction = shooter.getLocation().toVector().subtract(victim.getLocation().toVector());
        direction.normalize();
        double force = enchant.ATTRACTION_STRENGTH_BASE + (enchant.ATTRACTION_STRENGTH_PER_LEVEL * level);
        direction.multiply(force);
        direction.setY(direction.getY() + enchant.ATTRACTION_VERTICAL_BONUS);
        victim.setVelocity(direction);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.2f);
        shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.2f);
    }
}