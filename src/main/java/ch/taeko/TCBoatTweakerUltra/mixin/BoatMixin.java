package ch.taeko.TCBoatTweakerUltra.mixin;

import ch.taeko.TCBoatTweakerUltra.Engine.BoatMechanics;
import ch.taeko.TCBoatTweakerUltra.Utilities;
import ch.taeko.TCBoatTweakerUltra.client.TCBoatTweakerClient;
import ch.taeko.TCBoatTweakerUltra.hud.HudData;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
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
    float g = 9.81F;
    float gearRatio = 3.45F;
    int ticks = 0;
    double power = 0;

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void updatePaddles() {

        float torque = 0;
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
                power += 0.05;
                if (power > 1) power = 1;
            } else {
                power -= 0.1;
                if (power < 0) power = 0;
            }
            torque = (float) (power * 245);

            // brake
            if (this.pressingBack) {
                braking = true;
            }


        }

        // speed unit meters per tick ~ 1 m/tk = 20 m/s
        // use m/s for everything and convert at the end bcs otherwise it's annoying

        Vec3d currentV = Utilities.toSiV(this.getVelocity());
        // VVV  | now in si units |  VVV

        // mass calculation
        float mass = 1370 + (this.getPassengerList().size() * 60);

        // air resistance in tick
        float airDrag = cd * (1.24F * (float) Math.pow(Utilities.linV(currentV),2) * refArea);
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

        float Ares = engineAccel - dragAccel;
        if (Ares < 0 && brakingIsStopped) {
            Ares = 0;
            this.setVelocity(Vec3d.ZERO);
        }

        // upward acceleration
        double u = 0;
        // if boat can climb, begin climb
        if (this.horizontalCollision && !colliding) {
            this.colliding = true;
            g = 0;
            u = 2D;
        }
        // stop climb and restore previous velocity
        if (!this.horizontalCollision && colliding) {
            this.colliding = false;
            g = 9.81F;
            this.setVelocity(cachedSpeed);
        }
        // cache velocity for next tick
        if (!horizontalCollision) cachedSpeed = this.getVelocity();

        // VVV | back to MC | VVV

        float deltaA = (float) Utilities.toMc(Ares);

        if (this.hasPassengers()) {
            this.setVelocity(this.getVelocity().add(
                    MathHelper.sin((float) Math.toRadians(-this.getYaw())) * deltaA,
                    -Utilities.toMc(g) + u,
                    MathHelper.cos((float) Math.toRadians(this.getYaw())) * deltaA
            ));
        }

        if (Utilities.linV(cachedExhaustSpeed) != 0 && Utilities.engineRunning)
            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.getX(), this.getY(), this.getZ(), -cachedExhaustSpeed.getX() * 1.3, 0.05F, -cachedExhaustSpeed.getZ() * 1.3);

        if (TCBoatTweakerClient.hudData != null) {
            TCBoatTweakerClient.hudData.setTorque(torque);
            TCBoatTweakerClient.hudData.setRpm(0);
        }



/*

        if (this.hasPassengers()) {

            // acceleration force
            float f = 0.0F;

            // yaw cache
            float x = this.getYaw();

            // get a linear version of the vector velocity
            double linV = Utilities.linV(this.getVelocity());

            // different steering acceleration values depending on speed
            if (this.pressingLeft && !isStopped) this.yawVelocity -= linV < 0.75 ? 0.75 : 1/linV * 0.8;

            // different steering acceleration values depending on speed
            if (this.pressingRight && !isStopped) this.yawVelocity += linV < 0.75 ? 0.75 : 1/linV * 0.8;

            // turn vehicle
            this.setYaw(this.getYaw() + this.yawVelocity);
            this.setVelocity(this.getVelocity().rotateY((float) Math.toRadians(x - this.getYaw())));

            // accelerate forward
            if (this.pressingForward) {
                if (this.getNearbySlipperiness() > 0.9) f += 0.023F;
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

            // stop from spinning in place
            if (isStopped) this.yawVelocity = 0.0F;

            // upward acceleration
            double u = 0;

            // if boat can climb, begin climb
            if (this.horizontalCollision && !colliding) {
                this.colliding = true;
                u = 1D;
            }

            // stop climb and restore previous velocity
            if (!this.horizontalCollision && colliding) {
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
        }*/
    }
}