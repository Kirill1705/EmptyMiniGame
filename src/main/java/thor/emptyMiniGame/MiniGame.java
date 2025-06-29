package thor.emptyMiniGame;

import org.bukkit.entity.Player;

public interface MiniGame {
    void addPlayer(Player player);
    boolean waitingForPlayers();
    int playersCount();
    boolean finished();
    void removePlayer(Player player);
}
