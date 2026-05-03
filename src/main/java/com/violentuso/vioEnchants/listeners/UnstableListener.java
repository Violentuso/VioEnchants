package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class UnstableListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
        if (item == null || !item.hasItemMeta()) return;
        if (!item.getItemMeta().getPersistentDataContainer().has(CustomEnchant.UNSTABLE.KEY, PersistentDataType.INTEGER)) {
            return;
        }

        CustomEnchant enchant = CustomEnchant.UNSTABLE;
        int level = item.getItemMeta().getPersistentDataContainer().get(CustomEnchant.UNSTABLE.KEY, PersistentDataType.INTEGER);
        double chance = enchant.UNSTABLE_CHANCE_BASE + (enchant.UNSTABLE_CHANCE_PER_LEVEL * level);

        if (random.nextDouble() <= chance) {
            int extraDamage = enchant.UNSTABLE_EXTRA_DAMAGE_BASE + (enchant.UNSTABLE_EXTRA_DAMAGE_PER_LEVEL * level);
            if (extraDamage > 0) {
                event.setDamage(event.getDamage() + extraDamage);
                if (enchant.ACTIVATION_SOUND != null) {
                    player.playSound(player.getLocation(), enchant.ACTIVATION_SOUND, enchant.ACTIVATION_SOUND_VOLUME, enchant.ACTIVATION_SOUND_PITCH);
                }
            }
        }
    }
}