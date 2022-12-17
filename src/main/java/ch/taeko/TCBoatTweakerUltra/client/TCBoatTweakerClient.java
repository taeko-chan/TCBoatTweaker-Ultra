package ch.taeko.TCBoatTweakerUltra.client;

import ch.taeko.TCBoatTweakerUltra.Utilities;
import ch.taeko.TCBoatTweakerUltra.hud.HudData;
import ch.taeko.TCBoatTweakerUltra.hud.HudRenderer;
import ch.taeko.TCBoatTweakerUltra.mixin.BoatMixin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class TCBoatTweakerClient implements ClientModInitializer {

    public static MinecraftClient client = null;
    public static boolean ridingBoat = false;
    public static HudData hudData;
    public static HudRenderer hudRenderer;

    public void onInitializeClient() {

        client = MinecraftClient.getInstance();
        hudRenderer = new HudRenderer(client);

        ClientTickEvents.END_WORLD_TICK.register(clientWorld -> {
            if(client.player == null) return;
            if(client.player.getVehicle() instanceof BoatEntity) {
                hudData.update();
            }
            else if (ridingBoat) {
                ridingBoat = false;
            }
        });

        KeyBinding engineToggle = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "tcboats.switchgear",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_E,
                "ch.taeko.tcboats"));

        KeyBinding cruiseControl = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "tcboats.cruisecontrol",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "ch.taeko.tcboats"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (engineToggle.wasPressed()) {
                Utilities.engineRunning ^= true;
                client.player.sendMessage(Text.literal("Engine Toggled"), false);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (cruiseControl.wasPressed()) {
                Utilities.cruiseControl ^= true;
                client.player.sendMessage(Text.literal("Cruise Control Toggled"), false);
            }
        });
    }
}