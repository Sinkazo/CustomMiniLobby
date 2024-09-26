package org.dark.customminilobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CMLCommand implements CommandExecutor {
    private final CustomMiniLobby plugin;
    private final CustomScoreboard customScoreboard;

    public CMLCommand(CustomMiniLobby plugin) {
        this.plugin = plugin;
        this.customScoreboard = plugin.getCustomScoreboard();
    }

    private String translateMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(path, "Message not set in config.yml."));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                handleSpawnCommand((Player) sender);
            } else {
                sender.sendMessage(translateMessage("spawn.only-players-message"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("cml.spawn")) {
                    handleSetSpawnCommand(player);
                } else {
                    player.sendMessage(translateMessage("spawn.no-permission-message"));
                }
            } else {
                sender.sendMessage(translateMessage("spawn.only-players-message"));
            }
        } else if (args[0].equalsIgnoreCase("del")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("cml.spawn")) {
                    handleDeleteSpawnCommand(player);
                } else {
                    player.sendMessage(translateMessage("spawn.no-permission-message"));
                }
            } else {
                sender.sendMessage(translateMessage("spawn.only-players-message"));
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            handleReloadCommand(sender);
        } else {
            sender.sendMessage(ChatColor.RED + "Uso incorrecto. Usa /cml, /cml set, /cml del o /cml reload.");
        }

        return true;
    }

    private void handleSpawnCommand(Player player) {
        FileConfiguration config = plugin.getConfig();

        if (!config.getBoolean("spawn.enabled", true)) {
            player.sendMessage(translateMessage("spawn.spawn-disabled-message"));
            return;
        }

        if (!config.isSet("spawn.location.world")) {
            player.sendMessage(translateMessage("spawn.no-spawn-set-message"));
            return;
        }

        World world = Bukkit.getWorld(config.getString("spawn.location.world"));
        if (world == null) {
            player.sendMessage(translateMessage("spawn.world-not-exist-message"));
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
        player.sendMessage(translateMessage("spawn.teleported-message"));
    }

    private void handleSetSpawnCommand(Player player) {
        FileConfiguration config = plugin.getConfig();

        if (config.isSet("spawn.location.world")) {
            player.sendMessage(translateMessage("spawn.already-set-message"));
            return;
        }

        Location loc = player.getLocation();
        config.set("spawn.location.world", loc.getWorld().getName());
        config.set("spawn.location.x", loc.getX());
        config.set("spawn.location.y", loc.getY());
        config.set("spawn.location.z", loc.getZ());
        config.set("spawn.location.yaw", loc.getYaw());
        config.set("spawn.location.pitch", loc.getPitch());

        plugin.saveConfig();
        player.sendMessage(translateMessage("spawn.location-set-message"));
    }

    private void handleDeleteSpawnCommand(Player player) {
        FileConfiguration config = plugin.getConfig();

        if (!config.isSet("spawn.location.world")) {
            player.sendMessage(translateMessage("spawn.no-spawn-set-message"));
            return;
        }

        config.set("spawn.location", null);
        plugin.saveConfig();
        player.sendMessage(translateMessage("spawn.location-deleted-message"));
    }

    private void handleReloadCommand(CommandSender sender) {
        plugin.reloadConfig();
        customScoreboard.updateScoreboard();
        sender.sendMessage(ChatColor.GREEN + "La configuración, el scoreboard y los items han sido recargados correctamente.");

        // Aplicar el scoreboard recargado y los nuevos items a todos los jugadores en línea
        for (Player player : Bukkit.getOnlinePlayers()) {
            customScoreboard.applyToPlayer(player);
        }
    }
}
