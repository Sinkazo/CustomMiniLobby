package org.dark.customminilobby;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PluginFileManager {
    private final JavaPlugin plugin;

    public PluginFileManager(JavaPlugin plugin) {
        this.plugin = plugin;
        createPluginFolder();
        loadConfig();
    }

    private void createPluginFolder() {
        // Verificar si la carpeta de datos del plugin existe
        File pluginFolder = plugin.getDataFolder();
        if (!pluginFolder.exists()) {
            if (pluginFolder.mkdirs()) {
                plugin.getLogger().info("Plugin folder created successfully.");
            } else {
                plugin.getLogger().warning("Failed to create plugin folder.");
            }
        }
    }

    private void loadConfig() {
        // Verificar si el archivo config.yml existe
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            // Guardar el archivo config.yml predeterminado
            plugin.saveDefaultConfig();
            plugin.getLogger().info("Default config.yml created.");
        } else {
            plugin.getLogger().info("Config.yml already exists.");
        }
        // Recargar la configuración después de cargar o crear el archivo
        plugin.reloadConfig();
    }
}
