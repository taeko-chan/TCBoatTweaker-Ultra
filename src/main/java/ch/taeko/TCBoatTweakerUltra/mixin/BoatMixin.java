package ch.taeko.TCBoatTweakerUltra.mixin;

import ch.taeko.TCBoatTweakerUltra.BlockStateHeight;
import ch.taeko.TCBoatTweakerUltra.SpeedConverter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
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


    @Shadow public abstract Direction getMovementDirection();

    @Shadow private double y;

    @Shadow private double x;

    @Shadow private double z;

    public BoatMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void doVerticalAcceleration(CallbackInfo ci) {
        if (this.hasPassengers()) {

            double vms = SpeedConverter.convertVectorSpeed(this.getVelocity().x, this.getVelocity().z);
            double offset = Math.ceil(0.21 * vms);
            Entity driver = this.getPrimaryPassenger();
            double dX = driver.getX();
            double dY = Math.ceil(driver.getY());
            double dZ = driver.getZ();

            List<BlockStateHeight> targetBlocks = new ArrayList<>();

            BlockPos east = new BlockPos(dX + offset, dY, dZ);
            BlockPos ne = new BlockPos(dX + offset, dY, dZ - offset);
            BlockPos se = new BlockPos(dX + offset, dY, dZ + offset);

            BlockPos west = new BlockPos(dX - offset, dY, dZ);
            BlockPos nw = new BlockPos(dX - offset, dY, dZ - offset);
            BlockPos sw = new BlockPos(dX - offset, dY, dZ + offset);

            BlockPos south = new BlockPos(dX, dY, dZ + offset);
            BlockPos north = new BlockPos(dX, dY, dZ - offset);

            switch (this.getMovementDirection()) {
                case NORTH:
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(north), north));
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(nw), nw));
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(ne), ne));
                    break;
                case SOUTH:
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(south), south));
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(sw), sw));
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(se), se));
                    break;
                case EAST:
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(east), east));
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(ne), ne));
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(se), se));
                    break;
                case WEST:
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(west), west));
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(nw), nw));
                    targetBlocks.add(new BlockStateHeight(world.getBlockState(sw), sw));
                    break;
            }

            if (!targetBlocks.isEmpty()) for (BlockStateHeight b : targetBlocks) {
                if (b.blockState.getBlock().getSlipperiness() > 0.8 &&
                        world.getBlockState(new BlockPos(b.blockPos.getX(), b.blockPos.getY() + 1, b.blockPos.getZ())).isAir()) {

                    //SpeedConverter.determineClosest(targetBlocks, getBlockPos());

                    double difX = this.getX() - Math.floor(this.getX());
                    double difZ = this.getZ() - Math.floor(this.getZ());

                    Vec3d v = this.getVelocity();
                    this.setPos(b.blockPos.getX() + difX, this.getY()+1.2, b.blockPos.getZ() + difZ);
                    this.setVelocity(v);

                    break;
                }
            }
        }
    }
/*
    protected void onBlockCollision(BlockState state) {
        if (this.hasPassengers()) {

            Entity driver = this.getPrimaryPassenger();

            // set search distance depending on speed, 20.71 is a rough multiplier to get m/s
            // 0.21 = t to block that needs to be jumped over
            double vms = 20.71 * Math.sqrt(Math.pow(+this.getVelocity().x,2) + (Math.pow(+this.getVelocity().z,2)));
            double offset = Math.ceil(0.21 * vms);

            // this is terrible, will fix asap
            List<BlockStateHeight> targetBlocks = new ArrayList<>();

            targetBlocks.add(new BlockStateHeight(
                    world.getBlockState(new BlockPos(driver.getX() + offset, Math.ceil(driver.getY()), driver.getZ() + offset)),
                    new BlockPos(driver.getX() + offset, 1 + Math.ceil(driver.getY()), driver.getZ() + offset)
            ));
            targetBlocks.add(new BlockStateHeight(
                    world.getBlockState(new BlockPos(driver.getX() - offset, Math.ceil(driver.getY()), driver.getZ() - offset)),
                    new BlockPos(driver.getX() - offset, 1 + Math.ceil(driver.getY()), driver.getZ() - offset)
            ));
            targetBlocks.add(new BlockStateHeight(
                    world.getBlockState(new BlockPos(driver.getX() - offset, Math.ceil(driver.getY()), driver.getZ() + offset)),
                    new BlockPos(driver.getX() - offset, 1 + Math.ceil(driver.getY()), driver.getZ() + offset)
            ));
            targetBlocks.add(new BlockStateHeight(
                    world.getBlockState(new BlockPos(driver.getX() + offset, Math.ceil(driver.getY()), driver.getZ() - offset)),
                    new BlockPos(driver.getX() + offset, 1 + Math.ceil(driver.getY()), driver.getZ() - offset)
            ));

            for (BlockStateHeight b : targetBlocks) {
                if (b.blockState.getBlock().getSlipperiness() > 0.8 && world.getBlockState(b.blockPos).isAir()) {
                    // = around a bit more than 1 block
                    double d = 0.229F; // 0.229
                    Vec3d vec3d = this.getVelocity();
                    this.setVelocity(vec3d.x, d, vec3d.z);
                }
            }
        }
    }
    */
}
