package thor.survival;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    public static File file;
    public static FileConfiguration config;
    public static void init() {
        file = new File("/home/container/plugins/Survival/config.yml");
        try {
            config = YamlConfiguration.loadConfiguration(file);
            Game.INVULNAREBLE = config.getInt("invulnerable");
            Game.DURATION = config.getInt("duration");
            Game.SIZE = config.getInt("size");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
