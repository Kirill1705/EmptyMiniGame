package thor.survival;

import com.destroystokyo.paper.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.*;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static thor.survival.Survival.plugin;
import static thor.survival.Survival.scheduler;

public class Game implements Comparable<Game>{
    public static int INVULNAREBLE = 300;
    public static int DURATION = 2400;
    public static int SIZE = 24;
    public World world;
    public World lobby;
    public WorldBorder border;
    public int time = 0;
    public int k = 0;
    public int gameStep = 0;
    public int number;
    public ArrayList<Player> players = new ArrayList<>();
    public Scoreboard scoreboard;
    public Objective objective;
    public Game(int number, World lobby) {
        this.number=number;
        this.lobby=lobby;
        try {
            loadLobby();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective("game"+number, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("waiting for players");
    }
    public void loadLobby() throws IOException {
        StructureManager manager = plugin.getServer().getStructureManager();
        Structure structure = manager.loadStructure(new File("/home/container/world/generated/minecraft/structures/localLobby.nbt"));
        structure.place(new Location(lobby, number*50-12, 0, number*50-12),true, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());

    }
    public void iteration() {
        k++;
        if (gameStep==2) {
            time--;
            if (DURATION-time<=INVULNAREBLE) {
                int realTime = time-(DURATION-INVULNAREBLE);
                objective.setDisplayName("Invulnerable "+(realTime/60)+":"+(realTime%60));
                if (DURATION-time==INVULNAREBLE) {
                    for (Player player: players) {
                        player.sendTitle(new Title("Неуязвимость пропала!"));
                    }
                }
            }
            else if (time>=0) {
                updateZone();
            }
            for (Player player: players) {
                Location loc = player.getLocation();
                double x = Math.abs(loc.getX());
                double z = Math.abs(loc.getZ());
                if (2*x>=border.getSize()||2*z>=border.getSize()&&k%4==0) {
                    player.sendMessage(ChatColor.RED+"Граница мира "+border.getSize());
                    player.damage(1);
                }
            }
        }
        else if (gameStep==1) {
            objective.setDisplayName("start in "+time);
            if (time<5) {
                sendTitle(time + "", "");
            }
            time--;
            if (time==0) {
                startGame();
            }
        }
    }
    public void updateZone() {
        int realTime;
        String name = "Сужение зоны через ";
        if (time>400) {
            realTime = time-400;
            name = name+(realTime/60)+":"+(realTime%60);
        }
        else if (time>=0) {
            name = time/60+":"+time%60+" Граница: "+(border.getSize()/2);
            if (time==400) {
                sendTitle("Зона начинает уменьшаться!", "Скорость уменьшения 2 блока в секунду!");
            }
            if (time>10) {
                border.setSize(time * 4);
            }
        }
        else {
            name = "Kills";
        }
        setScoreboardName(name);
    }
    public void sendTitle(String title, String subtitle) {
        for (Player player: players) {
            player.sendTitle(new Title(title, subtitle));
        }
    }
    public void setScoreboardName(String name) {
        objective.setDisplayName(name);
    }
    public void startGame() {
        objective.setDisplayName("Invulnerable");
        time = DURATION;
        gameStep=2;
        world = Bukkit.createWorld(new WorldCreator("survival"+number));
        border = world.getWorldBorder();
        border.setSize(2000);
        for (Player player: players) {
            player.sendTitle(new Title("Игра началась!"));
            int y = world.getHighestBlockYAt(0, 0);
            player.teleport(new Location(world, 0, y, 0));
            player.setGameMode(GameMode.SURVIVAL);
            player.setFoodLevel(20);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.clearActivePotionEffects();
        }
        world.setTime(1000);
    }
    public boolean onDamage(Player player, EntityDamageEvent.DamageCause cause) {
        boolean result = false;
        if (gameStep==2) {
            if (DURATION-time<=INVULNAREBLE) {
                result=true;
            }
            if (cause== EntityDamageEvent.DamageCause.WORLD_BORDER&&!player.hasMetadata("noted")) {
                player.sendMessage(ChatColor.RED+"Граница мира: "+(border.getSize()/2)+" блоков!");
                player.setMetadata("noted", new FixedMetadataValue(Survival.plugin, true));
                result=false;
                Survival.scheduler.scheduleSyncDelayedTask(Survival.plugin, new Runnable() {
                    @Override
                    public void run() {
                        player.removeMetadata("noted", Survival.plugin);
                    }
                }, 100L);
            }
        }
        return result;
    }
    public boolean addPlayer(Player player) {
        if (gameStep==0&&players.size()<SIZE) {
            players.add(player);
            player.setScoreboard(scoreboard);
            player.setGameMode(GameMode.SURVIVAL);
            Score score = objective.getScore(player);
            score.setScore(0);
            player.setMetadata("survival", new FixedMetadataValue(plugin, this));
            if (players.size()==2) {
                gameStep=1;
            }
            else if (players.size()>2) {
                time=30;
            }
            return true;
        }
        return false;
    }
    public void gameEnd() {
        Player winner = players.get(0);
        winner.sendTitle(new Title(ChatColor.GREEN+"ВЫ ПОБЕДИЛИ!"));
        winner.setHealth(winner.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        winner.setMetadata("winner", new FixedMetadataValue(plugin, true));
        scoreboard.resetScores(winner);
        scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                winner.removeMetadata("winner", plugin);
                plugin.getServer().dispatchCommand(winner, "lobby");
                Bukkit.unloadWorld(world, false);
            }
        }, 100);
        Survival.flushPlayer(winner);
        players.clear();
    }
    public boolean removePlayer(Player player, Player killer) {
        try {
            if (killer != null) {
                Score score = objective.getScore(killer);
                score.setScore(score.getScore() + 1);
            }
            player.setMetadata("place", new FixedMetadataValue(Survival.plugin, players.size()));
            players.remove(player);
            scoreboard.resetScores(player);
            player.setMetadata("respawn", new FixedMetadataValue(Survival.plugin, player.getLocation()));
            if (players.size() == 1) {
                gameEnd();
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public int compareTo(@NotNull Game o) {
        return Integer.compare(number, o.number);
    }
}
