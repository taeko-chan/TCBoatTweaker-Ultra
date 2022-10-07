package ch.taeko.TCBoatTweakerUltra;

// import net.minecraft.util.math.BlockPos;
// import net.minecraft.util.math.Vec3d;

// import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Utilities {

    public static boolean surroundingBlocksAreSlippery(BlockPos pos, World world) {
	   if (world.getBlockState(pos.north()).getBlock().getSlipperiness() > 0.8) {
		  return true;
	   } else if (world.getBlockState(pos.south()).getBlock().getSlipperiness() > 0.8) {
		  return true;
	   } else if (world.getBlockState(pos.east()).getBlock().getSlipperiness() > 0.8) {
		  return true;
	   } else return world.getBlockState(pos.west()).getBlock().getSlipperiness() > 0.8;
    }

    /*
    public static double convertToMS(double v) {
	   return 20.71 * v;
    }

    public static double vectorToLinear(Vec3d vector) {
	   return Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.y, 2) + Math.pow(vector.z, 2));
    }

    public static BlockPos determineClosest(List<BlockStateHeight> l, BlockPos ps) {

	   List<Double> distances = null;
	   BlockPos closest = null;

	   for (BlockStateHeight p : l) {
		  Vec3d v = new Vec3d(p.blockPos.getX(), p.blockPos.getY(), p.blockPos.getZ());
		  distances.add(v.distanceTo(new Vec3d(
				ps.getX(), ps.getY(), ps.getZ()
		  )));
	   }

	   double cdist = distances.get(0);

	   for (int i = 0; i <= distances.size(); i++) {
		  if (distances.get(i) < cdist) {
			 cdist = distances.get(i);
			 closest = l.get(i).blockPos;
		  }
	   }
	   return closest != null ? closest : l.get(0).blockPos;
    }
    */

}
