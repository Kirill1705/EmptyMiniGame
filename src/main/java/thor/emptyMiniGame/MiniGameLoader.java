package thor.emptyMiniGame;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

public class MiniGameLoader {
    private final MiniGameCreator factory;
    private final MiniGameConfig config;
    private final Map<Integer, MiniGame> games = new TreeMap<>();
    private final Plugin plugin;
    private World world;
    private static final int DISTANCE = 3000;
    private World createGameWorld(String name) {
        WorldCreator creator = new WorldCreator(name).type(WorldType.FLAT)
                .generatorSettings("{\"biome\":\"minecraft:the_void\",\"layers\":[{\"block\":\"minecraft:air\",\"height\":1}]}");
        return creator.createWorld();
    }
    private void setupWorld(World world) {
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setDifficulty(config.getDifficulty());
    }
    public MiniGameLoader(MiniGameCreator factory, Supplier<? extends MiniGameConfig> configCreator, Plugin plugin) {
        this.factory=factory;
        this.config = configCreator.get();
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new MiniGameListener(config, this), plugin);
        world = Bukkit.getWorld(config.name());
        if (world==null) {
            world = createGameWorld(config.name());
        }
        setupWorld(world);
    }
    private MiniGame findMiniGameToJoin() {
        int count = -1;
        MiniGame res = null;
        for (Map.Entry<Integer, MiniGame> entry: games.entrySet()) {
            MiniGame game = entry.getValue();
            if (game.finished()) {
                games.remove(entry.getKey());
                continue;
            }
            int current = game.playersCount();
            if (game.waitingForPlayers()&&current< config.maxPlayersCount()&&current>count) {
                res = game;
                count = game.playersCount();
            }
        }
        return res;
    }
    World getWorld() {
        return world;
    }
    private Location createLocation(int i) {
        return new Location(world, DISTANCE*i, 0, DISTANCE*i);
    }
    private int findLocation() {
        int prev = 0;
        boolean first = true;
        for (Map.Entry<Integer, MiniGame> entry: games.entrySet()) {
            int i = entry.getKey();
            if (i!=0&&first) {
                return i;
            }
            first = false;
            if (i-prev>=2) {
                return i-1;
            }
            prev = i;
        }
        return prev+1;
    }
    private String metaDataName() {
        return config.name()+"qwerty";
    }
    private void addMetaData(Player player, MiniGame game) {
        player.setMetadata(metaDataName(), new FixedMetadataValue(plugin, game));
        game.addPlayer(player);
    }
    void giveItem(Player player) {
        Inventory inv = player.getInventory();
        inv.addItem(config.startItem());
    }
    public void removePlayer(Player player) {
        player.removeMetadata(metaDataName(), plugin);
    }
    public MiniGame getPlayerMiniGame(Player player) {
        MiniGame game = null;
        if (player.hasMetadata(metaDataName())) {
            game = (MiniGame) player.getMetadata(metaDataName()).get(0).value();
        }
        return game;
    }
    void addPlayer(Player player) {
        MiniGame game = findMiniGameToJoin();
        if (game!=null) {
            addMetaData(player, game);
            return;
        }
        int i = findLocation();
        game = factory.create(createLocation(i));
        games.put(i, game);
        addMetaData(player, game);
    }
}
