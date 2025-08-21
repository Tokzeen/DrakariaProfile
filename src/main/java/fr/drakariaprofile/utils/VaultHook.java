package fr.drakariaprofile.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private static Economy econ;

    public static void setupEconomy() {
        if (econ != null) return;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) econ = rsp.getProvider();
    }

    public static boolean deposit(Player player, double amount) {
        if (econ == null) setupEconomy();
        if (econ == null) return false;
        econ.depositPlayer(player, amount);
        return true;
    }
}
