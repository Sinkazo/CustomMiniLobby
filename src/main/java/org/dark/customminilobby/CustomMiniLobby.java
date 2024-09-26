package org.dark.customminilobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class CustomMiniLobby extends JavaPlugin {

    private CustomScoreboard customScoreboard;

    @Override
    public void onEnable() {
        // Cargar o crear la configuración desde el config.yml
        this.saveDefaultConfig();

        // Inicializar el CustomScoreboard y crear el scoreboard
        customScoreboard = new CustomScoreboard(this);
        customScoreboard.createScoreboard();

        // Mensaje en la consola al iniciar el plugin
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Se ha iniciado CustomMiniLobby correctamente");

        // Registro de eventos y comandos
        Bukkit.getPluginManager().registerEvents(new PlayerManager(this), this);
        this.getCommand("cml").setExecutor(new CMLCommand(this));
        getServer().getPluginManager().registerEvents(new ItemEventListener(this), this);

        // Aplicar el scoreboard a todos los jugadores conectados cuando el servidor arranca
        for (Player player : Bukkit.getOnlinePlayers()) {
            customScoreboard.applyToPlayer(player);
        }
    }

    @Override
    public void onDisable() {
        // Mensaje en la consola al desactivar el plugin
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Se ha desactivado CustomMiniLobby");
    }

    public CustomScoreboard getCustomScoreboard() {
        return customScoreboard;
    }

}
