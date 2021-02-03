package me.tobystrong.compactstorage.container;

import me.tobystrong.compactstorage.CompactStorage;
import me.tobystrong.compactstorage.block.tile.CompactChestTileEntity;
import me.tobystrong.compactstorage.util.ItemStackHandlerUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class BackpackContainer extends CompactStorageBaseContainer {
    public static BackpackContainer createContainerFromItemstack(int windowID, PlayerInventory playerInventory, net.minecraft.network.PacketBuffer extraData) {
        Hand hand = Hand.values()[extraData.readInt()];
        ItemStack backpack = playerInventory.player.getHeldItem(hand);

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

        return new BackpackContainer(windowID, playerInventory, inventoryWidth, inventoryHeight, inventoryHandler, hand);
    }

    public Hand hand;

    public BackpackContainer(int windowID, PlayerInventory playerInventory, int inventoryWidth, int inventoryHeight, ItemStackHandler inventoryHandler, Hand hand) {
        super(CompactStorage.BACKPACK_CONTAINER_TYPE, windowID, playerInventory, inventoryWidth, inventoryHeight, inventoryHandler);
        this.hand = hand;
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        ItemStackHandler handler = (ItemStackHandler) this.chestInventory;
        playerIn.getHeldItem(hand).getTag().put("Inventory", handler.serializeNBT());
    }
}
