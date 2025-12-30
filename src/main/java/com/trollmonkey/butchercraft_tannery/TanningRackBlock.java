package com.trollmonkey.butchercraft_tannery;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;


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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }
}