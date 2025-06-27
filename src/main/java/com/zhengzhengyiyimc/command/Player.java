package com.zhengzhengyiyimc.command;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

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

                    FakePlayer fakePlayer = FakePlayer.get(source.getWorld());

                    fakePlayer.setPosition(source.getPosition().x, source.getPosition().y, source.getPosition().z);
                    world.spawnEntity(fakePlayer);

                    source.sendFeedback(() -> Text.literal("generated player"), false);
                    return 1;
                })
        );
    }
}