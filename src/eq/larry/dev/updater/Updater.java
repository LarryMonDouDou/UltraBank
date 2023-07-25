package eq.larry.dev.updater;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import eq.larry.dev.UltraCore;
import net.Indyuce.mmoitems.MMOItems;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Updater extends BukkitRunnable {
    public void run() {
        Economy economy = UltraCore.get().getEconomy();
        FileConfiguration config = UltraCore.get().getConfig();
        for (Player player : Bukkit.getOnlinePlayers()) {
            int money = 0;
            HashMap<String, Integer> coinsTheyHave = new HashMap<>();
            for (ItemStack content : player.getInventory().getContents()) {
                if (content != null && content.getType() != Material.AIR && content.getItemMeta() != null) {
                    String coin = (String)UltraCore.get().getMaterialHashMap().get(content.getType());
                    if (coin != null) {
                        int amount = config.getInt("coins." + coin + ".worth");
                        int customModelData = config.getInt("coins." + coin + ".customModelData");
                        if (content.getItemMeta().hasCustomModelData() && content.getItemMeta().getCustomModelData() == customModelData) {
                            money += amount * content.getAmount();
                            coinsTheyHave.put(coin, Integer.valueOf(content.getAmount()));
                        }
                    }
                }
            }
            int oldAmount = 0;
            int currentAmount = (int)economy.getBalance((OfflinePlayer)player);
            if (UltraCore.get().getIgnoredPlayers().contains(player)) {
                oldAmount = currentAmount;
            } else if (UltraCore.get().getMoneyHashMap().get(player) != null) {
                oldAmount = ((Integer)UltraCore.get().getMoneyHashMap().getOrDefault(player, Integer.valueOf(0))).intValue();
            }
            if (oldAmount != currentAmount && (
                    currentAmount > oldAmount || oldAmount != 0)) {
                HashMap<String, Material> hashMap = new HashMap<>();
                UltraCore.get().getMaterialHashMap().keySet().forEach(material -> hashMap.put((String)UltraCore.get().getMaterialHashMap().get(material), material));
                updateCoins(player, oldAmount, currentAmount, hashMap);
            }
            economy.withdrawPlayer((OfflinePlayer)player, economy.getBalance((OfflinePlayer)player));
            economy.depositPlayer((OfflinePlayer)player, money);
            UltraCore.get().getMoneyHashMap().put(player, Integer.valueOf(money));
        }
    }

    public static void updateCoins(Player player, int oldAmount, int currentAmount, HashMap<String, Material> coinTypes) {
        int difference = currentAmount - oldAmount;
        FileConfiguration config = UltraCore.get().getConfig();
        int goldWorth = config.getInt("coins.gold.worth");
        int silverWorth = config.getInt("coins.silver.worth");
        int bronzeWorth = config.getInt("coins.bronze.worth");
        if (difference > 0) {
            if (Math.abs(difference) >= goldWorth) {
                int coinsToAdd = Math.abs(difference) / goldWorth;
                Bukkit.getScheduler().runTask((Plugin)UltraCore.get(), () -> addItemToInventory(player, MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get("MATERIAL"), config.getString("coins.gold.id")).newBuilder().build(), coinsToAdd));
                difference -= coinsToAdd * goldWorth;
            }
            if (Math.abs(difference) >= silverWorth) {
                int coinsToAdd = Math.abs(difference) / silverWorth;
                Bukkit.getScheduler().runTask((Plugin)UltraCore.get(), () -> addItemToInventory(player, MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get("MATERIAL"), config.getString("coins.silver.id")).newBuilder().build(), coinsToAdd));
                difference -= coinsToAdd * silverWorth;
            }
            if (difference != 0) {
                int coinsToAdd = Math.abs(difference) / bronzeWorth + ((Math.abs(difference) % bronzeWorth == 0) ? 0 : 1);
                Bukkit.getScheduler().runTask((Plugin)UltraCore.get(), () -> addItemToInventory(player, MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get("MATERIAL"), config.getString("coins.bronze.id")).newBuilder().build(), coinsToAdd));
            }
            return;
        }
        List<String> coinOrder = Arrays.asList(new String[] { "gold", "silver", "bronze" });
        for (String coin : coinOrder) {
            int worth = config.getInt("coins." + coin + ".worth");
            Material type = coinTypes.get(coin);
            for (ItemStack content : player.getInventory().getContents()) {
                if (difference == 0)
                    break;
                if (content != null && content.getType() == type && content.hasItemMeta() && content.getItemMeta().hasCustomModelData()) {
                    int customModelData = content.getItemMeta().getCustomModelData();
                    int coinStackValue = worth * content.getAmount();
                    if (difference < -coinStackValue) {
                        int amountToRemove = Math.abs(difference) / worth + ((Math.abs(difference) % worth == 0) ? 0 : 1);
                        ItemStack stackToRemove = content.clone();
                        stackToRemove.setAmount(amountToRemove);
                        player.getInventory().removeItem(new ItemStack[] { stackToRemove });
                        difference += worth * amountToRemove;
                    } else {
                        player.getInventory().removeItem(new ItemStack[] { content });
                        difference += coinStackValue;
                    }
                }
            }
        }
        if (difference != 0) {
            if (Math.abs(difference) >= goldWorth) {
                int coinsToAdd = Math.abs(difference) / goldWorth;
                Bukkit.getScheduler().runTask((Plugin)UltraCore.get(), () -> addItemToInventory(player, MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get("MATERIAL"), config.getString("coins.gold.id")).newBuilder().build(), coinsToAdd));
                difference -= coinsToAdd * goldWorth;
            }
            if (Math.abs(difference) >= silverWorth) {
                int coinsToAdd = Math.abs(difference) / silverWorth;
                Bukkit.getScheduler().runTask((Plugin)UltraCore.get(), () -> addItemToInventory(player, MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get("MATERIAL"), config.getString("coins.silver.id")).newBuilder().build(), coinsToAdd));
                difference -= coinsToAdd * silverWorth;
            }
            if (difference != 0) {
                int coinsToAdd = Math.abs(difference) / bronzeWorth + ((Math.abs(difference) % bronzeWorth == 0) ? 0 : 1);
                Bukkit.getScheduler().runTask((Plugin)UltraCore.get(), () -> addItemToInventory(player, MMOItems.plugin.getMMOItem(MMOItems.plugin.getTypes().get("MATERIAL"), config.getString("coins.bronze.id")).newBuilder().build(), coinsToAdd));
            }
        }
    }

    public static int getCoinValue(Material material) {
        return UltraCore.get().getConfig().getInt("coins." + (String)UltraCore.get().getMaterialHashMap().get(material) + ".worth");
    }

    public static void addItemToInventory(Player player, ItemStack item, int amount) {
        if (amount <= 0)
            return;
        int maxStackSize = item.getMaxStackSize();
        int fullStacks = amount / maxStackSize;
        int remainingAmount = amount % maxStackSize;
        for (int i = 0; i < fullStacks; i++) {
            ItemStack stack = item.clone();
            stack.setAmount(maxStackSize);
            HashMap<Integer, ItemStack> leftoverItems = player.getInventory().addItem(new ItemStack[] { stack });
            if (!leftoverItems.isEmpty())
                for (ItemStack leftover : leftoverItems.values())
                    Bukkit.getScheduler().runTask((Plugin)UltraCore.get(), () -> player.getWorld().dropItem(player.getLocation(), leftover));
        }
        if (remainingAmount > 0) {
            ItemStack stack = item.clone();
            stack.setAmount(remainingAmount);
            HashMap<Integer, ItemStack> leftoverItems = player.getInventory().addItem(new ItemStack[] { stack });
            if (!leftoverItems.isEmpty())
                for (ItemStack leftover : leftoverItems.values())
                    Bukkit.getScheduler().runTask((Plugin)UltraCore.get(), () -> player.getWorld().dropItem(player.getLocation(), leftover));
        }
    }
}