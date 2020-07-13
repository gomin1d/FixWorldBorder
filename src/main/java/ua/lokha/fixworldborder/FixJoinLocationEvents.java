package ua.lokha.fixworldborder;

import lombok.extern.java.Log;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Поскольку я уменьшил границы мира, некоторые игроки остались за границей и вечно там умирают.
 * Этот фикс просто перемещает их на спавн, если они находятся за границей.
 */
@Log
public class FixJoinLocationEvents implements Listener {
	
	private Main main;

	public FixJoinLocationEvents(Main main) {
		this.main = main;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void on(PlayerJoinEvent event) {
		Location from = event.getPlayer().getLocation();
		Location fixLocation = this.fixLocation(from);
		if(fixLocation != null){
			event.getPlayer().teleport(fixLocation);

			// телепорт может не сработать, по этому пробуем еще раз через секунду
			Bukkit.getScheduler().runTaskLater(main, ()->event.getPlayer().teleport(fixLocation), 20);

			event.getPlayer().sendMessage("§cВы были за границей мира, мы переместили вас на спавн.");
			log.warning("PlayerJoinEvent: Игрок " + event.getPlayer().getName() + " вошел на сервер за границей мира " + fixLocation.getWorld().getName() + ", перемещаем его " +
					from.getBlockX() + " " + from.getBlockY() + " " + from.getBlockZ() + " -> " +
					fixLocation.getBlockX() + " " + fixLocation.getBlockY() + " " + fixLocation.getBlockZ());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void on(PlayerRespawnEvent event) {
		Location from = event.getRespawnLocation();
		Location fixLocation = this.fixLocation(from);
		if(fixLocation != null){
			event.setRespawnLocation(fixLocation);
			event.getPlayer().sendMessage("§cВы были за границей мира, мы переместили вас на спавн.");
			log.warning("Игрок " + event.getPlayer().getName() + " попытался зареспавниться за границей мира " + fixLocation.getWorld().getName() + ", перемещаем его " +
					from.getBlockX() + " " + from.getBlockY() + " " + from.getBlockZ() + " -> " +
					fixLocation.getBlockX() + " " + fixLocation.getBlockY() + " " + fixLocation.getBlockZ());
		}
	}

	public Location fixLocation(Location loc) {
		World world = loc.getWorld();
		WorldBorder border = world.getWorldBorder();
		if(!border.isInside(loc)){
			for(int y = 1000; y > 0; y--){
				Block block = world.getBlockAt(0, y, 0);
				if(block.getType() != Material.AIR){
					return new Location(world, 0, y, 0);
				}
			}
			return new Location(world, 0, 100, 0);
		}
		return null;
	}
}
