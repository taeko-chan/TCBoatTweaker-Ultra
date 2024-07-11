package ch.taeko.TCBoatTweakerUltra.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntity.class)
public abstract class BoatMixin extends Entity {
    public BoatMixin(EntityType<?> type, World world) {
	   super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void setStepHeight(CallbackInfo ci) { this.setStepHeight(1F);}

}