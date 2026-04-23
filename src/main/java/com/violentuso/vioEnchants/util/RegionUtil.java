package com.violentuso.vioEnchants.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;
import java.util.Map;

public class RegionUtil {

    private static boolean hasWorldGuard = false;

    static {
        hasWorldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }


    public static boolean isEnchantBlocked(Location location, List<String> blockedRegions, List<String> blockedFlags) {
        if (!hasWorldGuard) return false;
        if (location == null || location.getWorld() == null) return false;

        try {
            com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(location);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet regionSet = query.getApplicableRegions(weLoc);


            if (blockedRegions != null && !blockedRegions.isEmpty()) {
                for (ProtectedRegion region : regionSet) {
                    if (blockedRegions.contains(region.getId())) {
                        return true;
                    }
                }
            }


            if (blockedFlags != null && !blockedFlags.isEmpty()) {
                for (String flagName : blockedFlags) {
                    if (isFlagDenied(regionSet, flagName)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {

            return false;
        }

        return false;
    }


    private static boolean isFlagDenied(ApplicableRegionSet regionSet, String flagName) {
        try {
            FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
            var flag = flagRegistry.get(flagName);

            if (flag instanceof StateFlag stateFlag) {
                StateFlag.State state = regionSet.queryState(null, stateFlag);
                return state == StateFlag.State.DENY;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static boolean hasWorldGuard() {
        return hasWorldGuard;
    }
}