package ch.taeko.TCBoatTweakerUltra.mixin;

import ch.taeko.TCBoatTweakerUltra.Utilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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

    boolean colliding = false;
    Vec3d cachedSpeed = Vec3d.ZERO;
    byte ticks = 0;
    byte power = 0;

    public BoatMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void updateVelocity() {




    } // method is empty.

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void updatePaddles() {

        Vec3d currentV = this.getVelocity();
        double e = -0.25;

        // drag
        this.setVelocity(currentV.x * 0.999, currentV.y + e, currentV.z * 0.999);
        yawVelocity *= 0.85;

        boolean isStopped = (this.getVelocity().getX() < 0.01 && this.getVelocity().getX() > -0.01) && (this.getVelocity().getZ() < 0.01 && this.getVelocity().getZ() > -0.01);

        if (this.hasPassengers()) {

            // acceleration force
            float f = 0.0F;
            // yaw cache
            float x = this.getYaw();
            // get a linear version of the vector velocity
            double linV = Utilities.linV(this.getVelocity());
            // different steering acceleration values depending on speed
            if (this.pressingLeft && !isStopped) this.yawVelocity -= linV < 0.6 ? 0.6 : 1/linV * 0.5;
            // different steering acceleration values depending on speed
            if (this.pressingRight && !isStopped) this.yawVelocity += linV < 0.6 ? 0.6 : 1/linV * 0.5;
            // turn vehicle
            this.setYaw(this.getYaw() + this.yawVelocity);
            this.setVelocity(this.getVelocity().rotateY((float) Math.toRadians(x - this.getYaw())));
            // accelerate forward
            if (this.pressingForward) {
                f += 0.03F;
            }
            // brake
            if (this.pressingBack) {
                if (isStopped) {
                    f = 0F;
                    this.setVelocity(Vec3d.ZERO);
                } else {
                    this.setVelocity(currentV.x * 0.87F, currentV.y + e, currentV.z * 0.87F);
                }
            }
            // stop from spinning in place
            if (isStopped) this.yawVelocity = 0.0F;
            // upward acceleration
            double u = 0;
            // if boat can climb, begin climb
            if (this.horizontalCollision && !colliding) {
                this.colliding = true;
                u = 1D;
            }
            if (this.horizontalCollision && colliding) ticks += 1;
            // stop climb and restore previous velocity
            if ((!this.horizontalCollision && colliding) || ticks >= 10) {
                this.colliding = false;
                this.setVelocity(cachedSpeed);
            }
            // cache velocity for next tick
            if (!horizontalCollision) cachedSpeed = this.getVelocity();
            f *= Utilities.currentGearNumber;
            // update velocity
            this.setVelocity(this.getVelocity().add(
                    MathHelper.sin((float) Math.toRadians(-this.getYaw())) * f,
                    u,
                    MathHelper.cos((float) Math.toRadians(this.getYaw())) * f
            ));

            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY(), this.getZ(), -this.getVelocity().getX(), 0.05F, -this.getVelocity().getZ());
        }
    }
}