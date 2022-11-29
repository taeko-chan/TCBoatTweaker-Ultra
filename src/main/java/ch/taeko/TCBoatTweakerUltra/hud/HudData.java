package ch.taeko.TCBoatTweakerUltra.hud;

import ch.taeko.TCBoatTweakerUltra.client.TCBoatTweakerClient;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class HudData {

    public int rpm;
    public double torque;
    public double speed;
    public String name;

    public HudData() {
	   this.name = TCBoatTweakerClient.client.player.getEntityName();
    }
    public void update() {
	   BoatEntity boat = (BoatEntity)TCBoatTweakerClient.client.player.getVehicle();
	   // Ignore vertical speed
	   Vec3d velocity = boat.getVelocity().multiply(1, 0, 1);
	   this.speed = velocity.length() * 20d; // Speed in Minecraft's engine is in meters/tick.
    }
    public double getTorque() {
	   return this.torque;
    }
    public double getRpm() {
	   return this.rpm;
    }
    public void setTorque(double t) {
	   this.torque = t;
    }
    public void setRpm(int r) {
	   this.rpm = r;
    }
}
