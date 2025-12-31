package com.trollmonkey.butchercraft_tannery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TanningRackBlockEntity extends BlockEntity {

    // Tracks tanning progress
    int progress = 0;

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

        // --- Weather Interaction ---
        boolean exposed = level.canSeeSky(pos.above());

        if (exposed && level.isRainingAt(pos)) {
            boolean isThunder = level.isThundering();
            double roll = Math.random(); // 0.0 - 1.0

            if (isThunder) {
                // Thunderstorm → guaranteed ruin
                be.progress = 0;
                level.setBlock(pos, state.setValue(TanningRackBlock.STAGE, TanningRackBlock.Stage.EMPTY), 3);
                be.setChanged();
                return;
            } else {
                // Rain-only → 60% reset, 40% ruin
                if (roll < 0.4) { // 40% ruin
                    be.progress = 0;
                    level.setBlock(pos, state.setValue(TanningRackBlock.STAGE, TanningRackBlock.Stage.EMPTY), 3);
                    be.setChanged();
                    return;
                } else { // 60% reset, stay SCRAPED
                    if (be.progress != 0) {
                        be.progress = 0;
                        be.setChanged();
                    }
                    /* Do not return; just skip tanning this tick
                    so execution can continue to canTan check below */
                }
            }
        }
        //Log our smoking progress for testing REMOVE WHEN DONE!
        ButchercraftTannery.LOGGER.info(
                "[Tannery] Tick at {}, stage={}, progress={}, smoke={}",
                pos,
                state.getValue(TanningRackBlock.STAGE),
                be.progress,
                hasCampfireSmoke(level, pos)
        );
        //Smoke + Sun + Sky = Proceed With Tanning. Otherwise, pause.
        if (canTan) {
            // Actively tanning
            be.progress++;

            int maxProgress = Config.TANNING_TIME_TICKS.get();  // read from config

            if (be.progress >= maxProgress) {
                be.progress = 0;
                level.setBlock(
                        pos,
                        state.setValue(TanningRackBlock.STAGE, TanningRackBlock.Stage.LEATHER),
                        3
                );
            }

            be.setChanged();
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