package com.trollmonkey.butchercraft_tannery;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

import javax.annotation.Nullable;

public class TanningRackBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;
	
	public static final EnumProperty<Stage> STAGE =
            EnumProperty.create("stage", Stage.class);

    // Tag for allowed scraping tools: butchercrafttannery:scraping_tools
    private static final TagKey<Item> SCRAPING_TOOLS = ItemTags.create(
            ResourceLocation.fromNamespaceAndPath(ButchercraftTannery.MODID, "scraping_tools")
    );

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
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(STAGE, Stage.EMPTY)
        );
    }
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(STAGE, Stage.EMPTY);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            // Just say “yep, handled” on the client so the hand animates
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getMainHandItem();
        Stage stage = state.getValue(STAGE);

        // EMPTY rack → place raw hide
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

        // RAW stage → scrape with knives / fleshing tool
        if (stage == Stage.RAW) {
            if (stack.is(SCRAPING_TOOLS)) {
                level.setBlock(pos, state.setValue(STAGE, Stage.SCRAPED), Block.UPDATE_ALL);

                // Damage the tool slightly
                if (stack.isDamageableItem()) {
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                }

                return InteractionResult.CONSUME;
            }

            // Wrong tool while raw → do nothing special
            return InteractionResult.PASS;
        }

        // LEATHER stage → empty hand to take leather and clear rack
        if (stage == Stage.LEATHER && stack.isEmpty()) {
            ItemStack leather = new ItemStack(Items.LEATHER);
            if (!player.addItem(leather)) {
                player.drop(leather, false);
            }
            level.setBlock(pos, state.setValue(STAGE, Stage.EMPTY), Block.UPDATE_ALL);
            return InteractionResult.CONSUME;
        }

        // SCRAPED with tools / anything else → handled later (tanning logic is in the BE)
        return InteractionResult.PASS;
    }
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            /* Always drop the rack itself */
            popResource(level, pos, new ItemStack(this.asItem()));

            /* Drop the “contents” based on stage */
            Stage stage = state.getValue(STAGE);
            switch (stage) {
                case RAW -> popResource(level, pos, new ItemStack(ButchercraftTannery.RAW_HIDE.get()));
                case SCRAPED -> popResource(level, pos, new ItemStack(ButchercraftTannery.SCRAPED_HIDE.get()));
                case LEATHER -> popResource(level, pos, new ItemStack(Items.LEATHER));
                default -> { /* EMPTY -> nothing extra */ }
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STAGE);
    }

    // ----- Block Entity plumbing -----

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TanningRackBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (type != ModBlockEntities.TANNING_RACK.get()) {
            return null;
        }

        return (lvl, pos, st, be) -> {
            if (be instanceof TanningRackBlockEntity rackBe) {
                TanningRackBlockEntity.tick(lvl, pos, st, rackBe);
            }
        };
    }
}