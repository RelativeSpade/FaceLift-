package xyz.mashtoolz.custom;

import net.minecraft.util.Identifier;

public class FaceTexture {

	public static final Identifier ITEM_GLOW = id("textures/gui/item_glow.png");
	public static final Identifier ITEM_STAR = id("textures/gui/item_star.png");
	public static final Identifier ABILITY_GLINT = id("textures/gui/ability_glint.png");
	public static final Identifier EMPTY_SHOVEL = id("textures/gui/empty_slot/shovel.png");
	public static final Identifier EMPTY_PICKAXE = id("textures/gui/empty_slot/pickaxe.png");
	public static final Identifier EMPTY_AXE = id("textures/gui/empty_slot/axe.png");
	public static final Identifier EMPTY_HOE = id("textures/gui/empty_slot/hoe.png");

	private static Identifier id(String path) {
		return new Identifier("facelift", path);
	}
}