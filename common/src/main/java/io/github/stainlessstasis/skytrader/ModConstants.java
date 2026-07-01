package io.github.stainlessstasis.skytrader;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModConstants {
    public static final String MOD_ID = "skytrader";
    public static final String MOD_NAME = "SkyTrader";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}