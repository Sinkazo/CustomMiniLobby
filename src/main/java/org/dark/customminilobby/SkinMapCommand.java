package org.dark.customminilobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class SkinMapCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Uso correcto: /mostrarcara <nombre_jugador>");
            return true;
        }

        Player player = (Player) sender;
        String targetName = args[0];

        try {
            // Descargar la skin del jugador
            URL url = new URL("https://minotar.net/skin/" + targetName);
            BufferedImage skin = ImageIO.read(url);

            // Extraer la cara (8x8 píxeles)
            BufferedImage face = skin.getSubimage(8, 8, 8, 8);
            BufferedImage scaledFace = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaledFace.createGraphics();
            g.drawImage(face, 0, 0, 128, 128, null);
            g.dispose();

            // Crear un nuevo mapa
            ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);
            MapView mapView = Bukkit.createMap(player.getWorld());
            mapView.getRenderers().clear();

            // Añadir un renderizador personalizado
            mapView.addRenderer(new MapRenderer() {
                @Override
                public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                    mapCanvas.drawImage(0, 0, scaledFace);
                }
            });

            // Asignar el mapa al item
            MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();
            mapMeta.setMapId(mapView.getId());
            mapItem.setItemMeta(mapMeta);

            // Dar el mapa al jugador
            player.getInventory().addItem(mapItem);
            player.sendMessage(ChatColor.GREEN + "Se ha mostrado la cara del jugador " + targetName + " en un mapa.");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "No se pudo cargar la skin del jugador.");
            e.printStackTrace();
        }

        return true;
    }
}
