package com.zhengzhengyiyimc.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.AirBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ChunkTicketType;

@Mixin(EnderPearlEntity.class)
public class EnderPearlEntityMixin {
    private ChunkPos loadedChunkPos;
    private boolean chunkLoaded = false;

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        EnderPearlEntity enderPearl = (EnderPearlEntity) (Object) this;
        
        if (!(enderPearl.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        ChunkPos currentChunkPos = new ChunkPos(enderPearl.getBlockPos());
        ServerChunkManager chunkManager = serverWorld.getChunkManager();

        if (!(enderPearl.getWorld().getBlockState((enderPearl.getBlockPos())).getBlock() instanceof AirBlock || enderPearl.getWorld().getBlockState((enderPearl.getBlockPos())).getBlock() instanceof NetherPortalBlock)) {
            Entity owner = enderPearl.getOwner();
            if (owner instanceof PlayerEntity player) {
                player.teleport(enderPearl.getX(), enderPearl.getY(), enderPearl.getZ());
                serverWorld.getServer().submit(() -> {
                    enderPearl.discard();
                });
            }
        }

        if (!chunkLoaded) {
            chunkManager.addTicket(
                ChunkTicketType.FORCED,
                currentChunkPos,
                1,
                currentChunkPos
            );
            loadedChunkPos = currentChunkPos;
            chunkLoaded = true;
        } else if (!loadedChunkPos.equals(currentChunkPos)) {
            chunkManager.removeTicket(
                ChunkTicketType.FORCED,
                loadedChunkPos,
                1,
                loadedChunkPos
            );
            loadedChunkPos = currentChunkPos;
            chunkManager.addTicket(
                ChunkTicketType.FORCED,
                currentChunkPos,
                1,
                currentChunkPos
            );
        }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void tickEnd(CallbackInfo ci) {
        EnderPearlEntity enderPearl = (EnderPearlEntity) (Object) this;
        
        if (enderPearl.isRemoved() && chunkLoaded && enderPearl.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.getChunkManager().removeTicket(
                ChunkTicketType.FORCED,
                loadedChunkPos,
                1,
                loadedChunkPos
            );
            chunkLoaded = false;
        }
    }
}