package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.VioEnchants;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Random;

public class ExplosiveListener implements Listener {

    private final Random random = new Random();
    private final NamespacedKey EXPLODED_KEY = new NamespacedKey(VioEnchants.INSTANCE, "already_exploded");

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getProjectile() instanceof Projectile projectile)) return;
        if (RegionUtil.isEnchantBlocked(event.getEntity().getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
        ItemStack bow = event.getBow();
        if (bow == null || !bow.hasItemMeta()) return;

        CustomEnchant enchant = CustomEnchant.EXPLOSIVE;
        if (!enchant.ENABLED) return;

        var pdc = bow.getItemMeta().getPersistentDataContainer();
        if (pdc.has(enchant.KEY, PersistentDataType.INTEGER)) {
            int level = pdc.get(enchant.KEY, PersistentDataType.INTEGER);
            projectile.getPersistentDataContainer().set(enchant.KEY, PersistentDataType.INTEGER, level);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        CustomEnchant enchant = CustomEnchant.EXPLOSIVE;

        if (!enchant.ENABLED) return;

        int level = 0;

        if (projectile instanceof Trident trident) {

            ItemStack tridentItem = trident.getItem();

            if (tridentItem.hasItemMeta()) {
                var pdc = tridentItem.getItemMeta().getPersistentDataContainer();
                if (pdc.has(enchant.KEY, PersistentDataType.INTEGER)) {
                    level = pdc.get(enchant.KEY, PersistentDataType.INTEGER);
                }
            }
        } else if (projectile instanceof AbstractArrow) {

            var pdc = projectile.getPersistentDataContainer();
            if (pdc.has(enchant.KEY, PersistentDataType.INTEGER)) {
                level = pdc.get(enchant.KEY, PersistentDataType.INTEGER);
            }
        }

        if (level > 0) {
            if (RegionUtil.isEnchantBlocked(projectile.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
            if (projectile.getPersistentDataContainer().has(EXPLODED_KEY, PersistentDataType.BYTE)) {
                return;
            }
            double chance = enchant.EXPLOSIVE_CHANCE_BASE + (enchant.EXPLOSIVE_CHANCE_PER_LEVEL * level);
            if (random.nextDouble() > chance) return;
            projectile.getPersistentDataContainer().set(EXPLODED_KEY, PersistentDataType.BYTE, (byte) 1);
            if (projectile instanceof Trident) {
                projectile.setInvulnerable(true);
            }

            Location loc = projectile.getLocation();
            if (enchant.EXPLOSIVE_BLOCK_DAMAGE) {
                projectile.getWorld().createExplosion(loc, 2.0f, enchant.EXPLOSIVE_FIRE, true);
            } else {
                projectile.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            }
            if (enchant.EXPLOSIVE_PARTICLES_ENABLED) {
                projectile.getWorld().spawnParticle(
                        enchant.EXPLOSIVE_PARTICLE_TYPE,
                        loc,
                        enchant.EXPLOSIVE_PARTICLE_COUNT,
                        0.5, 0.5, 0.5,
                        0.1
                );
            }
            double damage = enchant.EXPLOSIVE_DAMAGE_BASE + (enchant.EXPLOSIVE_DAMAGE_PER_LEVEL * level);
            double knockback = enchant.EXPLOSIVE_KNOCKBACK_BASE + (enchant.EXPLOSIVE_KNOCKBACK_PER_LEVEL * level);
            double radius = enchant.EXPLOSIVE_RADIUS;

            for (Entity entity : projectile.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
                if (!(entity instanceof LivingEntity victim)) continue;
                if (entity == projectile.getShooter()) continue;
                if (entity == projectile) continue;

                double distance = entity.getLocation().distance(loc);
                if (distance > radius) continue;

                victim.damage(damage, (Entity) projectile.getShooter());

                Vector vector = entity.getLocation().toVector().subtract(loc.toVector()).normalize();
                if (Double.isNaN(vector.getX())) vector = new Vector(0, 0.5, 0);

                vector.multiply(knockback);
                vector.setY(vector.getY() + 0.3);

                victim.setVelocity(victim.getVelocity().add(vector));

                if (enchant.EXPLOSIVE_FIRE) {
                    victim.setFireTicks(100);
                }
            }
            if (!(projectile instanceof Trident)) {
                projectile.remove();
            }
        }

    }
}