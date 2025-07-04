package thor.emptyMiniGame;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Интерфейс для реализации мини-игр в Minecraft.
 * <p>
 * Определяет базовый контракт, который должны соблюдать все мини-игры в системе.
 * Реализации этого интерфейса управляют жизненным циклом игроков внутри мини-игры.
 */
public interface MiniGame {

    /**
     * Добавляет игрока в мини-игру.
     * <p>
     * @param player Игрок для добавления в мини-игру (не может быть null)
     */
    void addPlayer(@NotNull Player player);

    /**
     * Проверяет, находится ли мини-игра в режиме ожидания игроков.
     * <p>
     * В этом состоянии мини-игра должна принимать новых игроков. Обычно это фаза лобби
     * перед началом основной игры.
     *
     * @return true если мини-игра ожидает новых игроков и еще не началась,
     *         false если игра уже активна или завершена
     */
    boolean waitingForPlayers();

    /**
     * Возвращает текущее количество игроков в мини-игре.
     * <p>
     * В подсчет должны включаться:
     * <ul>
     *   <li>Игроки в лобби</li>
     *   <li>Активные участники</li>
     * </ul>
     *
     * @return Текущее число участников (включая зрителей)
     */
    int playersCount();

    /**
     * Проверяет, завершена ли мини-игра.
     * <p>
     * После завершения мини-игра должна:
     * <ul>
     *   <li>Прекратить все игровые процессы</li>
     *   <li>Вывести финальные результаты</li>
     *   <li>Быть готовой к удалению</li>
     * </ul>
     *
     * @return true если мини-игра завершена,
     *         false если игра активна или в процессе завершения
     */
    boolean finished();

    /**
     * Удаляет игрока из мини-игры.
     * <p>
     * Реализация должна:
     * <ul>
     *   <li>Автоматически завершить игру, если игроков не осталось</li>
     *   <li>Не нужно очищать инвентарь игрока и менять другие его параметры, за вас это сделает библиотека</li>
     * </ul>
     *
     * @param player Игрок для удаления (не может быть null)
     */
    void removePlayer(@NotNull Player player);
}