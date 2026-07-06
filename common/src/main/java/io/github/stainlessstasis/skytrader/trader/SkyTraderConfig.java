package io.github.stainlessstasis.skytrader.trader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.stainlessstasis.skytrader.ModConstants;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class SkyTraderConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static Data data = new Data();
    private static int CURRENT_VERSION = 2;

    public static class Data {
        public int configVersion = -1;
        public Spawning spawning = new Spawning();
        public Flight flight = new Flight();
        public Search search = new Search();
    }

    public static class Spawning {
        public int tickDelay = 1200;
        public int spawnDelayTicks = 24000;
        public int despawnTicks = 48000;
        public int minSpawnChance = 25;
        public int maxSpawnChance = 75;
        public int spawnChanceIncrease = 25;
        public int spawnOneInXChance = 8;
        public int numberOfSpawnAttempts = 10;
        public int ghastHorizontalClearance = 2;
        public int ghastVerticalClearance = 4;
        public int ghastUpwardSearchLimit = 32;
        public int spawnAltitude = 60;
        public int searchRadius = 48;
    }

    public static class Flight {
        public float maxVerticalSpeed = 0.5f;
        public float turnSpeed = 0.25f;

        public int boardingTicks = 300;
        public int boardingExtensionOnNewPassenger = 100;
        public int maxDepartureAttempts = 3;

        public int takeoffHeight = 30;
        public float takeoffVerticalSpeed = 0.4f;
        public float takeoffForwardSpeed = 0.15f;

        public int cruiseHoverHeight = 75;
        public float cruiseSpeed = 0.8f;
        public int glideStartDistance = 200;

        public double finalApproachDistance = 4d;
        public int landingHoverHeight = 1;
        public double landingArrivedThreshold = 3d;
        public double landingArrivedRadius = 12d;
        public float descendSpeed = 0.3f;
        public float spawnDescentSpeed = 0.5f;
        public float descendHorizontalSpeed = 0.2f;

        public int arrivingTimeoutTicks = 200;
        public int maxLandingAttempts = 3;
        public int landingRetryRadius = 16;
        public double spawnJumpOffHeight = 20d;

        public int dismountGraceTicks = 200;
        public int returnFlightTicks = 600;
        public float returnSpeed = 0.5f;

        public int[] terrainLookaheadDistances = {8, 16, 24, 32};
        public int terrainSampleInterval = 10;
    }

    public static class Search {
        public int searchRadius = 2048;
        public int manualRouteMaxDistance = 10240;
    }

    private SkyTraderConfig() {}

    public static void init(Path loaderConfigDir) {
        configPath = loaderConfigDir.resolve(ModConstants.MOD_ID + ".json");
        load();
    }

    public static Data get() {
        return data;
    }

    public static void load() {
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                Data loaded = GSON.fromJson(reader, Data.class);
                if (loaded != null) {
                    data = loaded;
                    migrate();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read " + configPath, e);
            }
        } else {
            save(); // write defaults on first run
        }
    }

    public static void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save " + configPath, e);
        }
    }

    private static void migrate() {
        boolean changed = false;

        if (data.configVersion < 2) {
            // version 1 (1.0.0 / 1.1.0) -> 2 (1.2.0):
            // configVersion field was added (2)
            // manualRouteMaxDistance was added (10240)
            // cruising flight speed was increased from 0.6 -> 0.8
            data.configVersion = 2;
            if (data.flight.cruiseSpeed == 0.6f) {
                data.flight.cruiseSpeed = 0.8f;
            }
            changed = true;
        }

        data.configVersion = CURRENT_VERSION;
        if (changed) {
            save();
        }
    }
}