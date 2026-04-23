package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.VioEnchants;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class SniperListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (RegionUtil.isEnchantBlocked(event.getEntity().getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
        ItemStack bow = event.getBow();
        if (bow == null) return;
        int level = getSniperLevel(bow);
        if (level > 0) {
            handleSniperShot(event.getProjectile(), level, (Player) event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTridentThrow(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;

        if (!(trident.getShooter() instanceof Player player)) return;
        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != org.bukkit.Material.TRIDENT) {
            item = player.getInventory().getItemInOffHand();
        }
        if (item.getType() != org.bukkit.Material.TRIDENT) return;

        int level = getSniperLevel(item);
        if (level > 0) {
            handleSniperShot(trident, level, player);
        }
    }

    private int getSniperLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        CustomEnchant enchant = CustomEnchant.SNIPER;
        if (!enchant.ENABLED) return 0;

        var pdc = item.getItemMeta().getPersistentDataContainer();
        if (pdc.has(enchant.KEY, PersistentDataType.INTEGER)) {
            return pdc.get(enchant.KEY, PersistentDataType.INTEGER);
        }
        return 0;
    }

    private void handleSniperShot(Entity projectile, int level, Player shooter) {
        CustomEnchant enchant = CustomEnchant.SNIPER;
        double speedMultiplier = enchant.SNIPER_VELOCITY_MULTI_BASE + (enchant.SNIPER_VELOCITY_MULTI_PER_LEVEL * level);
        Vector originalVelocity = projectile.getVelocity();
        Vector newVelocity = originalVelocity.multiply(speedMultiplier);
        projectile.setVelocity(newVelocity);

        if (projectile instanceof AbstractArrow arrow) arrow.setGravity(true);
        if (projectile instanceof Trident trident) trident.setGravity(true);
        shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1.6f + (0.1f * level));
        Location predictedHit = predictImpactLocation(projectile.getLocation(), newVelocity);

        if (predictedHit != null && enchant.SNIPER_PARTICLES_ENABLED) {
            predictedHit.getWorld().spawnParticle(
                    org.bukkit.Particle.CRIT,
                    predictedHit,
                    10, 0.1, 0.1, 0.1, 0.05
            );
        }
        if (enchant.SNIPER_PARTICLES_ENABLED) {
            VioEnchants.INSTANCE.getServer().getScheduler().runTaskTimer(VioEnchants.INSTANCE, task -> {
                if (projectile.isDead() || projectile.isOnGround()) {
                    task.cancel();
                    return;
                }
                projectile.getWorld().spawnParticle(
                        enchant.SNIPER_PARTICLE_TYPE,
                        projectile.getLocation(),
                        enchant.SNIPER_PARTICLE_COUNT,
                        0, 0, 0, 0
                );
            }, 0L, 1L);
        }
    }

    private Location predictImpactLocation(Location start, Vector velocity) {
        World world = start.getWorld();
        if (world == null) return null;

        Vector currentVel = velocity.clone();
        Location currentLoc = start.clone();
        double drag = 0.99;
        double gravity = 0.05;
        for (int i = 0; i < 200; i++) {
            Location oldLoc = currentLoc.clone();
            currentLoc.add(currentVel);
            currentVel.multiply(drag);
            currentVel.setY(currentVel.getY() - gravity);
            double stepDistance = currentLoc.distance(oldLoc);
            if (stepDistance > 0.0) {
                Vector direction = currentLoc.toVector().subtract(oldLoc.toVector()).normalize();
                RayTraceResult hit = world.rayTraceBlocks(oldLoc, direction, stepDistance);

                if (hit != null && hit.getHitBlock() != null) {
                    return hit.getHitPosition().toLocation(world);
                }
            }
            if (currentLoc.getY() < world.getMinHeight()) {
                return null;
            }
        }
        return null;
    }
}