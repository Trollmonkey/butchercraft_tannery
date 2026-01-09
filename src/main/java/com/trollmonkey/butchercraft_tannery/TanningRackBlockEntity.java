package com.trollmonkey.butchercraft_tannery;

import com.trollmonkey.butchercraft_tannery.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TanningRackBlockEntity extends BlockEntity {

    // Actual item stored on the rack
    private ItemStack held = ItemStack.EMPTY;

    // Tracks tanning progress
    int progress = 0;

    public TanningRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TANNING_RACK.get(), pos, state);
    }

    public ItemStack getHeld() {
        return held;
    }

    public boolean isEmpty() {
        return held.isEmpty();
    }

    private boolean wasRainExposed = false;
    private boolean wasThunderExposed = false;

    // 0 = none, 1 = reset, 2 = ruin

    private byte rainOutcome = 0;

    public void setHeld(ItemStack stack) {
        this.held = stack;
        this.progress = 0;
        setChanged();
        syncStage();
    }

    public void clearHeld() {
        this.held = ItemStack.EMPTY;
        this.progress = 0;
        wasRainExposed = false;
        wasThunderExposed = false;
        rainOutcome = 0;
        setChanged();
        syncStage();
    }

    private void syncStage() {
        if (level == null || level.isClientSide) return;

        BlockState state = getBlockState();
        if (!state.hasProperty(TanningRackBlock.STAGE)) return;

        TanningRackBlock.Stage newStage = TanningRackBlock.stageFor(held);
        if (state.getValue(TanningRackBlock.STAGE) != newStage) {
            level.setBlock(worldPosition, state.setValue(TanningRackBlock.STAGE, newStage), 3);
        } else {
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TanningRackBlockEntity be) {
        if (level.isClientSide) return;

        // If empty or not scraped, no tanning progress
        if (be.held.isEmpty() || TanningRackBlock.stageFor(be.held) != TanningRackBlock.Stage.SCRAPED) {
            if (be.progress != 0) {
                be.progress = 0;
                be.setChanged();
            }
            return;
        }

        // Find a tanning recipe for the held item
        var match = level.getRecipeManager().getRecipeFor(
                ModRecipeTypes.tanning(),
                new SingleRecipeInput(be.held),
                level
        );

        if (match.isEmpty()) {
            if (be.progress != 0) {
                be.progress = 0;
                be.setChanged();
            }
            return;
        }

        var recipe = match.get().value(); // <-- now this is TanningRecipe (typed)
        int maxProgress = recipe.time();

        // --- Environmental Boolean declarations ---
        boolean exposed = level.canSeeSky(pos.above());
        boolean hasSmoke = hasCampfireSmoke(level, pos);
        boolean isDay = level.isDay();
        boolean canTan = hasSmoke && exposed && isDay;
        boolean rainingHere = exposed && level.isRainingAt(pos.above());
        boolean thundering = exposed && level.isThundering() && level.isRainingAt(pos.above());


        // --- Thunder: ruin once when thunder exposure starts ---
        if (thundering && !be.wasThunderExposed) {
            be.wasThunderExposed = true;
            be.clearHeld(); 
            return;
        }

        // If thunder exposure ended, reset flag (so a new thunder event can trigger later)
        if (!thundering && be.wasThunderExposed) {
            be.wasThunderExposed = false;
        }
        if (thundering) return;

        // --- Rain: roll once when rain exposure starts ---
        if (rainingHere && !be.wasRainExposed) {
            be.wasRainExposed = true;

            // Roll once per rain exposure event
            double roll = level.random.nextDouble(); // use world RNG
            be.rainOutcome = (byte) (roll < 0.4 ? 2 : 1); // 40% ruin, 60% reset

         if (be.rainOutcome == 2) {
            // Ruin immediately
             be.clearHeld();
                return;
         } else {
              // Reset progress immediately, then pause until rain ends
             if (be.progress != 0) {
                    be.progress = 0;
                    be.setChanged();
                }
                return; // pause tanning this tick
            }
        }
        
        // While rain continues: enforce stored outcome, no rerolls
        if (rainingHere && be.wasRainExposed) {
            if (be.rainOutcome == 2) {
                // Should have already ruined, but just in case
                be.clearHeld();
                return;
        } else if (be.rainOutcome == 1) {
            // Pause tanning until rain ends
            if (be.progress != 0) {
                be.progress = 0;
                 be.setChanged();
            }
            return;
        }
    }

        // Rain exposure ended (rain stopped or rack covered): clear event + outcome
        if (!rainingHere && be.wasRainExposed) {
         be.wasRainExposed = false;
         be.rainOutcome = 0;
        }

        
        // Smoke + Sun + Sky = Proceed With Tanning. Otherwise, pause.
        if (canTan) {
            be.progress++;

                if (be.progress >= maxProgress) {
                be.progress = 0;
                be.held = recipe.result().copy();
                be.setChanged();
                be.syncStage();
                return;
            }

            be.setChanged();
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
        wasRainExposed = tag.getBoolean("WasRainExposed");
        wasThunderExposed = tag.getBoolean("WasThunderExposed");
        rainOutcome = tag.getByte("RainOutcome");
        if (tag.contains("Held")) {
            this.held = ItemStack.parse(registries, tag.getCompound("Held")).orElse(ItemStack.EMPTY);
        } else {
            this.held = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Progress", this.progress);
        tag.putBoolean("WasRainExposed", wasRainExposed);
        tag.putBoolean("WasThunderExposed", wasThunderExposed);
        tag.putByte("RainOutcome", rainOutcome);
     
        if (!held.isEmpty()) {
            tag.put("Held", held.save(registries));
        
        }
    }
}