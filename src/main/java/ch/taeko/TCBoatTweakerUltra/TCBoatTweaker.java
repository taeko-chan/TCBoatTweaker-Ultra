package ch.taeko.TCBoatTweakerUltra;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogLevel;
import net.minecraft.util.Identifier;

public class TCBoatTweaker implements ModInitializer {
    public static Identifier UPDATE_SNOW = new Identifier("tcboattweaker","update_snow");
    public static Identifier RAINING = new Identifier("tcboattweaker", "raining");
    public static Identifier DRIFTING = new Identifier("tcboattweaker", "drifting");
    @Override
    public void onInitialize() {
        Log.log(LogLevel.INFO, LogCategory.GENERAL, "Taeko's TCBoatTweaker is Loaded");
    }
}
