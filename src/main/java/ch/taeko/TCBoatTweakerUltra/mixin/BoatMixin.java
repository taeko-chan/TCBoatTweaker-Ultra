package ch.taeko.TCBoatTweakerUltra.mixin;

import ch.taeko.TCBoatTweakerUltra.Utilities;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogLevel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(BoatEntity.class)
public abstract class BoatMixin extends Entity {

    @Shadow
    @Nullable
    public abstract Entity getPrimaryPassenger();

    @Shadow private boolean pressingLeft;

    @Shadow private boolean pressingRight;

    @Shadow private boolean pressingForward;

    @Shadow private boolean pressingBack;

    @Shadow private float yawVelocity;

    @Shadow public abstract Direction getMovementDirection();


    @Shadow public abstract float getNearbySlipperiness();

    public BoatMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void updateVelocity() { // method is empty.
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void updatePaddles() {

        Vec3d currentV = this.getVelocity();
        double e = -0.25;

        // drag
        this.setVelocity(currentV.x * 0.97, currentV.y + e, currentV.z * 0.97);
        yawVelocity *= 0.85;

        boolean isStopped = (this.getVelocity().getX() < 0.01 && this.getVelocity().getX() > -0.01) && (this.getVelocity().getZ() < 0.01 && this.getVelocity().getZ() > -0.01);

        if (this.hasPassengers()) {

            // acceleration force
            float f = 0.0F;

            // yaw cache
            float x = this.getYaw();

            // get a linear version of the vector velocity
            double linV = Utilities.vectorTo1D(this.getVelocity());

            // different steering acceleration values depending on speed
            if (this.pressingLeft && !isStopped) this.yawVelocity -= linV < 1 ? 1 : 1/linV * 0.8;

            // different steering acceleration values depending on speed
            if (this.pressingRight && !isStopped) this.yawVelocity += linV < 1 ? 1 : 1/linV * 0.8;

            // turn vehicle
            this.setYaw(this.getYaw() + this.yawVelocity);
            this.setVelocity(this.getVelocity().rotateY((float) Math.toRadians(x - this.getYaw())));

            // accelerate forward
            if (this.pressingForward) {
                if (this.getNearbySlipperiness() > 0.9) f += 0.045F;
                else f += 0.02F;
            }

            // brake
            if (this.pressingBack) {
                if (isStopped) {
                    f = 0F;
                    this.setVelocity(Vec3d.ZERO);
                } else {
                    this.setVelocity(currentV.x * 0.87, currentV.y + e, currentV.z * 0.87);
                }
            }

            if (isStopped) this.yawVelocity = 0.0F;

            double u = 0;

            if (this.horizontalCollision) u = 0.5D;

            f *= Utilities.currentGearNumber;

            this.setVelocity(this.getVelocity().add(
                    MathHelper.sin((float) Math.toRadians(-this.getYaw())) * f,
                    u,
                    MathHelper.cos((float) Math.toRadians(this.getYaw())) * f
            ));
        }
    }
}