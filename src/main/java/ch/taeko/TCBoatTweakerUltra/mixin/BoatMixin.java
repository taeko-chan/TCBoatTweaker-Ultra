package ch.taeko.TCBoatTweakerUltra.mixin;

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

    @Shadow
    private float velocityDecay;

    @Shadow
    private BoatEntity.Location lastLocation;

    @Shadow
    private double waterLevel;

    @Shadow
    private BoatEntity.Location location;

    @Shadow
    public abstract float getWaterHeightBelow();

    @Shadow
    private double fallVelocity;

    @Shadow
    private float nearbySlipperiness;

    @Shadow private boolean pressingLeft;

    @Shadow private boolean pressingRight;

    @Shadow private boolean pressingForward;

    @Shadow private boolean pressingBack;

    @Shadow private float yawVelocity;

    @Shadow public abstract void setPaddleMovings(boolean leftMoving, boolean rightMoving);

    @Shadow public abstract Direction getMovementDirection();

    public BoatMixin(EntityType<?> type, World world) {
	   super(type, world);
    }

    private List<Vec3d> speedCache = new ArrayList<>();

    /**
	* @author
	* @reason
	*/
    @Overwrite
    private void updateVelocity() {

	   double e = -0.25;
	   double f = 0.0;
	   this.velocityDecay = 0.05F;

	   if (this.lastLocation == BoatEntity.Location.IN_AIR && this.location != BoatEntity.Location.IN_AIR && this.location != BoatEntity.Location.ON_LAND) {

		  this.waterLevel = this.getBodyY(1.0);
		  this.setPosition(this.getX(), (double) (this.getWaterHeightBelow() - this.getHeight()) + 0.101, this.getZ());
		  this.setVelocity(this.getVelocity().multiply(1.0, 0.0, 1.0));
		  this.fallVelocity = 0.0;
		  this.location = BoatEntity.Location.IN_WATER;

	   } else {

		  if (this.location == BoatEntity.Location.IN_WATER) {

			 f = (this.waterLevel - this.getY()) / (double) this.getHeight();
			 this.velocityDecay = 0.9F;

		  } else if (this.location == BoatEntity.Location.UNDER_FLOWING_WATER) {

			 e = -7.0E-4;
			 this.velocityDecay = 0.9F;

		  } else if (this.location == BoatEntity.Location.UNDER_WATER) {

			 f = 0.009999999776482582;
			 this.velocityDecay = 0.45F;

		  } else if (this.location == BoatEntity.Location.IN_AIR) {

			 this.velocityDecay = 0.9F;

		  } else if (this.location == BoatEntity.Location.ON_LAND) {

                if (this.nearbySlipperiness >= 0.97) {
                    this.velocityDecay = this.nearbySlipperiness * 1.001F;
                } else this.velocityDecay = 0.9F;

                if (this.horizontalCollision) {
                    f = 20;
                }

		  }
	   }

	   if (f > 0.0) {
		  Vec3d vec3d2 = speedCache.get(speedCache.size() - 1);
		  this.setVelocity(vec3d2.x, (vec3d2.y + f * 0.06153846016296973) * 0.75, vec3d2.z);
	   }

	   Vec3d currentV = this.getVelocity();
	   this.setVelocity(currentV.x * (double) this.velocityDecay, currentV.y + e, currentV.z * (double) this.velocityDecay);

    }

    /**
	* @author
	* @reason
	*/
    @Overwrite
    private void updatePaddles() {

	   boolean isStopped = (this.getVelocity().getX() < 0.01 && this.getVelocity().getX() > -0.01) && (this.getVelocity().getZ() < 0.01 && this.getVelocity().getZ() > -0.01);

	   if (this.hasPassengers()) {
		  float f = 0.0F;

		  if (this.pressingLeft && !isStopped) {
			 --this.yawVelocity;
		  }

		  if (this.pressingRight && !isStopped) {
			 ++this.yawVelocity;
		  }

		  if (this.pressingRight != this.pressingLeft && !this.pressingForward && !this.pressingBack) {
			 // f += 0.005F;
		  }

		  this.setYaw(this.getYaw() + this.yawVelocity);
		  if (this.pressingForward) {
			 f += 0.04F;
		  }

		  if (this.pressingBack) {
			 if (isStopped || getMovementDirection() != getPrimaryPassenger().getHorizontalFacing()) {
				f = 0F;
				this.setVelocity(Vec3d.ZERO);
			 } else {
				f -= 0.05F;
			 }
		  }

		  if (isStopped) this.yawVelocity = 0;
		  //* 0.017453292

		  this.setVelocity(this.getVelocity().add(
				MathHelper.sin(-this.getYaw() * 0.017453292F) * f,
				0.0,
				MathHelper.cos(this.getYaw() * 0.017453292F) * f
		  ));
		  // this.setPaddleMovings(this.pressingRight && !this.pressingLeft || this.pressingForward, this.pressingLeft && !this.pressingRight || this.pressingForward);
	   }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void cacheSpeedWhenDriving(CallbackInfo ci) {
	   if (this.hasPrimaryPassenger()) {
		  if (speedCache.size() < 10) speedCache.add(this.getVelocity());
		  else {
			 speedCache.remove(speedCache.size() - 1);
			 speedCache.add(this.getVelocity());
		  }
	   }
    }
}
