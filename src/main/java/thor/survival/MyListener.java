package thor.survival;

import com.destroystokyo.paper.Title;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class MyListener implements Listener {
    @EventHandler
    public void onHanger(FoodLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (entity.getLocation().getWorld()==Survival.lobby) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onTap(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (event.getItem()!=null) {
            ItemStack itemStack = event.getItem();
            int data = 0;
            if (itemStack.getItemMeta().hasCustomModelData()) {
                data = itemStack.getItemMeta().getCustomModelData();
            }
            Material material = itemStack.getType();
            boolean isRMB = action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK);
            if (data == 1 && material==Material.YELLOW_DYE&& isRMB) {
                Survival.addPlayer(player);
            }
        }
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Survival.removePlayer(player, player.getKiller());
    }
    @EventHandler
    public void onExit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Survival.removePlayer(player, player.getKiller());
    }
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata("place")) {
            int place = player.getMetadata("place").get(0).asInt();
            player.sendTitle(new Title(ChatColor.YELLOW+"Ваше место: "+place));
            player.setGameMode(GameMode.SPECTATOR);
            event.setRespawnLocation((Location) player.getMetadata("respawn").get(0).value());
            player.removeMetadata("place", Survival.plugin);
            player.removeMetadata("respawn", Survival.plugin);
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        Player player = event.getPlayer();
        if (loc.getWorld() == Survival.lobby && !player.isOp()) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        Location loc = entity.getLocation();
        if (loc.getWorld()==Survival.lobby) {
            event.setCancelled(true);
        }
        if (entity.getType()==EntityType.PLAYER&&entity.hasMetadata("survival")) {
            Player player = (Player)entity;
            Game game = (Game) player.getMetadata("survival").get(0).value();
            if (game.onDamage(player, event.getCause())) {
                event.setCancelled(true);
            }
        }
    }
}
