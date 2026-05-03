package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.HashMap;

public class MagnetListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType() == Material.AIR || !tool.hasItemMeta()) return;

        ItemMeta meta = tool.getItemMeta();
        if (!meta.getPersistentDataContainer().has(CustomEnchant.MAGNET.KEY, PersistentDataType.INTEGER)) {
            return;
        }

        Block block = event.getBlock();
        if (!event.isDropItems()) return;

        Collection<ItemStack> drops = block.getDrops(tool);
        if (drops.isEmpty()) return;

        event.setDropItems(false);
        for (ItemStack drop : drops) {
            HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(drop);
            for (ItemStack surplus : leftOver.values()) {
                player.getWorld().dropItem(player.getLocation(), surplus);
            }
        }

        if (CustomEnchant.MAGNET.ACTIVATION_SOUND != null) {
            player.playSound(player.getLocation(), CustomEnchant.MAGNET.ACTIVATION_SOUND, CustomEnchant.MAGNET.ACTIVATION_SOUND_VOLUME, CustomEnchant.MAGNET.ACTIVATION_SOUND_PITCH);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.5f);
        }
    }
}