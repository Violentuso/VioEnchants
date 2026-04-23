package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import com.violentuso.vioEnchants.VioEnchants;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class AnvilListener implements Listener {

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack target = anvil.getItem(0);
        ItemStack sacrifice = anvil.getItem(1);

        if (target == null || sacrifice == null) return;
        ItemStack result = event.getResult();
        if (result == null || result.getType() == Material.AIR) {
            result = target.clone();
        }

        ItemMeta resultMeta = result.getItemMeta();
        ItemMeta sacrificeMeta = sacrifice.getItemMeta();
        if (resultMeta == null || sacrificeMeta == null) return;

        boolean changed = false;
        int repairCostAdded = 0;

        for (CustomEnchant enchant : CustomEnchant.values()) {
            int sacrificeLevel = 0;
            if (sacrificeMeta.getPersistentDataContainer().has(enchant.KEY, PersistentDataType.INTEGER)) {
                sacrificeLevel = sacrificeMeta.getPersistentDataContainer().get(enchant.KEY, PersistentDataType.INTEGER);
            }

            if (sacrificeLevel == 0) continue;
            if (!enchant.canEnchant(result) && result.getType() != Material.ENCHANTED_BOOK) continue;
            int currentLevel = 0;
            if (resultMeta.getPersistentDataContainer().has(enchant.KEY, PersistentDataType.INTEGER)) {
                currentLevel = resultMeta.getPersistentDataContainer().get(enchant.KEY, PersistentDataType.INTEGER);
            }

            int finalLevel = currentLevel;
            if (sacrificeLevel > currentLevel) {
                finalLevel = sacrificeLevel;
            } else if (sacrificeLevel == currentLevel) {
                finalLevel = currentLevel + 1;
            }
            if (finalLevel > enchant.MAX_LEVEL) finalLevel = enchant.MAX_LEVEL;
            if (finalLevel != currentLevel) {
                resultMeta.getPersistentDataContainer().set(enchant.KEY, PersistentDataType.INTEGER, finalLevel);
                changed = true;
                repairCostAdded += (enchant.ANVIL_COST * finalLevel);
            }
        }
        if (changed || (result != null && !result.equals(target))) {
            if (changed) {
                List<String> lore = resultMeta.hasLore() ? new ArrayList<>(resultMeta.getLore()) : new ArrayList<>();
                for (CustomEnchant enchant : CustomEnchant.values()) {
                    lore.removeIf(line -> line.contains(enchant.NAME));
                }
                for (CustomEnchant enchant : CustomEnchant.values()) {
                    if (resultMeta.getPersistentDataContainer().has(enchant.KEY, PersistentDataType.INTEGER)) {
                        int lvl = resultMeta.getPersistentDataContainer().get(enchant.KEY, PersistentDataType.INTEGER);
                        lore.add(enchant.getLoreLine(lvl));
                    }
                }
                resultMeta.setLore(lore);


                if (result.getType() != Material.ENCHANTED_BOOK) {

                }
            }

            result.setItemMeta(resultMeta);
            event.setResult(result);

            final int finalExtraCost = repairCostAdded;
            if (finalExtraCost > 0) {

                VioEnchants.INSTANCE.getServer().getScheduler().runTask(VioEnchants.INSTANCE, () -> {
                    if (event.getView().getPlayer() != null) {
                        int currentCost = anvil.getRepairCost();
                        anvil.setRepairCost(currentCost + finalExtraCost);
                    }
                });
            }
        }
    }
}