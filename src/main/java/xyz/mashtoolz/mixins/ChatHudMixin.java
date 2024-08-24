package xyz.mashtoolz.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.mashtoolz.config.Config;
import xyz.mashtoolz.helpers.XPDisplay;
import xyz.mashtoolz.utils.TextUtils;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(ChatHud.class)
public class ChatHudMixin {

	@Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "HEAD"), cancellable = true)
	private void addMessage(Text text, @Nullable MessageSignatureData messageSignatureData, int i, @Nullable MessageIndicator messageIndicator, boolean bl, CallbackInfo ci) {
		handleMessage(text, ci);
	}

	private void handleMessage(Text text, CallbackInfo ci) {

		if (!Config.xpDisplay.enabled)
			return;

		var message = text.getString().replaceAll("[.,]", "");
		if (TextUtils.escapeStringToUnicode(message, false).startsWith("\\uf804"))
			return;

		for (var regex : Config.xpRegexes) {
			var match = regex.getPattern().matcher(message);

			if (!match.find())
				continue;

			ci.cancel();

			switch (regex.getKey()) {
				case "skillXP": {

					var color = "#D1D1D1";

					try {
						color = text.getSiblings().get(0).getStyle().getColor().toString();
					} catch (Exception e) {
					}

					if (color.matches("#[0-9A-Fa-f]{6}"))
						color = "<" + color + ">";
					else
						color = Formatting.byName(color).toString();

					var key = match.group(1);
					if (!Config.xpDisplays.containsKey(key))
						Config.xpDisplays.put(key, new XPDisplay(key, color, 0, System.currentTimeMillis(), false));

					var display = Config.xpDisplays.get(key);
					display.setXP(Integer.parseInt(match.group(2)) + display.getXP());
					display.setTime(System.currentTimeMillis());
					display.setColor(color);
					break;
				}

				case "combatXP": {
					var key = "Combat";
					if (!Config.xpDisplays.containsKey(key))
						Config.xpDisplays.put(key, new XPDisplay(key, "<#8AF828>", 0, System.currentTimeMillis(), false));

					var display = Config.xpDisplays.get(key);
					display.setXP(Integer.parseInt(match.group(1)) + display.getXP());
					display.setTime(System.currentTimeMillis());
					display.setColor("<#8AF828>");
					break;
				}
			}
			return;
		}
	}

}
