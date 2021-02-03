package me.tobystrong.compactstorage.item;

import me.tobystrong.compactstorage.CompactStorage;
import me.tobystrong.compactstorage.container.BackpackContainer;
import me.tobystrong.compactstorage.util.ItemStackHandlerUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.UUID;

public class BackpackItem extends Item {
    public BackpackItem() {
        super(new Properties().group(CompactStorage.compactStorageItemGroup).maxStackSize(1));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if(!worldIn.isRemote) {
            if(!playerIn.getHeldItem(handIn).hasTag()) {
                CompoundNBT tag = new CompoundNBT();
                tag.putInt("width", 9);
                tag.putInt("height", 3);

                playerIn.getHeldItem(handIn).setTag(tag);
            }

            if(handIn == Hand.MAIN_HAND) {
                ItemStack mainHand = playerIn.getHeldItemMainhand();
                ItemStack offHand = playerIn.getHeldItem(Hand.OFF_HAND);
                BlockPos pos = new BlockPos(playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ());
                pos = pos.add(0f, 1f, 0f);
                ServerWorld serverWorld = (ServerWorld) worldIn;

                if(offHand.getItem() == CompactStorage.upgrade_row) {
                    int width = mainHand.getTag().getInt("width");

                    if(width < 24) {
                        mainHand.getTag().putInt("width", width + 1);
                        serverWorld.spawnParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX(), pos.getY(), pos.getZ(), 5,0.25,0, 0.25, 0);
                        worldIn.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1f);
                        offHand.setCount(offHand.getCount() - 1);
                        return ActionResult.resultPass(mainHand);
                    } else {
                        serverWorld.spawnParticle(ParticleTypes.ANGRY_VILLAGER, pos.getX(), pos.getY(), pos.getZ(), 5,0.25,0, 0.25, 0);
                        worldIn.playSound(null, pos, SoundEvents.UI_TOAST_OUT, SoundCategory.PLAYERS, 1f, 1f);
                        playerIn.sendMessage(new TranslationTextComponent("message.compactstorage.upgrade.max_width_backpack"), UUID.randomUUID());
                        return ActionResult.resultFail(mainHand);
                    }
                } else if(offHand.getItem() == CompactStorage.upgrade_column) {
                    int height = mainHand.getTag().getInt("height");

                    if(height < 12) {
                        mainHand.getTag().putInt("height", height + 1);
                        serverWorld.spawnParticle(ParticleTypes.HAPPY_VILLAGER, pos.getX(), pos.getY(), pos.getZ(), 5,0.25,0, 0.25, 0);
                        worldIn.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1f, 1f);
                        offHand.setCount(offHand.getCount() - 1);
                        return ActionResult.resultPass(mainHand);
                    } else {
                        serverWorld.spawnParticle(ParticleTypes.ANGRY_VILLAGER, pos.getX(), pos.getY(), pos.getZ(), 5,0.25,0, 0.25, 0);
                        worldIn.playSound(null, pos, SoundEvents.UI_TOAST_OUT, SoundCategory.PLAYERS, 1f, 1f);
                        playerIn.sendMessage(new TranslationTextComponent("message.compactstorage.upgrade.max_height_backpack"), UUID.randomUUID());
                        return ActionResult.resultFail(mainHand);
                    }
                }
            }

            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) playerIn;
            NetworkHooks.openGui(serverPlayerEntity, new BackpackItemContainerProvider(handIn, playerIn.getHeldItem(handIn)), (buf) -> {
                buf.writeInt(handIn == Hand.MAIN_HAND ? 0 : 1);
            });
        }

        return ActionResult.resultPass(playerIn.getHeldItem(handIn));
    }

    public class BackpackItemContainerProvider implements INamedContainerProvider {
        private int inventoryWidth;
        private int inventoryHeight;
        private ItemStackHandler inventory;
        private Hand hand;

        public BackpackItemContainerProvider(Hand hand, ItemStack backpack) {
            int inventoryWidth = 9;
            int inventoryHeight = 3;

            if(backpack.getTag() == null) {
                backpack.setTag(new CompoundNBT());
            }

            CompoundNBT tag = backpack.getTag();

            if(!tag.contains("width")) {
                tag.putInt("width", inventoryWidth);
                tag.putInt("height", inventoryHeight);
            } else {
                inventoryWidth = tag.getInt("width");
                inventoryHeight = tag.getInt("height");
            }

            ItemStackHandler inventoryHandler = new ItemStackHandler(inventoryWidth * inventoryHeight);

            if(tag.contains("Inventory")) {
                inventoryHandler.deserializeNBT(tag.getCompound("Inventory"));
                inventoryHandler = ItemStackHandlerUtil.validateHandlerSize(inventoryHandler, inventoryWidth, inventoryHeight);
            }

            this.inventoryWidth = inventoryWidth;
            this.inventoryHeight = inventoryHeight;
            this.inventory = inventoryHandler;
            this.hand = hand;
        }

        @Override
        public ITextComponent getDisplayName() {
            return new TranslationTextComponent("container.compactstorage.backpack");
        }

        @Override
        public Container createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity playerEntity) {
            return new BackpackContainer(windowID, playerInventory, inventoryWidth, inventoryHeight, inventory, hand);
        }
    }
}
