package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.util.RegionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AutoSmeltListener implements Listener {

    private static final Map<Material, ItemStack> smeltCache = new HashMap<>();
    private static long lastCacheUpdate = 0;

    public AutoSmeltListener() {
        updateRecipes();
    }

    public static void updateRecipes() {
        smeltCache.clear();
        Iterator<Recipe> iter = Bukkit.recipeIterator();
        while (iter.hasNext()) {
            if (iter.next() instanceof FurnaceRecipe furnaceRecipe) {
                Material input = furnaceRecipe.getInput().getType();
                if (input != Material.AIR) {
                    smeltCache.put(input, furnaceRecipe.getResult());
                }
            }
        }
        lastCacheUpdate = System.currentTimeMillis();
    }

    public static ItemStack trySmelt(ItemStack original) {
        if (System.currentTimeMillis() - lastCacheUpdate > 300000) {
            updateRecipes();
        }

        Material type = original.getType();
        if (smeltCache.containsKey(type)) {
            ItemStack result = smeltCache.get(type).clone();
            result.setAmount(original.getAmount());
            return result;
        }
        return null;
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        Player player = event.getPlayer();


        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType() == Material.AIR || !tool.hasItemMeta()) return;


        if (tool.getItemMeta().getPersistentDataContainer().has(CustomEnchant.AUTOSMELT.KEY, PersistentDataType.INTEGER)) {


            ItemStack rawBlock = new ItemStack(event.getBlock().getType());


            if (trySmelt(rawBlock) != null) {

                event.setExpToDrop(0);
            }
        }
    }
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        if (RegionUtil.isEnchantBlocked(player.getLocation(), CustomEnchant.GLOBAL_BLOCKED_REGIONS, CustomEnchant.GLOBAL_BLOCKED_FLAGS)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType() == Material.AIR || !tool.hasItemMeta()) return;

        if (!tool.getItemMeta().getPersistentDataContainer().has(CustomEnchant.AUTOSMELT.KEY, PersistentDataType.INTEGER)) {
            return;
        }

        boolean smeltedAny = false;

        for (Item itemEntity : event.getItems()) {
            ItemStack stack = itemEntity.getItemStack();
            ItemStack smelted = trySmelt(stack);

            if (smelted != null) {
                itemEntity.setItemStack(smelted);
                smeltedAny = true;
            }
        }

        if (smeltedAny && CustomEnchant.AUTOSMELT.ACTIVATION_SOUND != null) {
            player.playSound(player.getLocation(), CustomEnchant.AUTOSMELT.ACTIVATION_SOUND, CustomEnchant.AUTOSMELT.ACTIVATION_SOUND_VOLUME, CustomEnchant.AUTOSMELT.ACTIVATION_SOUND_PITCH);
        }
    }
}