package thor.emptyMiniGame;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public interface MiniGameConfig {
    default int maxPlayersCount() {
        return 10;
    }
    ItemStack startItem();
    default World getLobby() {
        return Bukkit.getWorld("world");
    }
    String name();
    default Difficulty getDifficulty() {
        return Difficulty.HARD;
    }
    default boolean autoRemoveByDeath() {
        return true;
    }
}
