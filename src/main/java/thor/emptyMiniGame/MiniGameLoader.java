package thor.emptyMiniGame;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Это основной класс библиотеки и есть загрузчик ваших мини игр. Обязательно создайте и сохраните 1 объект данного класса
 */
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

    /**
     * В вашем плагине обязательно создать 1 экземпляр данного класса. Именно он будет вызывать методы в вашей мини игре. Именно этот загрузчик управляет всеми вашими минииграми на сервере
     * @param factory Объект класса реализующего интерфейс фабрики мини игры. То есть он будет создавать объекты ваших мини игр
     * @param config Объект конфигурации мини игры
     * @param plugin Ваш плагин (ОсновнойКласс extends JavaPlugin
     */
    public MiniGameLoader(MiniGameCreator factory, MiniGameConfig config, Plugin plugin) {
        this.factory=factory;
        this.config = config;
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new MiniGameListener(config, this), plugin);
        world = Bukkit.getWorld(config.getName());
        if (world==null) {
            world = createGameWorld(config.getName());
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
            if (game.waitingForPlayers()&&current< config.getMaxPlayers()&&current>count) {
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
        return config.getName()+"qwerty";
    }
    private void addMetaData(Player player, MiniGame game) {
        player.setMetadata(metaDataName(), new FixedMetadataValue(plugin, game));
        game.addPlayer(player);
    }
    void giveItem(Player player) {
        Inventory inv = player.getInventory();
        inv.addItem(config.getStartItem());
    }

    /**
     * Удаляет метаданные с игрока который покинул мини игру.
     * Если в конфигурации вы используете autoRemovePlayerByDeath, то его этот метод можете не реализовывать
     * @param player Игрок с которого надо удалить метаданные
     */
    public void removePlayer(Player player) {
        player.removeMetadata(metaDataName(), plugin);
    }

    /**
     * Вернёт объект мини игры в которой участвует игрок
     * @param player Игрок участвующий в мини игре
     * @return Объект мини игры в которой находится игрок сейчас,
     *         null если игрок сейчас не играет ни в одну вашу мини игру
     */
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
