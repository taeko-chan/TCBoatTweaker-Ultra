package ch.taeko.TCBoatTweakerUltra.mixin;

import ch.taeko.TCBoatTweakerUltra.client.TCBoatTweakerClient;
import ch.taeko.TCBoatTweakerUltra.hud.HudData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow
    private @Final MinecraftClient client;

    @Inject(
		  method = "onEntityPassengersSet(Lnet/minecraft/network/packet/s2c/play/EntityPassengersSetS2CPacket;)V",
		  at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/gui/hud/InGameHud;setOverlayMessage(Lnet/minecraft/text/Text;Z)V",
				shift = At.Shift.AFTER
		  )
    )
    private void checkBoatEntry(EntityPassengersSetS2CPacket packet, CallbackInfo info) {
	   if(!(TCBoatTweakerClient.client.world.getEntityById(packet.getId()) instanceof BoatEntity)) return;
	   TCBoatTweakerClient.ridingBoat = true;
	   TCBoatTweakerClient.hudData = new HudData();
    }
}