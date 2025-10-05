package com.vypnito.arena.player;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
	private final Map<UUID, PlayerState> playerStates = new HashMap<>();

	public void setPlayerState(Player player, PlayerState state) {
		playerStates.put(player.getUniqueId(), state);
	}

	public PlayerState getPlayerState(Player player) {
		return playerStates.get(player.getUniqueId());
	}

	public void clearPlayerState(Player player) {
		playerStates.remove(player.getUniqueId());
	}
}