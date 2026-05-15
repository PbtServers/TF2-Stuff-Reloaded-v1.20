package net.minecraft.world;

import net.minecraft.network.chat.Component;

public class ServerBossEvent {
	public ServerBossEvent(Component name, BossInfo.Color color, BossInfo.Overlay overlay) {}

	public void setPercent(float percent) {}

	public void addPlayer(Object player) {}

	public void removePlayer(Object player) {}
}
