package ch.taeko.TCBoatTweakerUltra.mixin;

import ch.taeko.TCBoatTweakerUltra.BlockStateHeight;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(BoatEntity.class)
public abstract class BoatMixin extends Entity {

    @Shadow
    @Nullable
    public abstract Entity getPrimaryPassenger();


    public BoatMixin(EntityType<?> type, World world) {
        super(type, world);
    }

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
}
