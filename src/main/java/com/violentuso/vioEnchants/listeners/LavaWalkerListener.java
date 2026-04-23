package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class LavaWalkerListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;

        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || !boots.hasItemMeta()) return;

        PersistentDataContainer pdc = boots.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(CustomEnchant.LAVA_WALKER.KEY, PersistentDataType.INTEGER)) return;

        int level = pdc.get(CustomEnchant.LAVA_WALKER.KEY, PersistentDataType.INTEGER);

        int width = CustomEnchant.LAVA_WALKER.LAVA_WALKER_W.getOrDefault(level, 3);
        int height = CustomEnchant.LAVA_WALKER.LAVA_WALKER_H.getOrDefault(level, 1);
        int length = CustomEnchant.LAVA_WALKER.LAVA_WALKER_L.getOrDefault(level, 3);

        Block centerBlock = event.getTo().getBlock();

        boolean canActivate = false;
        for (int y = 1; y <= height; y++) {
            Material type = centerBlock.getRelative(0, -y, 0).getType();
            if (type == Material.LAVA || type == Material.OBSIDIAN || type == Material.BASALT) {
                canActivate = true;
                break;
            }
        }

        if (!canActivate) return;

        BlockFace facing = player.getFacing();

        int xSpan = width;
        int zSpan = length;

        if (facing == BlockFace.EAST || facing == BlockFace.WEST) {
            xSpan = length;
            zSpan = width;
        }
        int halfX = (xSpan - 1) / 2;
        int halfZ = (zSpan - 1) / 2;

        for (int x = -halfX; x <= (xSpan / 2); x++) {
            for (int z = -halfZ; z <= (zSpan / 2); z++) {
                for (int y = 1; y <= height; y++) {

                    Block targetBlock = centerBlock.getRelative(x, -y, z);

                    if (targetBlock.getType() == Material.LAVA) {
                        if (targetBlock.getBlockData() instanceof Levelled levelledData) {
                            if (levelledData.getLevel() == 0) {
                                targetBlock.setType(Material.OBSIDIAN);
                            } else {
                                targetBlock.setType(Material.BASALT);
                            }
                        }
                    }
                }
            }
        }
    }
}