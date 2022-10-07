package ch.taeko.TCBoatTweakerUltra;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogLevel;

public class TCBoatTweaker implements ModInitializer {
    @Override
    public void onInitialize() {
        Log.log(LogLevel.INFO, LogCategory.GENERAL, "Taeko's TCBoatTweaker is Loaded");
    }
}
