package ch.taeko.TCBoatTweakerUltra.mixin;

import ch.taeko.TCBoatTweakerUltra.TCBoatTweaker;
import ch.taeko.TCBoatTweakerUltra.Utilities;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.PacketByteBuf;
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

@Mixin(BoatEntity.class)
public abstract class BoatMixin extends Entity {

    @Shadow
    @Nullable
    public abstract Entity getPrimaryPassenger();

    @Shadow
    private boolean pressingLeft;
    @Shadow
    private boolean pressingRight;
    @Shadow
    private boolean pressingForward;
    @Shadow
    private boolean pressingBack;
    @Shadow
    private float yawVelocity;

    @Shadow
    public abstract Direction getMovementDirection();

    @Shadow
    public float velocityDecay;
    @Shadow
    public BoatEntity.Location lastLocation;
    @Shadow
    public BoatEntity.Location location;
    @Shadow
    private double waterLevel;

    @Shadow
    public abstract float getWaterHeightBelow();

    @Shadow
    private float nearbySlipperiness;
    @Shadow
    private double fallVelocity;


    @Shadow
    public abstract void animateDamage();

    boolean colliding = false;
    Vec3d cachedSpeed = Vec3d.ZERO;
    float cachedY = 0F;
    float cachedYaw = 0F;

    float refArea = 0.5625F * 1.375F;
    float cd = 0.25F;
    // float muR = 13 * 10E-5F;
    float muBrake = 0.08F;
    float g = -9.81F;
    float u = 30F;
    float gearRatio = 3.45F;
    int ticks = 0;
    float accelerator = 0F;
    float maxTorque = 245F;

    public BoatMixin(EntityType<?> type, World world) {
	   super(type, world);
    }

    /**
	* @author
	* @reason
	*/
    @Overwrite
    public static boolean canCollide(Entity entity, Entity other) {
	   return false;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void setStepHeight(CallbackInfo ci) {
	   this.stepHeight = 1F;
    }

    /**
	* @author taeko_chan
	* @reason lol
	*/
    @Overwrite
    private void updateVelocity() {



	   BlockState nearbySurface = world.getBlockState(this.getBlockPos().down());
	   boolean drivingOnRoad =
			 nearbySurface.isOf(Blocks.CYAN_TERRACOTTA) ||
				    nearbySurface.isOf(Blocks.PACKED_ICE) ||
				    nearbySurface.isOf(Blocks.BLUE_ICE) ||
				    nearbySurface.isOf(Blocks.STONE);
	   drivingOnRoad = true; // for benamy

	   if (!Utilities.engineRunning) {
		  double d = -0.03999999910593033D;
		  double e = this.hasNoGravity() ? 0.0D : -0.4905;
		  double f = 0.0D;
		  this.velocityDecay = 0.05F;
		  if (this.lastLocation == BoatEntity.Location.IN_AIR && this.location != BoatEntity.Location.IN_AIR && this.location != BoatEntity.Location.ON_LAND) {
			 this.waterLevel = this.getBodyY(1.0D);
			 this.setPosition(this.getX(), (double) (this.getWaterHeightBelow() - this.getHeight()) + 0.101D, this.getZ());
			 this.setVelocity(this.getVelocity().multiply(1.0D, 0.0D, 1.0D));
			 this.fallVelocity = 0.0D;
			 this.location = BoatEntity.Location.IN_WATER;
		  } else {
			 if (this.location == BoatEntity.Location.IN_WATER) {
				f = (this.waterLevel - this.getY()) / (double) this.getHeight();
				this.velocityDecay = 0.9F;
			 } else if (this.location == BoatEntity.Location.UNDER_FLOWING_WATER) {
				e = -7.0E-4D;
				this.velocityDecay = 0.9F;
			 } else if (this.location == BoatEntity.Location.UNDER_WATER) {
				f = 0.009999999776482582D;
				this.velocityDecay = 0.45F;
			 } else if (this.location == BoatEntity.Location.IN_AIR) {
				this.velocityDecay = 1F;
			 } else if (this.location == BoatEntity.Location.ON_LAND) {
				if (drivingOnRoad) {
				    this.velocityDecay = 0.98F;
				} else {
				    this.velocityDecay = 0.88F;
				}
				if (this.getPrimaryPassenger() instanceof PlayerEntity) {
				    this.nearbySlipperiness /= 2.0F;
				}
			 }

			 Vec3d vec3d = this.getVelocity();
			 this.setVelocity(vec3d.x * (double) this.velocityDecay, vec3d.y + e, vec3d.z * (double) this.velocityDecay);
			 this.yawVelocity *= this.velocityDecay;
			 if (f > 0.0D) {
				Vec3d vec3d2 = this.getVelocity();
				this.setVelocity(vec3d2.x, (vec3d2.y + f * 0.06153846016296973D) * 0.75D, vec3d2.z);
			 }
		  }
	   }
    }

    /**
	* @author
	* @reason
	*/
    @Overwrite
    private void updatePaddles() {

	   if (Utilities.engineRunning) {

		  BlockState nearbySurface = world.getBlockState(this.getBlockPos().down());
		  boolean drivingOnRoad =
				nearbySurface.isOf(Blocks.CYAN_TERRACOTTA) ||
					   nearbySurface.isOf(Blocks.PACKED_ICE) ||
					   nearbySurface.isOf(Blocks.BLUE_ICE) ||
					   nearbySurface.isOf(Blocks.STONE);

		  Vec3d currentV = this.getVelocity();
		  double e = -0.4905;

		  // drag
		  double d;
		  if (drivingOnRoad) {
			 d = 0.998F;
		  } else {
			 d = 0.88F;
		  }
		  this.setVelocity(currentV.x * d, currentV.y + e, currentV.z * d);
		  yawVelocity *= 0.85;

		  boolean isStopped = (this.getVelocity().getX() < 0.01 && this.getVelocity().getX() > -0.01) && (this.getVelocity().getZ() < 0.01 && this.getVelocity().getZ() > -0.01);

		  if (this.hasPassengers()) {

			 // acceleration force
			 float f = 0.0F;

			 // yaw cache
			 float x = this.getYaw();

			 // get a linear version of the vector velocity
			 double linV = this.getVelocity().length();

			 // different steering acceleration values depending on speed
			 double steeringRate = linV < 1 ? 1 : 1 / linV * 0.8;
			 if (this.pressingLeft && !isStopped) this.yawVelocity -= steeringRate;

			 // different steering acceleration values depending on speed
			 if (this.pressingRight && !isStopped) this.yawVelocity += steeringRate;

			 // turn vehicle
			 this.setYaw(this.getYaw() + this.yawVelocity);
			 this.setVelocity(this.getVelocity().rotateY((float) Math.toRadians(x - this.getYaw())));

			 // accelerate forward
			 if (this.pressingForward) {
				f += 0.03F;
			 }

			 // brake
			 if (this.pressingBack) {
				Vec3d vec3d = this.getVelocity();
				double len = vec3d.length();
				Vec3d unit = vec3d.multiply(1 / len);
				double brakedLen = len - 0.05;
				this.setVelocity(unit.multiply(brakedLen));
			 }

			 if (isStopped) this.yawVelocity = 0.0F;

			 double u = 0;

			 /*if (this.horizontalCollision) {
				this.setVelocity(this.getVelocity().add(0F, 0.8F, 0F));
				if (!colliding) colliding = true;
			 } else {
				if (colliding) {
				    if (this.getY() >= cachedY + 0.5) {
					   this.setVelocity(cachedSpeed.multiply(0.75).rotateY((float) Math.toRadians(cachedYaw - this.getYaw())));
					   colliding = false;
				    }
				}
				cachedSpeed = this.getVelocity();
				cachedY = (float) this.getY();
				cachedYaw = this.getYaw();
			 }*/

			 f *= Utilities.currentGearNumber;
			 if (this.getVelocity().length() >= 2.98611111) f = 0;

			 this.setVelocity(this.getVelocity().add(
				    MathHelper.sin((float) Math.toRadians(-this.getYaw())) * f,
				    u,
				    MathHelper.cos((float) Math.toRadians(this.getYaw())) * f
			 ));
		  }
	   } else {

		  if (this.hasPassengers()) {
			 float f = 0.0F;
			 if (this.pressingLeft) {
				--this.yawVelocity;
			 }

			 if (this.pressingRight) {
				++this.yawVelocity;
			 }

			 if (this.pressingRight != this.pressingLeft && !this.pressingForward && !this.pressingBack) {
				f += 0.005F;
			 }

			 this.setYaw(this.getYaw() + this.yawVelocity);

			 if (this.pressingForward) {
				f += 0.06F;
			 }

			 if (this.pressingBack) {
				f -= 0.02F;
/*
				Vec3d vec3d = this.getVelocity();
				double len = vec3d.length();
				Vec3d unit = vec3d.multiply(1 / len);
				double brakedLen = len - 0.1;
				this.setVelocity(unit.multiply(brakedLen));
				this.yawVelocity *= 0.85;*/
			 }

			 if (this.getVelocity().length() >= 2.98611111) f = 0;
			 this.setVelocity(this.getVelocity().add((double) (MathHelper.sin(-this.getYaw() * 0.017453292F) * f), 0.0D, (double) (MathHelper.cos(this.getYaw() * 0.017453292F) * f)));

			 /*if (this.horizontalCollision) {
				this.setVelocity(this.getVelocity().add(0F, 0.8F, 0F));
				if (!colliding) colliding = true;
			 } else {
				if (colliding) {
				    if (this.getY() >= cachedY + 0.5) {
					   this.setVelocity(cachedSpeed.multiply(0.75).rotateY((float) Math.toRadians(cachedYaw - this.getYaw())));
					   colliding = false;
				    }
				}
				cachedSpeed = this.getVelocity();
				cachedY = (float) this.getY();
				cachedYaw = this.getYaw();
			 }*/
		  }
	   }

	   if (world.isRaining() && this.getVelocity() != Vec3d.ZERO && this.onGround) {
		  PacketByteBuf buf = PacketByteBufs.create();
		  buf.writeLongArray(
				new long[]{Math.round(this.getX() * 100),
					   Math.round(this.getY() * 100),
					   Math.round(this.getZ() * 100)}
		  );
		  ClientPlayNetworking.send(TCBoatTweaker.RAINING, buf);
	   }

	   if (world.getBlockState(this.getBlockPos()).isOf(Blocks.SNOW)) {
		  PacketByteBuf buf = PacketByteBufs.create();
		  buf.writeBlockPos(this.getBlockPos());
		  ClientPlayNetworking.send(TCBoatTweaker.UPDATE_SNOW, buf);
	   }

	   if (world.getBlockState(this.getBlockPos().north()).isOf(Blocks.SNOW)) {
		  PacketByteBuf buf = PacketByteBufs.create();
		  buf.writeBlockPos(this.getBlockPos().north());
		  ClientPlayNetworking.send(TCBoatTweaker.UPDATE_SNOW, buf);
	   }

	   if (world.getBlockState(this.getBlockPos().south()).isOf(Blocks.SNOW)) {
		  PacketByteBuf buf = PacketByteBufs.create();
		  buf.writeBlockPos(this.getBlockPos().south());
		  ClientPlayNetworking.send(TCBoatTweaker.UPDATE_SNOW, buf);
	   }

	   if (world.getBlockState(this.getBlockPos().east()).isOf(Blocks.SNOW)) {
		  PacketByteBuf buf = PacketByteBufs.create();
		  buf.writeBlockPos(this.getBlockPos().east());
		  ClientPlayNetworking.send(TCBoatTweaker.UPDATE_SNOW, buf);
	   }

	   if (world.getBlockState(this.getBlockPos().west()).isOf(Blocks.SNOW)) {
		  PacketByteBuf buf = PacketByteBufs.create();
		  buf.writeBlockPos(this.getBlockPos().west());
		  ClientPlayNetworking.send(TCBoatTweaker.UPDATE_SNOW, buf);
	   }
    }
}