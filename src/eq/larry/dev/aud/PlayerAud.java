package eq.larry.dev.aud;

import eq.larry.dev.UltraCore;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class PlayerAud implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UltraCore.get().getIgnoredPlayers().add(event.getPlayer());
        Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)UltraCore.get(), () -> UltraCore.get().getIgnoredPlayers().remove(event.getPlayer()), 5L);
    }
}