package ch.taeko.TCBoatTweakerUltra.mixin;

import ch.taeko.TCBoatTweakerUltra.Utilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(BoatEntity.class)
public abstract class BoatMixin extends Entity {

    public BoatMixin(EntityType<?> type, World world) { super(type, world); }

    private List<Vec3d> speedCache = new ArrayList<>();

    @Inject(at = @At("HEAD"), method = "tick")
    public void onHorizontalCollision(CallbackInfo ci) {

        if (speedCache.size() <= 10) speedCache.add(this.getVelocity()); else {
            speedCache.clear();
            speedCache.add(this.getVelocity());
        }

        if (this.horizontalCollision
                && !(world.getBlockState(this.getBlockPos().down(2)).isAir())
                && Utilities.surroundingBlocksAreSlippery(getBlockPos(), world)) {

            this.setPos(this.getX(), this.getY() + 0.4, this.getZ());
            this.setVelocity(speedCache.isEmpty() ? this.getVelocity() : speedCache.get(0));

        }

    }
}
