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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class TanningRackBlock extends Block {

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
                this.stateDefinition.any().setValue(STAGE, Stage.EMPTY)
        );
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

        // RAW stage → scrape with knives / tools
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

        // SCRAPED with tools / anything else → handled later when we add tanning logic
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }
}