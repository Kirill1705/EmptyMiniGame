package thor.emptyMiniGame;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

class MiniGameListener implements Listener {
    MiniGameConfig config;
    MiniGameLoader loader;
    public MiniGameListener(MiniGameConfig config, MiniGameLoader loader) {
        this.config=config;
        this.loader=loader;
    }
    @EventHandler
    public void onTap(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack itemStack = event.getItem();
        if (action.isRightClick()&&itemStack==config.getStartItem()) {
            loader.addPlayer(player);
        }
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        MiniGame game = loader.getPlayerMiniGame(player);
        if (game!=null&&config.autoRemoveByDeath()) {
            loader.removePlayer(player);
            game.removePlayer(player);
        }
    }
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        MiniGame game = loader.getPlayerMiniGame(player);
        if (game==null) {
            player.performCommand("lobby");
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getWorld()== loader.getWorld()) {
            player.performCommand("lobby");
        }
    }
    @EventHandler (priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to==config.getLobby()&&from!=config.getLobby()) {
            Inventory inv = player.getInventory();
            ItemStack itemStack = config.getStartItem();
            if (!inv.contains(itemStack)) {
                loader.giveItem(player);
            }
        }
    }
    @EventHandler
    public void onExit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        MiniGame game = loader.getPlayerMiniGame(player);
        if (game!=null) {
            loader.removePlayer(player);
            game.removePlayer(player);
        }
    }

}
