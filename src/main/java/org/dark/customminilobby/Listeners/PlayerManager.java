package org.dark.customminilobby.Listeners;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.dark.customminilobby.CustomMiniLobby;

import java.util.List;

public class PlayerManager implements Listener {
    private final CustomMiniLobby plugin;

    public PlayerManager(CustomMiniLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        FileConfiguration config = plugin.getConfig();
        Player player = event.getPlayer();
        event.setJoinMessage("");
        plugin.getCustomScoreboard().applyToPlayer(player);

        // Verificar si la funcionalidad de spawn está habilitada
        if (!config.getBoolean("spawn.enabled", true)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("spawn.spawn-disabled-message")));
            return;
        }

        // Modo de juego por defecto
        String defaultGameModeString = config.getString("spawn.default-game-mode", "hardcore").toUpperCase();
        GameMode defaultGameMode;
        try {
            defaultGameMode = GameMode.valueOf(defaultGameModeString);
        } catch (IllegalArgumentException e) {
            defaultGameMode = GameMode.SURVIVAL;
            Bukkit.getLogger().warning("El modo de juego por defecto en config.yml no es válido. Usando SURVIVAL.");
        }
        player.setGameMode(defaultGameMode);

        // Ocultar jugadores si está habilitado
        boolean hidePlayersEnabled = config.getBoolean("spawn.hide-players", false);
        if (hidePlayersEnabled) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.hidePlayer(plugin, player);
                player.hidePlayer(plugin, onlinePlayer);
            }
        }

        // Teletransportar si always-teleport está habilitado
        boolean alwaysTeleportEnabled = config.getBoolean("spawn.always-teleport", false);
        if (alwaysTeleportEnabled) {
            teleportToSpawn(player);
        }

        // Mostrar mensaje de bienvenida si está habilitado
        if (config.getBoolean("send-welcome-message", true)) {
            sendWelcomeMessage(player);
        }

        // Reproducir sonido de bienvenida si está habilitado
        if (config.getBoolean("send-welcome-sound", true)) {
            playWelcomeSound(player);
        }
    }

    // Método para teletransportar al spawn
    private void teleportToSpawn(Player player) {
        FileConfiguration config = plugin.getConfig();

        if (!config.isSet("spawn.location.world")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("spawn.no-spawn-set-message")));
            return;
        }

        World world = Bukkit.getWorld(config.getString("spawn.location.world"));
        if (world == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("spawn.world-not-exist-message")));
            return;
        }

        Location spawnLocation = new Location(
                world,
                config.getDouble("spawn.location.x"),
                config.getDouble("spawn.location.y"),
                config.getDouble("spawn.location.z"),
                (float) config.getDouble("spawn.location.yaw"),
                (float) config.getDouble("spawn.location.pitch")
        );

        player.teleport(spawnLocation);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("spawn.teleported-message")));
    }

    // Método para enviar el mensaje de bienvenida
    private void sendWelcomeMessage(Player player) {
        FileConfiguration config = plugin.getConfig();
        List<String> welcomeMessages = config.getStringList("welcome-message");

        for (String message : welcomeMessages) {
            if (message.isEmpty()) {
                player.sendMessage("");
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{player}", player.getName())));
            }
        }
    }

    // Método para reproducir el sonido de bienvenida
    private void playWelcomeSound(Player player) {
        FileConfiguration config = plugin.getConfig();
        String sound = config.getString("welcome-sound", "ENTITY_PLAYER_LEVELUP");

        try {
            Sound soundEnum = Sound.valueOf(sound.toUpperCase());
            player.playSound(player.getLocation(), soundEnum, 1.0F, 1.0F);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("El sonido " + sound + " no es válido. Verifica tu config.yml.");
        }
    }

    // Otros eventos, como los de no-break, no-drop, etc., se mantienen igual...

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        FileConfiguration config = plugin.getConfig();
        Player player = event.getPlayer();
        if (config.getBoolean("no-break", true) && !player.getGameMode().equals(GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        FileConfiguration config = plugin.getConfig();
        Player player = event.getPlayer();
        if (config.getBoolean("no-drop", true) && !player.getGameMode().equals(GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        FileConfiguration config = plugin.getConfig();
        Player player = event.getPlayer();
        if (config.getBoolean("no-pickup", true) && !player.getGameMode().equals(GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        FileConfiguration config = plugin.getConfig();
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (config.getBoolean("no-damage", true) && !player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        FileConfiguration config = plugin.getConfig();
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (config.getBoolean("no-pvp", true) && !player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        FileConfiguration config = plugin.getConfig();
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (config.getBoolean("no-inventoryclick", true) && !player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        FileConfiguration config = plugin.getConfig();
        if (config.getBoolean("no-hungry", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage("");
    }
}
