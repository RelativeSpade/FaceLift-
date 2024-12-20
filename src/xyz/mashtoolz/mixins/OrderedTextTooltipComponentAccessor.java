package xyz.mashtoolz.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.text.OrderedText;

@Mixin(OrderedTextTooltipComponent.class)
public interface OrderedTextTooltipComponentAccessor {

	@Accessor("text")
	OrderedText getText();
}
