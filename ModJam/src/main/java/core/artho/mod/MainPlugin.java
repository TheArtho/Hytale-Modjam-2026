package core.artho.mod;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import core.artho.mod.config.ModConfig;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MainPlugin extends JavaPlugin {

    private static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private Config<ModConfig> config;

    public MainPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
        this.config = this.withConfig("ModConfig", ModConfig.CODEC);
    }

    @Override
    protected void setup() {
        // Setting up the config file
        this.config.save();

        LOGGER.atInfo().log("Mod started!");
    }
}
