package org.dark.customminilobby.Listeners;

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
import org.dark.customminilobby.CustomMiniLobby;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemEventListener implements Listener {

    private final CustomMiniLobby plugin;
    private final Set<String> playersWithItems = new HashSet<>();

    public ItemEventListener(CustomMiniLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().getBoolean("items-join-enabled")) {
            for (String key : plugin.getConfig().getConfigurationSection("items").getKeys(false)) {
                String id = plugin.getConfig().getString("items." + key + ".id");
                int amount = plugin.getConfig().getInt("items." + key + ".amount");
                String displayName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("items." + key + ".displayname"));
                List<String> lore = plugin.getConfig().getStringList("items." + key + ".lore");
                lore.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));

                if (amount <= 0) {
                    plugin.getLogger().warning("La cantidad de " + key + " debe ser mayor que 0.");
                    continue;
                }

                Material material = Material.getMaterial(id);
                if (material != null) {
                    ItemStack item = new ItemStack(material, amount);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(displayName);
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }

                    if (!player.getInventory().contains(item.getType(), item.getAmount())) {
                        int slot = plugin.getConfig().getInt("items." + key + ".slot");
                        if (player.getInventory().getItem(slot) == null) {
                            player.getInventory().setItem(slot, item);
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

        if (item != null && (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR ||
                event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) {

            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String displayName = item.getItemMeta().getDisplayName();

                for (String key : plugin.getConfig().getConfigurationSection("items").getKeys(false)) {
                    String configuredDisplayName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("items." + key + ".displayname"));
                    if (configuredDisplayName.equalsIgnoreCase(displayName)) {
                        String command = plugin.getConfig().getString("items." + key + ".command");
                        if (command != null && !command.isEmpty()) {
                            executeCommand(command.replace("%player%", player.getName()), player);
                        } else {
                            player.sendMessage(ChatColor.RED + "El comando está vacío para el ítem " + displayName + ".");
                        }
                        return;
                    }
                }
                player.sendMessage(ChatColor.RED + "No se encontró un comando asociado para el ítem " + displayName + ".");
            }
        }
    }

    private void executeCommand(String command, Player player) {
        command = command.trim(); // Asegúrate de eliminar espacios al principio y al final

        if (command.startsWith("[msg]")) {
            String message = command.replace("[msg]", "").trim();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        } else if (command.startsWith("[player]")) {
            String playerCommand = command.replace("[player]", "").trim();

            // Crear un nuevo objeto PlayerCommandPreprocessEvent
            org.bukkit.event.player.PlayerCommandPreprocessEvent playerCommandEvent =
                    new org.bukkit.event.player.PlayerCommandPreprocessEvent(player, "/" + playerCommand);

            // Llamar al evento
            Bukkit.getPluginManager().callEvent(playerCommandEvent);

            // Verificar si el evento fue cancelado
            if (!playerCommandEvent.isCancelled()) {
                // Si no fue cancelado, ejecuta el comando
                player.performCommand(playerCommand);
            }
        } else if (command.startsWith("[console]")) {
            String consoleCommand = command.replace("[console]", "").trim();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand);
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}