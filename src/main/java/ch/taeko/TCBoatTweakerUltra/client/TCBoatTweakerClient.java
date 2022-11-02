package ch.taeko.TCBoatTweakerUltra.client;

import ch.taeko.TCBoatTweakerUltra.Utilities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class TCBoatTweakerClient implements ClientModInitializer {

    public void onInitializeClient() {

        KeyBinding gearChange = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "tcboats.switchgear",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_E,
                "ch.taeko.tcboats"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (gearChange.wasPressed()) {
                Utilities.currentGearNumber *= -1;
                client.player.sendMessage(Text.literal("Gear " + Utilities.currentGearNumber + " selected"), false);
            }
        });
    }
}