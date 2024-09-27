package org.dark.customminilobby;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CustomScoreboard {
    private final JavaPlugin plugin;
    private BukkitRunnable updateTask;

    public CustomScoreboard(JavaPlugin plugin) {
        this.plugin = plugin;
        createScoreboard();
    }

    public void createScoreboard() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        startUpdatingScoreboard();
        updateScoreboard(); // Actualizamos el scoreboard al inicio
    }

    public void updateScoreboard() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerScoreboard(player);
        }
    }

    public void applyToPlayer(Player player) {
        updatePlayerScoreboard(player);
    }

    private void updatePlayerScoreboard(Player player) {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            return;
        }

        Scoreboard board = player.getScoreboard();
        if (board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        // Limpiar el scoreboard anterior
        if (board.getObjective("lobby") != null) {
            board.getObjective("lobby").unregister();
        }
        for (Team team : board.getTeams()) {
            team.unregister();
        }

        // Crear nuevo objetivo
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.title", "&eServidor"));
        Objective obj = board.registerNewObjective("lobby", "dummy", title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        FileConfiguration config = plugin.getConfig();
        List<String> lines = config.getStringList("scoreboard.lines");

        int lineCount = 0;
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            if (line.equalsIgnoreCase("blank")) {
                line = getUniqueBlankLine(lineCount);
            } else {
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    line = PlaceholderAPI.setPlaceholders(player, line);
                }
                line = line.replace("%playername%", player.getName());
                line = ChatColor.translateAlternateColorCodes('&', line);
            }

            Team team = board.registerNewTeam("line" + lineCount);
            String entry = getUniqueCode(lineCount);
            team.addEntry(entry);
            team.setPrefix(line);
            team.setSuffix("");
            obj.getScore(entry).setScore(lineCount + 1);

            lineCount++;
        }

        player.setScoreboard(board);
    }

    private void startUpdatingScoreboard() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateScoreboard();
            }
        };
        updateTask.runTaskTimer(plugin, 0L, 20L); // Actualiza cada segundo (20 ticks)
    }

    private String getUniqueBlankLine(int lineCount) {
        return ChatColor.values()[lineCount % 16] + "" + ChatColor.values()[(lineCount + 1) % 16];
    }

    private String getUniqueCode(int lineCount) {
        return ChatColor.values()[lineCount % 16] + "" + ChatColor.RESET;
    }

    public void stopUpdatingScoreboard() {
        if (updateTask != null) {
            updateTask.cancel();
        }
    }
}