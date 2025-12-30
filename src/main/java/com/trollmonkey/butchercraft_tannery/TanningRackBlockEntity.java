package com.trollmonkey.butchercraft_tannery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TanningRackBlockEntity extends BlockEntity {

    // Simple progress counter
    private int progress = 0;
    // For now: 60 seconds (20 ticks/sec * 60)
    private static final int MAX_PROGRESS = 20 * 60;

    public TanningRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TANNING_RACK.get(), pos, state);
    }

    // Called every tick by the ticker in TanningRackBlock
    public static void tick(Level level, BlockPos pos, BlockState state, TanningRackBlockEntity be) {
        if (level.isClientSide()) return;

        var stage = state.getValue(TanningRackBlock.STAGE);

        if (stage == TanningRackBlock.Stage.SCRAPED) {
            be.progress++;

            if (be.progress >= MAX_PROGRESS) {
                be.progress = 0;
                level.setBlock(
                        pos,
                        state.setValue(TanningRackBlock.STAGE, TanningRackBlock.Stage.LEATHER),
                        3
                );
            }

            be.setChanged();
        } else if (be.progress != 0) {
            // Reset when we’re no longer in SCRAPED stage
            be.progress = 0;
            be.setChanged();
        }
    }

    // 1.21.1 uses *loadAdditional* and *saveAdditional* with a HolderLookup.Provider
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getInt("Progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", this.progress);
    }
}