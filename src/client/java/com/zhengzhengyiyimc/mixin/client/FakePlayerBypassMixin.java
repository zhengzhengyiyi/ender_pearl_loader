package com.zhengzhengyiyimc.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class FakePlayerBypassMixin {
    @Inject(method = "onEntitySpawn", at = @At("HEAD"), cancellable = true)
    private void allowFakePlayer(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        if (packet.getEntityType() == EntityType.PLAYER) {
            ci.cancel();
            
            ClientWorld world = ((ClientPlayNetworkHandler)(Object)this).getWorld();
            OtherClientPlayerEntity fakePlayer = new OtherClientPlayerEntity(
                world, 
                new GameProfile(packet.getUuid(), "fake player")
            );
            
            fakePlayer.updateTrackedPosition(packet.getX(), packet.getY(), packet.getZ());
            fakePlayer.setPitch(packet.getPitch());
            fakePlayer.setYaw(packet.getYaw());
            
            world.addEntity(fakePlayer);
        }
    }
}