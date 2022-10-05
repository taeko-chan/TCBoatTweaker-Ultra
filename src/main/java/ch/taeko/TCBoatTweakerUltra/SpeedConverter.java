package ch.taeko.TCBoatTweakerUltra;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class SpeedConverter {
    public static double convertVectorSpeed(double vx, double vz) {
	   return 20.71 * Math.sqrt(Math.pow(vx,2) + (Math.pow(vz,2)));
    }

    public static BlockPos determineClosest(List<BlockStateHeight> l, BlockPos ps) {
	   List<Double> distances = null;
	   BlockPos closest = null;

	   for (BlockStateHeight p : l) {
		  Vec3d v = new Vec3d(p.blockPos.getX(), p.blockPos.getY(), p.blockPos.getZ());
		  distances.add(v.distanceTo(new Vec3d(ps.getX(), ps.getY(), ps.getZ())));
	   }

	   double cdist = distances.get(0);

	   for (int i = 0; i <= distances.size(); i++) {
		  if (distances.get(i) < cdist) {
			 cdist = distances.get(i);
			 closest = l.get(i).blockPos;
		  }
	   }
	   return closest;
    }

}
