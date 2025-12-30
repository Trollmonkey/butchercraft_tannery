package com.trollmonkey.butchercraft_tannery;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

@SuppressWarnings("NullableProblems")
public class TanningRackBlock extends Block {

    public static final EnumProperty<Stage> STAGE =
            EnumProperty.create("stage", Stage.class);

    public enum Stage implements StringRepresentable {
        EMPTY("empty"),
        RAW("raw"),
        SCRAPED("scraped"),
        LEATHER("leather");

        private final String name;

        Stage(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public TanningRackBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(STAGE, Stage.EMPTY)
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            // Client just says "yep, handled" so the hand animates
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getMainHandItem(); // main hand in this pipeline
        Stage stage = state.getValue(STAGE);

        // EMPTY rack → place hides
        if (stage == Stage.EMPTY) {
            if (stack.is(ButchercraftTannery.RAW_HIDE.get())) {
                level.setBlock(pos, state.setValue(STAGE, Stage.RAW), Block.UPDATE_ALL);
                stack.shrink(1);
                return InteractionResult.CONSUME;
            }
            if (stack.is(ButchercraftTannery.SCRAPED_HIDE.get())) {
                level.setBlock(pos, state.setValue(STAGE, Stage.SCRAPED), Block.UPDATE_ALL);
                stack.shrink(1);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        // Final stage → give leather on empty-hand click
        if (stage == Stage.LEATHER && stack.isEmpty()) {
            ItemStack leather = new ItemStack(Items.LEATHER);
            if (!player.addItem(leather)) {
                player.drop(leather, false);
            }
            level.setBlock(pos, state.setValue(STAGE, Stage.EMPTY), Block.UPDATE_ALL);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }
}