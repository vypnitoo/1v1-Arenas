package com.vypnito.arena.arenas;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Arenas {

	private final String name;
	private Location pos1;
	private Location pos2;
	private ArenaSettings settings;

	private final Set<UUID> playersInArena = new HashSet<>();
	private boolean wallActive = false;
	private final Map<Location, BlockData> originalWallBlocks = new HashMap<>();

	public Arenas(String name, Location pos1, Location pos2, ArenaSettings settings) {
		this.name = name;
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.settings = settings;
	}

	public String getName() {
		return name;
	}

	public Location getPos1() {
		return pos1;
	}

	public void setPos1(Location pos1) {
		this.pos1 = pos1;
	}

	public Location getPos2() {
		return pos2;
	}

	public void setPos2(Location pos2) {
		this.pos2 = pos2;
	}

	public ArenaSettings getSettings() {
		return settings;
	}

	public void setSettings(ArenaSettings settings) {
		this.settings = settings;
	}

	public void addPlayer(UUID playerUuid) {
		playersInArena.add(playerUuid);
	}

	public void removePlayer(UUID playerUuid) {
		playersInArena.remove(playerUuid);
	}

	public Set<UUID> getPlayers() {
		return new HashSet<>(playersInArena);
	}

	public boolean isPlayerInArena(Player player) {
		return playersInArena.contains(player.getUniqueId());
	}

	public boolean isWallActive() {
		return wallActive;
	}

	public void createBoundaryWall() {
		if (pos1 == null || pos2 == null || settings.getWallMaterial() == null) {
			return;
		}

		Material wallMaterial = settings.getWallMaterial();
		originalWallBlocks.clear();

		Set<Material> replaceableMaterials = ArenaManager.getReplaceableWallMaterials();
		if (replaceableMaterials.isEmpty()) {
			return;
		}

		int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
		int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
		int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
		int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
		int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
		int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					Location currentLocation = new Location(pos1.getWorld(), x, y, z);

					if (isLocationInWall(currentLocation)) {
						Block block = currentLocation.getBlock();

						if (replaceableMaterials.contains(block.getType())) {
							originalWallBlocks.put(currentLocation.clone(), block.getBlockData());
							block.setType(wallMaterial);
						} else if (block.getType() == wallMaterial) {
							originalWallBlocks.put(currentLocation.clone(), block.getBlockData());
						}
					}
				}
			}
		}
		wallActive = true;
	}

	public void removeBoundaryWall() {
		if (pos1 == null || pos2 == null) {
			return;
		}

		for (Map.Entry<Location, BlockData> entry : originalWallBlocks.entrySet()) {
			Location loc = entry.getKey();
			BlockData data = entry.getValue();
			if (loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getChunk())) {
				loc.getBlock().setBlockData(data);
			}
		}
		originalWallBlocks.clear();
		wallActive = false;
	}

	public boolean isLocationInWall(Location location) {
		if (pos1 == null || pos2 == null || location.getWorld() == null ||
				!location.getWorld().equals(pos1.getWorld())) {
			return false;
		}

		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();

		int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
		int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
		int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
		int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
		int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
		int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

		boolean isInBoundedRegion = (x >= minX && x <= maxX &&
				y >= minY && y <= maxY &&
				z >= minZ && z <= maxZ);

		if (!isInBoundedRegion) {
			return false;
		}

		boolean isXBoundary = (x == minX || x == maxX);
		boolean isYBoundary = (y == minY || y == maxY);
		boolean isZBoundary = (z == minZ || z == maxZ);

		return isXBoundary || isYBoundary || isZBoundary;
	}
}