package io.github.stainlessstasis.skytrader;

import io.github.stainlessstasis.skytrader.entity.SkyTraderGhast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Util;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Locates the nearest village structure, offloading the expensive structure search to a background thread to avoid main thread lag spikes.
 * Runs during the SEARCHING phase of the {@link SkyTraderGhast}.
 */
public class VillageLocator {
    private VillageLocator() {}

    /**
     * Starts an async search for the nearest village.
     * Returns null (via the future) if no village could be found.
     * The returned BlockPos has an arbitrary Y.
     * Callers must resolve the real surface height themselves via {@link #resolveSurfacePosition}, on the main thread.
     */
    public static CompletableFuture<@Nullable BlockPos> findNearestVillageStructureAsync(
            ServerLevel serverLevel, BlockPos origin, int searchRadius) {
        return CompletableFuture.supplyAsync(
                () -> findNearestVillageStructureBlocking(serverLevel, origin, searchRadius),
                Util.backgroundExecutor()
        );
    }

    private static @Nullable BlockPos findNearestVillageStructureBlocking(
            ServerLevel serverLevel, BlockPos origin, int searchRadius) {
        var structureRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var villageTag = structureRegistry.get(StructureTags.VILLAGE);
        if (villageTag.isEmpty()) {
            return null;
        }

        var closestVillage = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(
                serverLevel,
                villageTag.get(),
                origin,
                searchRadius / 16,
                false
        );

        return closestVillage != null ? closestVillage.getFirst() : null;
    }

    /**
     * Resolves the real surface Y for a structure center.
     * Must be called on the main server thread: touches chunk/heightmap data.
     */
    public static BlockPos resolveSurfacePosition(ServerLevel serverLevel, BlockPos structureCenter) {
        int surfaceY = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, structureCenter.getX(), structureCenter.getZ());
        if (surfaceY <= serverLevel.getMinY()) {
            var randomState = serverLevel.getChunkSource().randomState();
            surfaceY = serverLevel.getChunkSource().getGenerator().getBaseHeight(
                    structureCenter.getX(),
                    structureCenter.getZ(),
                    Heightmap.Types.WORLD_SURFACE,
                    serverLevel,
                    randomState
            );
        }

        if (surfaceY <= serverLevel.getMinY()) {
            surfaceY = serverLevel.getSeaLevel();
        }

        return new BlockPos(structureCenter.getX(), surfaceY, structureCenter.getZ());
    }
}