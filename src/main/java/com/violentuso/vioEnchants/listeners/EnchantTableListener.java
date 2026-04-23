package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnchantTableListener implements Listener {
    private final Random random = new Random();

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        boolean changed = false;
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        int expCost = event.getExpLevelCost();

        for (CustomEnchant enchant : CustomEnchant.values()) {
            if (!enchant.ENABLED) continue;
            if (!enchant.TABLE_ENABLED) continue;
            if (!enchant.canEnchant(item)) continue;
            double baseChance = enchant.TABLE_CHANCE_BASE + (expCost * enchant.TABLE_CHANCE_PER_EXP);
            if (random.nextDouble() > baseChance) continue;
            int level = 1;
            for (int nextLvl = 2; nextLvl <= enchant.MAX_LEVEL; nextLvl++) {
                if (!enchant.LEVEL_CHANCES.containsKey(nextLvl)) break;

                int threshold = enchant.LEVEL_THRESHOLDS.get(nextLvl);
                double chance = enchant.LEVEL_CHANCES.get(nextLvl);
                if (expCost < threshold) break;
                if (random.nextDouble() < chance) {
                    level = nextLvl;
                } else {
                    break;
                }
            }

            meta.getPersistentDataContainer().set(enchant.KEY, PersistentDataType.INTEGER, level);
            lore.add(enchant.getLoreLine(level));
            changed = true;
        }

        if (changed) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }
}