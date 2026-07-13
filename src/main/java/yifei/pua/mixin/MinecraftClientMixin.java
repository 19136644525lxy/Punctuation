package yifei.pua.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yifei.pua.client.PunctuationClient;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "doItemPick", at = @At("HEAD"), cancellable = true)
    private void cancelItemPick(CallbackInfo ci) {
        if (PunctuationClient.currentMode == PunctuationClient.Mode.MARKER) {
            ci.cancel();
        }
    }
}