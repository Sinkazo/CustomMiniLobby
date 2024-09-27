package org.dark.customminilobby.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldManager {

    private final JavaPlugin plugin;

    public WorldManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void configureDayAndWeather() {
        boolean alwaysDay = plugin.getConfig().getBoolean("always-day", true);
        boolean neverRain = plugin.getConfig().getBoolean("never-rain", true);

        for (World world : Bukkit.getWorlds()) {
            // Configurar siempre de día
            if (alwaysDay) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (world.getTime() >= 12000) { // Si es de noche, cambiar a día
                            world.setTime(1000);
                        }
                    }
                }.runTaskTimer(plugin, 0, 40); // Revisar cada 2 segundos (100 ticks)
            }

            // Configurar sin lluvia
            if (neverRain) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (world.hasStorm() || world.isThundering()) {
                            world.setStorm(false);
                            world.setThundering(false);
                        }
                    }
                }.runTaskTimer(plugin, 0, 40); // Revisar cada 2 segundos (100 ticks)
            }
        }
    }
}
