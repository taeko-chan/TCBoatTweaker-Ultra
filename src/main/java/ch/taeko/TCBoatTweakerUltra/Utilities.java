package ch.taeko.TCBoatTweakerUltra;

import net.minecraft.util.math.Vec3d;

public class Utilities {

    public enum Gears {
	   FORWARD, REVERSE, I, II, III, IV, V, VI
    }

    public static Gears currentGear = Gears.FORWARD;
    public static double currentGearNumber = 1;

    public static boolean engineRunning = false;

    public static double toSi(double v) {
	   return 20 * v;
    }
    public static Vec3d toSiV(Vec3d v) {
	   return new Vec3d(v.getX() * 20, v.getY() * 20, v.getZ() * 20);
    }
    public static double toMc(double v) {
	   return v / 20;
    }
    public static float getTorqueAtRpm(float rpm) {
	   return (float) (-(210/Math.pow(3500, 4)) * Math.pow(rpm-3500, 4) + 210);
    }

    public static Vec3d toMcV(Vec3d v) {
	   return new Vec3d(v.getX() / 20, v.getY() / 20, v.getZ() / 20);
    }

    public static double linV(Vec3d vector) { return Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.z, 2)); }

    public static double getYawRotation(Vec3d speed) {
	   double x = linV(speed);
	   return -0.1 * x + 6;
    }
}
