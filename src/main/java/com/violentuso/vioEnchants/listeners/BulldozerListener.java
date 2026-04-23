package com.violentuso.vioEnchants.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.VioEnchants;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;

public class BulldozerListener implements Listener {

    private final Random random = new Random();
    private final boolean hasWorldGuard;

    public BulldozerListener() {
        this.hasWorldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;
        if (WebListener.isMining.contains(player.getUniqueId())) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType() == Material.AIR || !tool.hasItemMeta()) return;

        CustomEnchant enchantType = null;
        int level = 0;
        if (tool.getItemMeta().getPersistentDataContainer().has(CustomEnchant.MEGA_BULLDOZER.KEY, PersistentDataType.INTEGER)) {
            enchantType = CustomEnchant.MEGA_BULLDOZER;
            level = 1;
        } else if (tool.getItemMeta().getPersistentDataContainer().has(CustomEnchant.LUMBERJACK.KEY, PersistentDataType.INTEGER)) {
            enchantType = CustomEnchant.LUMBERJACK;
            level = tool.getItemMeta().getPersistentDataContainer().get(CustomEnchant.LUMBERJACK.KEY, PersistentDataType.INTEGER);
        } else if (tool.getItemMeta().getPersistentDataContainer().has(CustomEnchant.BULLDOZER.KEY, PersistentDataType.INTEGER)) {
            enchantType = CustomEnchant.BULLDOZER;
            level = tool.getItemMeta().getPersistentDataContainer().get(CustomEnchant.BULLDOZER.KEY, PersistentDataType.INTEGER);
        }

        if (enchantType == null) return;

        event.setCancelled(true);

        boolean hasAutoSmelt = tool.getItemMeta().getPersistentDataContainer().has(CustomEnchant.AUTOSMELT.KEY, PersistentDataType.INTEGER);
        boolean hasWeb = tool.getItemMeta().getPersistentDataContainer().has(CustomEnchant.WEB.KEY, PersistentDataType.INTEGER);

        int radius;
        int depth;

        if (enchantType == CustomEnchant.MEGA_BULLDOZER) {
            radius = 4;
            depth = 9;
            if (isRegionBlocked(player, event.getBlock(), enchantType.MEGA_BULLDOZER_BLOCKED_REGIONS)) {
                player.sendMessage("§cМагия Мега-Бульдозера здесь не работает!");
                return;
            }
        } else {
            radius = enchantType.BULLDOZER_RADIUS_BASE + (enchantType.BULLDOZER_RADIUS_PER_LEVEL * (level - 1));
            depth = enchantType.BULLDOZER_DEPTH_BASE + (enchantType.BULLDOZER_DEPTH_PER_LEVEL * (level - 1));
        }

        Block center = event.getBlock();
        BlockFace face = getBlockFace(player);
        if (face == null) return;
        WebListener.isMining.add(player.getUniqueId());

        try {
            for (int d = 0; d < depth; d++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {

                        Block target;
                        if (face == BlockFace.UP || face == BlockFace.DOWN) {
                            target = center.getRelative(x, d * (face == BlockFace.UP ? -1 : 1), y);
                        } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
                            target = center.getRelative(d * (face == BlockFace.EAST ? -1 : 1), x, y);
                        } else {
                            target = center.getRelative(x, y, d * (face == BlockFace.SOUTH ? -1 : 1));
                        }

                        if (target.getType() == Material.AIR || target.getType() == Material.BEDROCK) continue;

                        if (hasWeb && WebListener.isWhitelisted(target.getType())) {
                            WebListener.performWebMining(target, player, tool);
                            if (tool.getType() == Material.AIR) return;
                            continue;
                        }

                        BlockBreakEvent checkEvent = new BlockBreakEvent(target, player);
                        checkEvent.setDropItems(false);

                        VioEnchants.INSTANCE.getServer().getPluginManager().callEvent(checkEvent);
                        if (checkEvent.isCancelled()) continue;

                        if (enchantType == CustomEnchant.MEGA_BULLDOZER) {
                            if (isRegionBlocked(player, target, enchantType.MEGA_BULLDOZER_BLOCKED_REGIONS)) continue;
                        }

                        WebListener.processDrop(target, player, tool, hasAutoSmelt);
                        target.setType(Material.AIR);

                        applyDurability(tool, player);
                        if (tool.getType() == Material.AIR) return;
                    }
                }
            }
        } finally {
            WebListener.isMining.remove(player.getUniqueId());
        }
    }

    private BlockFace getBlockFace(Player player) {
        List<Block> lastBlocks = player.getLastTwoTargetBlocks(null, 10);
        if (lastBlocks.size() < 2) return null;
        return lastBlocks.get(1).getFace(lastBlocks.get(0));
    }

    private void applyDurability(ItemStack item, Player player) {
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

    private boolean isRegionBlocked(Player player, Block block, List<String> blockedRegions) {
        if (blockedRegions == null || blockedRegions.isEmpty() || !hasWorldGuard) return false;
        try {
            com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(block.getLocation());
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(loc);
            for (ProtectedRegion region : set) {
                if (blockedRegions.contains(region.getId())) return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}