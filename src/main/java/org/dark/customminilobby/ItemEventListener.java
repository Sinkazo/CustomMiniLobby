package org.dark.customminilobby;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemEventListener implements Listener {

    private final CustomMiniLobby plugin; // Referencia a la clase principal
    private final Set<String> playersWithItems = new HashSet<>(); // Almacena los jugadores que ya recibieron los ítems

    public ItemEventListener(CustomMiniLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Comprobar si se permite dar ítems al entrar
        if (plugin.getConfig().getBoolean("items-join-enabled")) {
            for (String key : plugin.getConfig().getConfigurationSection("items").getKeys(false)) {
                String id = plugin.getConfig().getString("items." + key + ".id");
                int amount = plugin.getConfig().getInt("items." + key + ".amount");
                String displayName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("items." + key + ".displayname"));
                List<String> lore = plugin.getConfig().getStringList("items." + key + ".lore");
                lore.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line)); // Traducir lore

                // Verificar si la cantidad es mayor que 0
                if (amount <= 0) {
                    plugin.getLogger().warning("La cantidad de " + key + " debe ser mayor que 0.");
                    continue; // Salta a la siguiente iteración
                }

                // Crea el ítem
                Material material = Material.getMaterial(id);
                if (material != null) {
                    ItemStack item = new ItemStack(material, amount);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(displayName);
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }

                    // Verifica si el jugador ya tiene el ítem en el inventario
                    if (!player.getInventory().contains(item.getType(), item.getAmount())) {
                        // Verifica si el slot especificado está vacío
                        int slot = plugin.getConfig().getInt("items." + key + ".slot");
                        if (player.getInventory().getItem(slot) == null) {
                            player.getInventory().setItem(slot, item); // Establecer el ítem en el slot correspondiente
                        } else {
                            plugin.getLogger().info("El jugador " + player.getName() + " ya tiene el ítem " + displayName + " en el slot " + slot + ".");
                        }
                    } else {
                        plugin.getLogger().info("El jugador " + player.getName() + " ya tiene el ítem " + displayName + " en su inventario.");
                    }
                } else {
                    plugin.getLogger().warning("Material " + id + " no encontrado para " + key + ".");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Asegúrate de que el jugador está haciendo clic con un ítem en la mano
        if (item != null && (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR ||
                event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) {

            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String displayName = item.getItemMeta().getDisplayName();

                // Busca el ítem en la configuración
                for (String key : plugin.getConfig().getConfigurationSection("items").getKeys(false)) {
                    String configuredDisplayName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("items." + key + ".displayname"));
                    if (configuredDisplayName.equalsIgnoreCase(displayName)) { // Comparar sin considerar mayúsculas
                        String command = plugin.getConfig().getString("items." + key + ".command");
                        if (command != null && !command.isEmpty()) {
                            executeCommand(command.replace("%player%", player.getName()), player);
                        } else {
                            player.sendMessage(ChatColor.RED + "El comando está vacío para el ítem " + displayName + ".");
                        }
                        return; // Salir después de ejecutar el comando
                    }
                }
                // Mensaje de depuración si no se encuentra el ítem
                player.sendMessage(ChatColor.RED + "No se encontró un comando asociado para el ítem " + displayName + ".");
            }
        }
    }

    private void executeCommand(String command, Player player) {
        // Manejo de comandos según su tipo
        if (command.startsWith("[msg]")) {
            String message = command.replace("[msg]", "").trim();
            // Muestra el mensaje al jugador
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message)); // Enviar el mensaje al jugador
        } else if (command.startsWith("[player]")) {
            String playerCommand = command.replace("[player]", "").trim();
            player.performCommand(playerCommand); // Ejecutar como si fuera el jugador
        } else if (command.startsWith("[console]")) {
            String consoleCommand = command.replace("[console]", "").trim();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
        }
    }
}
