package com.trollmonkey.butchercraft_tannery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.trollmonkey.butchercraft_tannery.ButchercraftTannery;

public class TanningRackBlockEntity extends BlockEntity {

    // Tracks tanning progress
    int progress = 0;
    // For now: 60 seconds (20 ticks/sec * 60)
    private static final int MAX_PROGRESS = 20 * 60;

    public TanningRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TANNING_RACK.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TanningRackBlockEntity be) {
    if (level.isClientSide) return;

    TanningRackBlock.Stage stage = state.getValue(TanningRackBlock.STAGE);

    if (stage == TanningRackBlock.Stage.SCRAPED) {
        boolean hasSmoke = hasCampfireSmoke(level, pos);
        boolean hasSky = level.canSeeSky(pos.above());
        boolean isDay = level.isDay();
        boolean canTan = hasSmoke && hasSky && isDay;

        ButchercraftTannery.LOGGER.info(
                "[Tannery] Tick at {}, stage={}, progress={}, smoke={}",
                pos,
                state.getValue(TanningRackBlock.STAGE),
                be.progress,
                hasCampfireSmoke(level, pos)
        );

        if (canTan) {
            // Actively tanning
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
        } else {
            // Conditions not met: pause progress, but don't reset
        }

    } else {
        // Any non-SCRAPED stage: reset progress if needed
        if (be.progress != 0) {
            be.progress = 0;
            be.setChanged();
        }
    }
}

    // Must be two blocks over a campfire: rack at Y, gap at Y-1, campfire at Y-2
    private static boolean hasCampfireSmoke(Level level, BlockPos pos) {
        BlockState belowTwo = level.getBlockState(pos.below(2));
        return belowTwo.is(Blocks.CAMPFIRE) || belowTwo.is(Blocks.SOUL_CAMPFIRE);
    }

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