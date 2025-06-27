package com.zhengzhengyiyimc.command;

import java.util.HashSet;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class Player {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerPlayerCommand(dispatcher);
        });
    }

    private static void registerPlayerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("player")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    ServerWorld world = source.getWorld();

                    world.getServer().execute(() -> {
                        // FakePlayer fakePlayer = FakePlayer.get(world, new GameProfile(UUID.randomUUID(), source.getName() + "_fakeplayer"));
                        ServerPlayerEntity fakePlayer = new ServerPlayerEntity(source.getServer(), world, new GameProfile(UUID.randomUUID(), source.getName() + "_fakeplayer"), SyncedClientOptions.createDefault()){
                            @Override
                            public boolean isSpectator() { return false; }
                            @Override
                            public boolean isCreative() { return false; }
                            @Override
                            public boolean isInvulnerableTo(DamageSource damageSource) {
                                return false;
                            }
                            @Override
                            public void tick() {
                                super.tick();

                                if (this.age % 5 == 0) {
                                    this.networkHandler.syncWithPlayerPosition();
                                    this.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(
                                        this.getX(), this.getY(), this.getZ(),
                                        this.getYaw(), this.getPitch(),
                                        new HashSet<>(), this.age
                                    ));
                                }
                            }
                        };

                        fakePlayer.networkHandler = new ServerPlayNetworkHandler(source.getServer(), null, fakePlayer, ConnectedClientData.createDefault(fakePlayer.getGameProfile(), false));

                        Vec3d pos = source.getPosition();
                        fakePlayer.refreshPositionAndAngles(
                            pos.x, pos.y, pos.z,
                            source.getRotation().y,
                            source.getRotation().x
                        );

                        // fakePlayer.setNoGravity(false);
                        // fakePlayer.setInvulnerable(false);
                        // fakePlayer.setSilent(false);

                        source.getServer().getPlayerManager().onPlayerConnect(
                            null,
                            fakePlayer,
                            ConnectedClientData.createDefault(fakePlayer.getGameProfile(), false)
                        );

                        world.spawnEntity(fakePlayer);

                        world.getChunkManager().updatePosition(fakePlayer);
                        fakePlayer.networkHandler.syncWithPlayerPosition();

                        source.sendFeedback(() -> Text.literal("generated player"), false);
                    });
                    return 1;
                })
        );
    }

    @SuppressWarnings("unused")
    private static final class FakeClientConnection extends ClientConnection {
		private FakeClientConnection() {
			super(NetworkSide.SERVERBOUND);
		}
	}
}
