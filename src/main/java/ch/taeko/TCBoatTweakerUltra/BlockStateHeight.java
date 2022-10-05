package ch.taeko.TCBoatTweakerUltra;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockStateHeight {
    public BlockState blockState;
    public BlockPos blockPos;

    public BlockStateHeight(BlockState blockState, BlockPos blockPos) {
        this.blockState = blockState;
        this.blockPos = blockPos;
    }
}