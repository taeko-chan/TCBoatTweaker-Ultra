package ch.taeko.TCBoatTweakerUltra.mixin;

import ch.taeko.TCBoatTweakerUltra.Utilities;
import ch.taeko.TCBoatTweakerUltra.client.TCBoatTweakerClient;
import ch.taeko.TCBoatTweakerUltra.hud.HudData;
import net.minecraft.block.Blocks;
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

import static ch.taeko.TCBoatTweakerUltra.Utilities.cruiseControlSpeed;

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
    boolean colliding = false;
    Vec3d cachedSpeed = Vec3d.ZERO;
    Vec3d cachedExhaustSpeed = Vec3d.ZERO;

    public BoatMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * @author taeko_chan
     * @reason lol
     */
    @Overwrite
    private void updateVelocity() {} // method is empty.
    float refArea = 0.5625F * 1.375F;
    float cd = 0.25F;
    // float muR = 13 * 10E-5F;
    float muBrake = 0.08F;
    float g = -9.81F;
    float u = 10F;
    float gearRatio = 3.45F;
    int ticks = 0;
    float accelerator = 0F;
    float maxTorque = 245F;

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void updatePaddles() {

        float torque;
        float dragRes = 0F;

        // get a linear version of the vector velocity
        double linV = Utilities.linV(this.getVelocity());
        double linVMS = Utilities.toSi(linV);

        if (linV != 0) cachedExhaustSpeed = this.getVelocity();

        this.yawVelocity *= 0.85;
        boolean brakingIsStopped = (linVMS < 1);
        boolean isStopped = (this.getVelocity().getX() < 0.01 && this.getVelocity().getX() > -0.01) && (this.getVelocity().getZ() < 0.01 && this.getVelocity().getZ() > -0.01);
        boolean braking = false;

        if (this.hasPassengers()) {

            float x = this.getYaw();

            // different steering acceleration values depending on speed
            if (this.pressingLeft && !isStopped) this.yawVelocity -= linV < 0.6 ? 0.6 : 1/linV * 0.5;

            // different steering acceleration values depending on speed
            if (this.pressingRight && !isStopped) this.yawVelocity += linV < 0.6 ? 0.6 : 1/linV * 0.5;

            // rotate vehicle
            this.setYaw(this.getYaw() + this.yawVelocity);
            this.setVelocity(this.getVelocity().rotateY((float) Math.toRadians(x - this.getYaw())));

            double missalignment = HudData.driftAngle;
            if (missalignment > 1) {
                this.setVelocity(this.getVelocity().rotateY(
                        -((float) Math.toRadians(missalignment))
                ));
            }

            if (this.pressingForward && Utilities.engineRunning) {
                if (Utilities.cruiseControl) Utilities.cruiseControl = false;
                accelerator += 0.05;
                if (accelerator > 1) accelerator = 1;
            } else if (!Utilities.cruiseControl) {
                accelerator -= 0.1;
                if (accelerator < 0) accelerator = 0;
            }

            // brake
            if (this.pressingBack) {
                if (Utilities.cruiseControl) Utilities.cruiseControl = false;
                braking = true;
            }


        }

        // speed unit meters per tick ~ 1 m/tk = 20 m/s
        // use m/s for everything and convert at the end bcs otherwise it's annoying

        Vec3d currentV = Utilities.toSiV(this.getVelocity());
        // VVV  | now in si units |  VVV

        float airDrag = cd * (1.24F * (float) Math.pow(Utilities.linV(currentV),2) * refArea);

        if (Utilities.cruiseControl) {
            if (cruiseControlSpeed == null) {
                cruiseControlSpeed = linV;
            }
            accelerator = airDrag / (gearRatio * maxTorque);
        }

        torque = accelerator * maxTorque;

        // mass calculation
        float mass = 1370 + (this.getPassengerList().size() * 60);

        // air resistance in tick
        dragRes += airDrag;

        // rolling resistance in tick, simplfied for now
        float rollingRes; // µR * mass * g * (float) Utilities.linV(currentV);
        rollingRes = 0;
        dragRes += rollingRes;

        if (braking) dragRes += muBrake * mass * g;

        // resulting counteractive forces
        float dragAccel = dragRes/mass;

        /*
        * For now, I am omitting engine resistance.
        */

        // engine force & acceleration
        float engineAccel = (torque * gearRatio)/mass; // nm * 1/m -> F -> F/m = a
        System.out.println(engineAccel + ", " + dragAccel);

        float Ares = engineAccel - dragAccel;
        if (Ares < 0 && brakingIsStopped) {
            Ares = 0;
            this.setVelocity(Vec3d.ZERO);
        }

        // if boat can climb, begin climb
        if (this.horizontalCollision && !colliding) {
            this.colliding = true;
            g = 0;
        }
        // stop climb and restore previous velocity
        if (!this.horizontalCollision && colliding) {
            this.colliding = false;
            g = -9.81F;
            this.setVelocity(cachedSpeed);
        }
        // cache velocity for next tick
        if (!horizontalCollision) cachedSpeed = this.getVelocity();

        // VVV | back to MC | VVV

        float deltaA = (float) Utilities.toMc(Ares);

        if (this.hasPassengers()) {
            this.setVelocity(this.getVelocity().add(
                    MathHelper.sin((float) Math.toRadians(-this.getYaw())) * deltaA,
                    Utilities.toMc(g) + (this.colliding ? Utilities.toMc(u) : 0),
                    MathHelper.cos((float) Math.toRadians(this.getYaw())) * deltaA
            ));
        }

        if (Utilities.linV(cachedExhaustSpeed) != 0 && Utilities.engineRunning)
            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY(), this.getZ(), -cachedExhaustSpeed.getX() * 1.3, 0.05F, -cachedExhaustSpeed.getZ() * 1.3);

        if (world.getBlockState(this.getBlockPos()).isOf(Blocks.SNOW)) world.removeBlock(this.getBlockPos(), false);

        if (TCBoatTweakerClient.hudData != null) {
            TCBoatTweakerClient.hudData.setTorque(torque);
            TCBoatTweakerClient.hudData.setRpm(0);
        }

    }
}