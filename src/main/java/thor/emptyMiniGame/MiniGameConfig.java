package thor.emptyMiniGame;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 * Конфигурация мини игры
 */
public final class MiniGameConfig {
    private final String name;
    private final ItemStack startItem;
    private final int maxPlayers;
    private final boolean removeByDeath;
    private final World lobby;
    private final Difficulty difficulty;
    private MiniGameConfig(Builder builder) {
        this.name = builder.name;
        this.maxPlayers = builder.maxPlayers;
        this.startItem = builder.startItem;
        this.lobby = builder.lobby;
        this.removeByDeath = builder.autoRemove;
        this.difficulty=builder.difficulty;
    }

    public String getName() { return name; }
    public int getMaxPlayers() { return maxPlayers; }
    public boolean autoRemoveByDeath() { return removeByDeath; }
    public World getLobby() { return lobby; }
    public ItemStack getStartItem() { return startItem; }
    public Difficulty getDifficulty() {return difficulty;}
    public interface GameNameStage {
        ItemStackStage name(String name);
    }

    public interface ItemStackStage {
        BuildStage startItem(ItemStack startItem);
    }

    public interface BuildStage {
        BuildStage maxPlayers(int players);
        BuildStage autoRemovePlayerByDeath(boolean remove);
        BuildStage lobby(World lobby);
        BuildStage difficulty(Difficulty difficulty);
        MiniGameConfig build();
    }

    private static class Builder implements GameNameStage, ItemStackStage, BuildStage {
        private String name;
        private ItemStack startItem;
        private int maxPlayers = 10;
        private boolean autoRemove = true;
        private World lobby = Bukkit.getWorld("world");
        private Difficulty difficulty = Difficulty.HARD;

        /**
         * Установите имя мини игры
         * @param name имя мини игры
         * @return Объект для дальнейшей настройки конфигурации.
         */
        @Override
        public ItemStackStage name(String name) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Game name cannot be empty");
            }
            this.name = name;
            return this;
        }

        /**
         * Установите предмет, нажав на который игрок попадёт в вашу мини игру
         * @param startItem Предмет
         * @return Объект для дальнейшей настройки конфигурации.
         */
        @Override
        public BuildStage startItem(ItemStack startItem) {
            if (startItem==null) {
                throw new IllegalArgumentException("Start item can not be null!");
            }
            this.startItem = startItem;
            return this;
        }

        /**
         * Установите максимальное количество игроков в мини игре (по умолчанию 10)
         * @param maxPlayers макс количество игроков > 0
         * @return Объект для дальнейшей настройки конфигурации.
         */
        @Override
        public BuildStage maxPlayers(int maxPlayers) {
            if (maxPlayers <= 0) {
                throw new IllegalArgumentException("Max Players should be > 0");
            }
            this.maxPlayers = maxPlayers;
            return this;
        }

        /**
         * Установите будет ли загрузчик удалять игрока автоматически из мини игры при его смерти
         * @param remove tru если будет, false если нет (по умолчанию true)
         * @return Объект для дальнейшей настройки конфигурации.
         */
        @Override
        public BuildStage autoRemovePlayerByDeath(boolean remove) {
            this.autoRemove = remove;
            return this;
        }

        /**
         * Установите общее лобби для всех мини игр сервера (по умолчанию мир с именем "world" (не устанавливайте для нашего сервера!)
         * @param lobby Мир лобби
         * @return Объект для дальнейшей настройки конфигурации.
         */
        @Override
        public BuildStage lobby(World lobby) {
            if (lobby==null) {
                throw new IllegalArgumentException("Lobby can not be null!");
            }
            this.lobby=lobby;
            return this;
        }

        /**
         * Установите сложность (по умолчанию хард)
         * @param difficulty Сложность мира мини игры
         * @return Объект для дальнейшей настройки конфигурации.
         */
        @Override
        public BuildStage difficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        /**
         * Вернёт объект конфигурации который необходимо передать в конструктор MiniGameLoader
         * @return Объект конфигурации вашей мини игры
         */
        @Override
        public MiniGameConfig build() {
            if (name == null || startItem == null) {
                throw new IllegalStateException("Required parameters not set");
            }
            return new MiniGameConfig(this);
        }
    }

    /**
     * Начните заполнение конфигурации вашей мини игры
     * @return Объект для настройки конфигурации.
     */
    public static GameNameStage builder() {
        return new Builder();
    }
}