package eq.larry.dev;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import eq.larry.dev.aud.PlayerAud;
import eq.larry.dev.updater.Updater;
public final class UltraCore extends JavaPlugin {
    static UltraCore plugin;

    Economy economy;

    HashMap<Material, String> materialHashMap;

    HashMap<Player, Integer> moneyHashMap;

    List<Player> ignoredPlayers;

    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Impossible de trouver Vault installé !");
            setEnabled(false);
            return;
        }
        if (!setupEconomy()) {
            getLogger().severe("Impossible de configurer l’économie ! ");
            setEnabled(false);
            return;
        }
        this.materialHashMap = new HashMap<>();
        this.moneyHashMap = new HashMap<>();
        this.ignoredPlayers = new ArrayList<>();
        try {
            for (String coins : ((ConfigurationSection)Objects.<ConfigurationSection>requireNonNull(getConfig().getConfigurationSection("coins"))).getKeys(false))
                this.materialHashMap.put(Material.getMaterial(Objects.<String>requireNonNull(getConfig().getString("coins." + coins + ".type"))), coins);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        (new Updater()).runTaskTimerAsynchronously((Plugin)this, 0L, 2L);
        getServer().getPluginManager().registerEvents((Listener)new PlayerAud(), (Plugin)this);
    }

    public void onDisable() {}

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        this.economy = (Economy)rsp.getProvider();
        return true;
    }

    public static UltraCore get() {
        return plugin;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public HashMap<Material, String> getMaterialHashMap() {
        return this.materialHashMap;
    }

    public HashMap<Player, Integer> getMoneyHashMap() {
        return this.moneyHashMap;
    }

    public List<Player> getIgnoredPlayers() {
        return this.ignoredPlayers;
    }
}