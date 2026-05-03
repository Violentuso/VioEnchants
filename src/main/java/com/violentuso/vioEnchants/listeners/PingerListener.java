package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PingerListener implements Listener {

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        Player player = event.getPlayer();
        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
        if (meta == null) return;
        if (!meta.getPersistentDataContainer().has(CustomEnchant.PINGER.KEY, PersistentDataType.INTEGER)) {
            return;
        }
        CustomEnchant enchant = CustomEnchant.PINGER;
        if (meta instanceof Damageable damageable) {
            int maxDurability = item.getType().getMaxDurability();
            int currentDamage = damageable.getDamage();
            int damageToApply = event.getDamage();
            int remainingUses = maxDurability - (currentDamage + damageToApply);
            if (remainingUses <= enchant.PINGER_THRESHOLD && remainingUses > 0) {
                try {
                    Sound sound = Sound.valueOf(enchant.PINGER_SOUND_NAME.toUpperCase());
                    player.playSound(player.getLocation(), sound, 1.0f, 2.0f);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().info("[VioEnchants] Ошибка: Неверный звук для Pinger в конфиге!");
                }
            }
        }
    }
}