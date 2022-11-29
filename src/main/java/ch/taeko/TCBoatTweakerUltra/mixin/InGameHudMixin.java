package ch.taeko.TCBoatTweakerUltra.mixin;

import ch.taeko.TCBoatTweakerUltra.client.TCBoatTweakerClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(
		  method = "render",
		  at = @At("TAIL")
    )
    public void render(MatrixStack stack, float tickDelta, CallbackInfo info) {
	   if(TCBoatTweakerClient.ridingBoat && !(TCBoatTweakerClient.client.currentScreen instanceof ChatScreen)) {
		  TCBoatTweakerClient.hudRenderer.render(stack, tickDelta);
	   }
    }
}
