package ch.taeko.TCBoatTweakerUltra;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogLevel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class TCBoatTweaker implements ModInitializer {
    @Override
    public void onInitialize() {
        Log.log(LogLevel.INFO, LogCategory.GENERAL, "Taeko's TCBoatTweaker is Loaded");
    }
}
