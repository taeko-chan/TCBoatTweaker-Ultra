package ch.taeko.TCBoatTweakerUltra;

import net.minecraft.util.math.Vec3d;

public class Utilities {

    public enum Gears {
	   FORWARD, REVERSE, I, II, III, IV, V, VI
    }

    public static Gears currentGear = Gears.FORWARD;
    public static double currentGearNumber = 1;

    public static double convertToMS(double v) {
	   return 20.71 * v;
    }

    public static double linV(Vec3d vector) {
	   return Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.z, 2));
    }

    public static double getYawRotation(Vec3d speed) {
	   double x = linV(speed);
	   return -0.1 * x + 6;
    }

    public static Vec3d brakingSpeed(Vec3d speed, double b) {

	   double netV = Math.sqrt(Math.pow(speed.getY(), 2) + Math.pow(speed.getZ(), 2));

	   double angleNS = Math.acos(speed.getZ() / netV);
	   double angleEW = Math.acos(speed.getX() / netV);

	   netV = netV > 0 ? netV + b : netV - b;

	   double newX = Math.cos(angleEW) * netV;
	   double newZ = Math.cos(angleNS) * netV;

	   return speed.add(newX, 0.0F, newZ);

    }
}
