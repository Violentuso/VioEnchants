package com.violentuso.vioEnchants.listeners;

import com.violentuso.vioEnchants.main.CustomEnchant;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class GrindstoneListener implements Listener {

    @EventHandler
    public void onGrindstonePrepare(PrepareGrindstoneEvent event) {
        if (!(event.getInventory() instanceof GrindstoneInventory)) return;

        ItemStack upper = event.getInventory().getItem(0);
        ItemStack lower = event.getInventory().getItem(1);

        boolean inputHasCustomEnchants = hasCustomEnchants(upper) || hasCustomEnchants(lower);
        if (!inputHasCustomEnchants) return;

        ItemStack result = event.getResult();
        if (result == null || result.getType() == Material.AIR) return;

        ItemStack newResult = result.clone();
        ItemMeta meta = newResult.getItemMeta();
        if (meta == null) return;

        for (CustomEnchant enchant : CustomEnchant.values()) {
            if (meta.getPersistentDataContainer().has(enchant.KEY, PersistentDataType.INTEGER)) {
                meta.getPersistentDataContainer().remove(enchant.KEY);
            }
        }

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        if (upper != null && upper.hasItemMeta() && upper.getItemMeta().hasLore()) {
            for (String line : upper.getItemMeta().getLore()) {
                if (!lore.contains(line)) lore.add(line);
            }
        }
        if (lower != null && lower.hasItemMeta() && lower.getItemMeta().hasLore()) {
            for (String line : lower.getItemMeta().getLore()) {
                if (!lore.contains(line)) lore.add(line);
            }
        }

        for (CustomEnchant enchant : CustomEnchant.values()) {
            lore.removeIf(line -> line.contains(enchant.NAME));
        }

        meta.setLore(lore.isEmpty() ? null : lore);
        removeGlowIfNoEnchants(meta);
        newResult.setItemMeta(meta);
        event.setResult(newResult);
    }

    private boolean hasCustomEnchants(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        for (CustomEnchant enchant : CustomEnchant.values()) {
            if (meta.getPersistentDataContainer().has(enchant.KEY, PersistentDataType.INTEGER)) {
                return true;
            }
        }
        return false;
    }

    private void removeGlowIfNoEnchants(ItemMeta meta) {
        boolean hasCustom = false;
        for (CustomEnchant enchant : CustomEnchant.values()) {
            if (meta.getPersistentDataContainer().has(enchant.KEY, PersistentDataType.INTEGER)) {
                hasCustom = true;
                break;
            }
        }

        if (!hasCustom && !hasRealEnchants(meta)) {
            meta.removeEnchant(Enchantment.LURE);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }

    private boolean hasRealEnchants(ItemMeta meta) {
        if (!meta.hasEnchants()) return false;
        if (meta.getEnchants().size() == 1
                && meta.hasEnchant(Enchantment.LURE)
                && meta.getEnchantLevel(Enchantment.LURE) == 1) {
            return false;
        }
        return true;
    }
}