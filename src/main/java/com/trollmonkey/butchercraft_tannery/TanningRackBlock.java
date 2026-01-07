package com.trollmonkey.butchercraft_tannery;

import com.trollmonkey.butchercraft_tannery.recipe.input.ScrapingInput;
import com.trollmonkey.butchercraft_tannery.registry.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class TanningRackBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static final EnumProperty<Stage> STAGE =
            EnumProperty.create("stage", Stage.class);

    private static final TagKey<Item> SCRAPING_TOOLS = ItemTags.create(
            ResourceLocation.fromNamespaceAndPath(ButchercraftTannery.MODID, "scraping_tools")
    );

    /* ---------------- ENUM ---------------- */

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

    /* ---------------- CONSTRUCTOR ---------------- */

    public TanningRackBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(STAGE, Stage.EMPTY)
        );
    }

    /* ---------------- PLACEMENT ---------------- */

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(STAGE, Stage.EMPTY);
    }

    /* ---------------- ITEM INTERACTION (SCRAPING / PLACING) ---------------- */

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TanningRackBlockEntity rack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStack handStack = player.getItemInHand(hand);
        ItemStack held = rack.getHeld();

        // Place hide on empty rack
        if (held.isEmpty()) {
            if (handStack.is(ButchercraftTannery.RAW_HIDE.get())
                    || handStack.is(ButchercraftTannery.SCRAPED_HIDE.get())) {

                ItemStack one = handStack.copy();
                one.setCount(1);
                rack.setHeld(one);

                if (!player.isCreative()) {
                    handStack.shrink(1);
                }

                return ItemInteractionResult.CONSUME;
            }

            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        // Scrape via recipe
        var scrapeInput = ScrapingInput.of(held, handStack);
        var scrapeMatch = level.getRecipeManager().getRecipeFor(
                ModRecipeTypes.scraping(),
                scrapeInput,
                level
        );

        if (scrapeMatch.isPresent()) {
            var recipe = scrapeMatch.get().value();

            ItemStack out = recipe.getResultItem(level.registryAccess()).copy();
            rack.setHeld(out);

            if (!player.isCreative() && handStack.isDamageableItem()) {
                handStack.hurtAndBreak(recipe.toolDamage(), player, EquipmentSlot.MAINHAND);
            }

            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /* ---------------- EMPTY HAND INTERACTION (TAKE LEATHER) ---------------- */

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TanningRackBlockEntity rack)) return InteractionResult.PASS;

        ItemStack held = rack.getHeld();

        if (!held.isEmpty() && held.is(Items.LEATHER)) {
            if (!player.addItem(held.copy())) {
                player.drop(held.copy(), false);
            }
            rack.clearHeld();
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    /* ---------------- BLOCK BREAK ---------------- */

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TanningRackBlockEntity rack && !rack.isEmpty()) {
                popResource(level, pos, rack.getHeld().copy());
                rack.clearHeld();
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /* ---------------- STATE ---------------- */

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STAGE);
    }

    /* ---------------- BLOCK ENTITY ---------------- */

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

        return (lvl, p, st, be) -> {
            if (be instanceof TanningRackBlockEntity rackBe) {
                TanningRackBlockEntity.tick(lvl, p, st, rackBe);
            }
        };
    }

    /* ---------------- STAGE DERIVATION ---------------- */

    public static Stage stageFor(ItemStack stack) {
        if (stack.isEmpty()) return Stage.EMPTY;
        if (stack.is(ButchercraftTannery.RAW_HIDE.get())) return Stage.RAW;
        if (stack.is(ButchercraftTannery.SCRAPED_HIDE.get())) return Stage.SCRAPED;
        if (stack.is(Items.LEATHER)) return Stage.LEATHER;
        return Stage.EMPTY;
    }
}