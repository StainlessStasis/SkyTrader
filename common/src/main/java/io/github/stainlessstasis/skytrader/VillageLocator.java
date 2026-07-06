package io.github.stainlessstasis.skytrader;

import io.github.stainlessstasis.skytrader.entity.SkyTraderGhast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Locates the nearest village structure.
 * Runs during the SEARCHING phase of the {@link SkyTraderGhast}.
 */
public class VillageLocator {

    private VillageLocator() {}

    /**
     * Starts a search for the nearest village.
     * Returns null (via the future) if no village could be found.
     * Callers must resolve the real surface height themselves via {@link #resolveSurfacePosition}.
     */
    public static CompletableFuture<@Nullable BlockPos> findNearestVillageStructure(
            ServerLevel serverLevel, BlockPos origin, int searchRadius) {

        CompletableFuture<@Nullable BlockPos> future = new CompletableFuture<>();

        serverLevel.getServer().execute(() -> {
            try {
                BlockPos result = findNearestVillageStructureBlocking(serverLevel, origin, searchRadius);
                future.complete(result);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
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