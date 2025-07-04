package thor.emptyMiniGame;

import org.bukkit.Location;

/**
 * Интерфейс который должен уметь создавать мини игру.
 */
public interface MiniGameCreator {
    /**
     * Единственный метод возвращает созданный объект - мини игру.
     * @param loc Место в котором игровая карта должна быть размещена
     *            Желательно чтобы все игровые процессы были в пределах 1000 блоков от этого места!
     * @return Созданный объект вашей мини игры (пока без игроков)
     */
    MiniGame create(Location loc);
}
