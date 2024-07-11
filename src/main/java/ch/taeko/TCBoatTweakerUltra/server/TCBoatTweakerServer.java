package ch.taeko.TCBoatTweakerUltra.server;

import ch.taeko.TCBoatTweakerUltra.TCBoatTweaker;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;

public class TCBoatTweakerServer implements DedicatedServerModInitializer {

    public void onInitializeServer() {
        ServerPlayNetworking.registerGlobalReceiver(TCBoatTweaker.UPDATE_SNOW, (server, player, handler, buf, responseSender) -> {
            BlockPos target = buf.readBlockPos();
            server.execute(() -> player.getWorld().breakBlock(target, false));
        });
        ServerPlayNetworking.registerGlobalReceiver(TCBoatTweaker.RAINING, (server, player, handler, buf, responseSender) -> {
            long[] target = buf.readLongArray();
            // server.execute(() -> player.getWorld().addParticle(ParticleTypes.FALLING_WATER, (double) (target[0]) / 100, (double) (target[1]) / 100, (double) (target[2]) / 100, 15,0D,0D, 0D, 0D));
        });
        ServerPlayNetworking.registerGlobalReceiver(TCBoatTweaker.DRIFTING, (server, player, handler, buf, responseSender) -> {
            long[] target = buf.readLongArray();
            // server.execute(() -> player.getWorld().addParticle(ParticleTypes.POOF, (double) (target[0]) / 100, (double) (target[1]) / 100, (double) (target[2]) / 100, 15,0D,0D, 0D, 0D));
        });
    }
}
