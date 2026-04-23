package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.VioEnchants;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class WebListener implements Listener {

    private static final Random random = new Random();
    public static final Set<UUID> isMining = new HashSet<>();

    private static final BlockFace[] FACES = {
            BlockFace.UP, BlockFace.DOWN,
            BlockFace.NORTH, BlockFace.SOUTH,
            BlockFace.EAST, BlockFace.WEST
    };

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
        if (isMining.contains(player.getUniqueId())) return;

        if (player.getGameMode() == GameMode.CREATIVE || player.isSneaking()) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType() == Material.AIR || !tool.hasItemMeta()) return;

        if (!tool.getItemMeta().getPersistentDataContainer().has(CustomEnchant.WEB.KEY, PersistentDataType.INTEGER)) {
            return;
        }

        Block startBlock = event.getBlock();
        if (!isWhitelisted(startBlock.getType())) return;

        event.setCancelled(true);

        isMining.add(player.getUniqueId());
        try {
            performWebMining(startBlock, player, tool);
        } finally {
            isMining.remove(player.getUniqueId());
        }
    }

    public static boolean isWhitelisted(Material mat) {
        CustomEnchant enchant = CustomEnchant.WEB;
        if (enchant.WEB_WHITELIST.contains("*")) return true;
        String matName = mat.name();
        for (String listed : enchant.WEB_WHITELIST) {
            if (matName.equals(listed)) return true;
            if (listed.startsWith("*") && matName.endsWith(listed.substring(1))) return true;
        }
        return false;
    }

    public static void performWebMining(Block startBlock, Player player, ItemStack tool) {
        Material targetMaterial = startBlock.getType();
        boolean hasAutoSmelt = tool.getItemMeta().getPersistentDataContainer().has(CustomEnchant.AUTOSMELT.KEY, PersistentDataType.INTEGER);
        int limit = CustomEnchant.WEB.WEB_LIMIT;

        Set<Block> blocksToBreak = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        queue.add(startBlock);
        visited.add(startBlock);
        blocksToBreak.add(startBlock);

        while (!queue.isEmpty() && blocksToBreak.size() < limit) {
            Block current = queue.poll();
            for (BlockFace face : FACES) {
                Block relative = current.getRelative(face);
                if (visited.contains(relative)) continue;

                if (relative.getType() == targetMaterial) {
                    visited.add(relative);
                    blocksToBreak.add(relative);
                    queue.add(relative);
                    if (blocksToBreak.size() >= limit) break;
                }
            }
        }

        for (Block block : blocksToBreak) {
            if (block.getType() == Material.AIR) continue;

            BlockBreakEvent checkEvent = new BlockBreakEvent(block, player);
            checkEvent.setDropItems(false);
            VioEnchants.INSTANCE.getServer().getPluginManager().callEvent(checkEvent);
            if (checkEvent.isCancelled()) continue;

            processDrop(block, player, tool, hasAutoSmelt);

            block.setType(Material.AIR);
            applyDurability(tool, player);
            if (tool.getType() == Material.AIR) break;
        }
    }

    public static void processDrop(Block block, Player player, ItemStack tool, boolean hasAutoSmelt) {
        Collection<ItemStack> drops = block.getDrops(tool);
        int expToDrop = 0;

        if (!block.getType().name().contains("STONE") && !block.getType().name().contains("DIRT")) {
            expToDrop = 1;
        }

        for (ItemStack drop : drops) {
            if (drop == null || drop.getType() == Material.AIR) continue;
            if (hasAutoSmelt) {
                ItemStack smelted = AutoSmeltListener.trySmelt(drop);
                if (smelted != null) {
                    drop = smelted;
                    if (expToDrop == 0) expToDrop = 1;
                }
            }
            HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(drop);
            for (ItemStack trash : leftOver.values()) {
                block.getWorld().dropItemNaturally(block.getLocation(), trash);
            }
        }

        if (expToDrop > 0) {
            block.getWorld().spawn(block.getLocation(), ExperienceOrb.class).setExperience(expToDrop);
        }
    }

    private static void applyDurability(ItemStack item, Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) return;

        int unbreaking = item.getEnchantmentLevel(Enchantment.DURABILITY);
        if (random.nextInt(unbreaking + 1) == 0) {
            int damage = damageable.getDamage() + 1;
            damageable.setDamage(damage);
            item.setItemMeta(meta);
            if (damage >= item.getType().getMaxDurability()) {
                item.setAmount(0);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            }
        }
    }
}