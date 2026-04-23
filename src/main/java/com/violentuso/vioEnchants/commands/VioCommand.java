package com.violentuso.vioEnchants.commands;

import com.violentuso.vioEnchants.VioEnchants;
import com.violentuso.vioEnchants.listeners.AutoSmeltListener;
import com.violentuso.vioEnchants.main.CustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VioCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("op")) {
            sender.sendMessage("§cНет прав.");
            return true;
        }

        boolean isPlayer = sender instanceof Player;
        Player p = isPlayer ? (Player) sender : null;

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            VioEnchants instance = VioEnchants.INSTANCE;
            instance.reloadConfig();
            instance.checkAndUpdateConfig();
            CustomEnchant.updateConfigValues();
            AutoSmeltListener.updateRecipes();
            sender.sendMessage("§a[VioEnchants] Конфигурация перезагружена.");
            return true;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("give")) {
            String enchantName = args[1];

            int level;
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cНеверный уровень.");
                return true;
            }

            CustomEnchant enchant = getEnchantByName(enchantName);
            if (enchant == null) {
                sender.sendMessage("§cНеизвестное зачарование.");
                return true;
            }

            // Если ко��анда из консоли — цель обязательна
            Player target;
            if (args.length >= 4) {
                target = Bukkit.getPlayerExact(args[3]);
                if (target == null) {
                    sender.sendMessage("§cИгрок §f" + args[3] + " §cне найден.");
                    return true;
                }
            } else {
                if (!isPlayer) {
                    sender.sendMessage("§cИз консоли укажите имя игрока: /vioenchant give <зачарование> <уровень> <игрок>");
                    return true;
                }
                target = p;
            }

            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();

            meta.getPersistentDataContainer().set(enchant.KEY, PersistentDataType.INTEGER, level);

            List<String> lore = new ArrayList<>();
            lore.add(enchant.getLoreLine(level));
            meta.setLore(lore);

            book.setItemMeta(meta);

            target.getInventory().addItem(book);

            String roman = CustomEnchant.toRoman(level);
            if (target == p) {
                sender.sendMessage("§aВыдана книга: §f" + enchant.NAME + " " + roman);
            } else {
                sender.sendMessage("§aВыдана книга §f" + enchant.NAME + " " + roman + " §aигроку §f" + target.getName());
                target.sendMessage("§aВам выдана книга: §f" + enchant.NAME + " " + roman);
            }
            return true;
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("enchant")) {
            if (!isPlayer) {
                sender.sendMessage("§cЭта команда доступна только для игроков.");
                return true;
            }

            ItemStack item = p.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) {
                p.sendMessage("§cВозьмите предмет в руку.");
                return true;
            }

            String enchantName = args[1];
            int level;
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                p.sendMessage("§cНеверный уровень.");
                return true;
            }

            CustomEnchant enchant = getEnchantByName(enchantName);
            if (enchant == null) {
                p.sendMessage("§cНе найдено.");
                return true;
            }

            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(enchant.KEY, PersistentDataType.INTEGER, level);

            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.removeIf(line -> line.contains(enchant.NAME));
            lore.add(enchant.getLoreLine(level));
            meta.setLore(lore);
            CustomEnchant.applyGlow(meta);

            item.setItemMeta(meta);
            p.sendMessage("§aПредмет зачарован.");
            return true;
        }

        return false;
    }

    private CustomEnchant getEnchantByName(String name) {
        for (CustomEnchant ce : CustomEnchant.values()) {
            if (ce.name().equalsIgnoreCase(name) || ce.NAME.equalsIgnoreCase(name)) {
                return ce;
            }
        }
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("give", "enchant", "reload");
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reload")) return List.of();
            return Arrays.stream(CustomEnchant.values())
                    .map(e -> e.name().toLowerCase())
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            return List.of("1", "2", "3", "4", "5");
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }

        return null;
    }
}