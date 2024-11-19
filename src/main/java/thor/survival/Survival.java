package thor.survival;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;
import java.util.TreeSet;

public final class Survival extends JavaPlugin {
    public static BukkitScheduler scheduler;
    public static Plugin plugin;
    public static World lobby;
    public static SortedSet<Game> games = new TreeSet<>();

    @Override
    public void onEnable() {
        scheduler = Bukkit.getServer().getScheduler();
        plugin = this;
        lobby = Bukkit.getWorld("survivalLobby");
        getServer().getPluginManager().registerEvents(new MyListener(), this);
        Config.init();
        startTimer();
    }
    public static void removePlayer(Player player, Player killer) {
        if (player.hasMetadata("survival")) {
            Game game = (Game) player.getMetadata("survival").get(0).value();
            if (!game.removePlayer(player, killer)) {
                games.remove(game);
            }
        }
    }
    public static void addPlayer(Player player) {
        for (Game game: games) {
            if (game.addPlayer(player)) {
                return;
            }
        }
        int n = -1;
        for (Game game: games) {
            if (game.number>=n+2) {
                break;
            }
            n++;
        }
        n++;
        Game game = new Game(n, lobby);
        games.add(game);
        game.addPlayer(player);
    }
    public static void startTimer() {
        scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Game game: games) {
                    game.iteration();
                }
            }
        }, 0L, 20L);
    }
    public static void flushPlayer(Player player) {
        player.removeMetadata("survival", plugin);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String commandLabel, @NotNull String[] args) {
        if (cmd.getName().equalsIgnoreCase("survival")) {
            ConsoleCommandSender console = (ConsoleCommandSender)sender;
            if (args[0].equalsIgnoreCase("setsize")) {
                Game.SIZE = Integer.parseInt(args[1]);
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
