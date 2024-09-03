package xyz.mashtoolz.config;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.world.RaycastContext;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.custom.FaceItem;
import xyz.mashtoolz.custom.FaceTool;
import xyz.mashtoolz.custom.FaceToolType;
import xyz.mashtoolz.utils.PlayerUtils;

public class AutoTool {

	private static FaceLift instance = FaceLift.getInstance();

	public int pickaxe = 15;
	public int woodcuttingaxe = 16;
	public int hoe = 17;

	public FaceTool get(FaceToolType type) {
		return FaceTool.getByType(type);
	}

	public static void update() {

		var client = instance.client;
		var player = client.player;

		var eyePos = player.getEyePos();
		var reach = ClientPlayerEntity.getReachDistance(false);
		var rayEnd = eyePos.add(player.getRotationVector().multiply(reach));
		var blockHitResult = client.world.raycast(new RaycastContext(eyePos, rayEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));

		var inventory = player.getInventory();

		var hotbarSlot = inventory.selectedSlot;
		var stack = player.getMainHandStack();

		var item = new FaceItem(stack);
		var tooltip = item.invalid ? null : item.getTooltip();
		FaceTool currentTool = null;
		if (tooltip != null) {
			for (var tool : FaceTool.values())
				if (tooltip.contains(tool.getFaceToolType().getName())) {
					currentTool = tool;
					break;
				}
		}

		var targetTool = PlayerUtils.getTargetTool(blockHitResult, stack);
		if (targetTool != null && targetTool.getFaceToolType().equals(FaceToolType.BEDROCK))
			return;

		if (tooltip == null || currentTool == null) {
			if (targetTool != null && !inventory.getStack(targetTool.getSlotIndex()).isEmpty())
				PlayerUtils.clickSlot(targetTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
			return;
		}

		var isSlotEmpty = inventory.getStack(currentTool.getSlotIndex()).isEmpty();
		if (targetTool == null) {
			if (isSlotEmpty) {
				PlayerUtils.clickSlot(36 + hotbarSlot, 0, SlotActionType.PICKUP);
				PlayerUtils.clickSlot(currentTool.getSlotIndex(), 0, SlotActionType.PICKUP);
				return;
			}
			PlayerUtils.clickSlot(currentTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
			return;
		}

		if (currentTool.getFaceToolType().equals(targetTool.getFaceToolType()))
			return;

		if (isSlotEmpty) {
			PlayerUtils.clickSlot(36 + hotbarSlot, 0, SlotActionType.PICKUP);
			PlayerUtils.clickSlot(currentTool.getSlotIndex(), 0, SlotActionType.PICKUP);
			PlayerUtils.clickSlot(targetTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
		} else {
			PlayerUtils.clickSlot(currentTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
			PlayerUtils.clickSlot(targetTool.getSlotIndex(), hotbarSlot, SlotActionType.SWAP);
		}
	}
}
