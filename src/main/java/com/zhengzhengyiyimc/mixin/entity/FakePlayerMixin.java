package com.zhengzhengyiyimc.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.api.entity.FakePlayer;

@Mixin(FakePlayer.class)
public abstract class FakePlayerMixin {
    @Inject(method="tick", at=@At("HEAD"), cancellable=true)
    public void tick(CallbackInfo ci) {
        ci.cancel();
    }
}
