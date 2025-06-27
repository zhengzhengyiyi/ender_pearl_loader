package com.zhengzhengyiyimc;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhengzhengyiyimc.command.Player;

public class Ender_pearl_loader implements ModInitializer {
	public static final String MOD_ID = "ender_pearl_loader";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Player.register();
	}
}