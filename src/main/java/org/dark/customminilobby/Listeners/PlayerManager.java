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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.dark.customminilobby.CustomMiniLobby;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
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

        // Mostrar mensaje de bienvenida con la cara del jugador
        if (config.getBoolean("send-welcome-message", true)) {
            sendWelcomeMessageWithFace(player);
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

    // Método para enviar el mensaje de bienvenida con la cara del jugador
    private void sendWelcomeMessageWithFace(Player player) {
        FileConfiguration config = plugin.getConfig();
        String targetName = player.getName();

        try {
            // Descargar la skin del jugador
            URL url = new URL("https://minotar.net/skin/" + targetName);
            BufferedImage skin = ImageIO.read(url);

            // Extraer la cara (8x8 píxeles)
            BufferedImage face = skin.getSubimage(8, 8, 8, 8);

            // Convertir la cara en líneas de texto coloreadas
            String[] faceLines = new String[8];
            for (int y = 0; y < face.getHeight(); y++) {
                StringBuilder line = new StringBuilder();
                for (int x = 0; x < face.getWidth(); x++) {
                    int rgb = face.getRGB(x, y);
                    ChatColor closestColor = getClosestChatColor(new Color(rgb, true));
                    line.append(closestColor).append("█"); // Usamos el carácter █ para simular el píxel
                }
                faceLines[y] = line.toString();
            }

            // Leer el mensaje de bienvenida desde el config.yml
            List<String> welcomeMessages = config.getStringList("welcome-message");
            for (String message : welcomeMessages) {
                // Reemplazar los placeholders con las líneas de la cara y otros valores
                message = message
                        .replace("{caralinea1}", faceLines[0])
                        .replace("{caralinea2}", faceLines[1])
                        .replace("{caralinea3}", faceLines[2])
                        .replace("{caralinea4}", faceLines[3])
                        .replace("{caralinea5}", faceLines[4])
                        .replace("{caralinea6}", faceLines[5])
                        .replace("{caralinea7}", faceLines[6])
                        .replace("{caralinea8}", faceLines[7])
                        .replace("{player}", player.getName());

                // Enviar el mensaje procesado al jugador
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }

        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "No se pudo cargar la skin del jugador.");
            e.printStackTrace();
        }
    }

    // Método para obtener el color de chat más cercano al color dado
    private ChatColor getClosestChatColor(Color color) {
        ChatColor[] colors = ChatColor.values();
        ChatColor closest = ChatColor.WHITE;
        double minDistance = Double.MAX_VALUE;

        for (ChatColor chatColor : colors) {
            if (!chatColor.isColor()) continue;

            Color awtColor = chatColor.asBungee().getColor();
            double distance = getColorDistance(color, awtColor);
            if (distance < minDistance) {
                minDistance = distance;
                closest = chatColor;
            }
        }

        return closest;
    }

    // Método para calcular la distancia entre dos colores
    private double getColorDistance(Color c1, Color c2) {
        int redDiff = c1.getRed() - c2.getRed();
        int greenDiff = c1.getGreen() - c2.getGreen();
        int blueDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);
    }

    // Método para reproducir el sonido de bienvenida
    private void playWelcomeSound(Player player) {
        FileConfiguration config = plugin.getConfig();
        String sound = config.getString("welcome-sound", "ENTITY_PLAYER_LEVELUP");

        try {
            Sound soundEnum = Sound.valueOf(sound.toUpperCase());
            player.playSound(player.getLocation(), soundEnum, 1.0F, 1.0F);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Sound " + sound + " is not valid. Check your config.yml.");
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
