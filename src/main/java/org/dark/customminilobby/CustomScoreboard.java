package org.dark.customminilobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.List;

public class CustomScoreboard {
    private final JavaPlugin plugin;
    private Scoreboard scoreboard;
    private Objective objective;

    public CustomScoreboard(JavaPlugin plugin) {
        this.plugin = plugin;
        createScoreboard();
    }

    public void createScoreboard() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective("lobby", "dummy", ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("scoreboard.title", "&eServidor")));
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

        updateScoreboard();
    }

    public void updateScoreboard() {
        FileConfiguration config = plugin.getConfig();

        // Limpiamos cualquier línea previa en el scoreboard
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        List<String> lines = config.getStringList("scoreboard.lines");

        int scoreValue = lines.size();  // Se usa para ordenar las líneas desde la parte superior
        for (String line : lines) {
            if (line.contains("%playername%")) {
                line = line.replace("%playername%", "{player}"); // Se reemplazará después por el nombre del jugador
            }
            if (!line.isEmpty()) {
                Score score = objective.getScore(ChatColor.translateAlternateColorCodes('&', line));
                score.setScore(scoreValue);
                scoreValue--;
            }
        }
    }

    public void applyToPlayer(Player player) {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            return; // Si el scoreboard no está habilitado, no hacer nada
        }

        // Reemplazamos %playername% por el nombre real del jugador
        Scoreboard personalScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective personalObjective = personalScoreboard.registerNewObjective("lobby", "dummy", objective.getDisplayName());
        personalObjective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

        // Agregamos las líneas al scoreboard personal del jugador
        for (String entry : scoreboard.getEntries()) {
            String updatedEntry = entry.replace("{player}", player.getName());
            Score score = personalObjective.getScore(updatedEntry);
            score.setScore(scoreboard.getObjective("lobby").getScore(entry).getScore());
        }

        player.setScoreboard(personalScoreboard);
    }
}
