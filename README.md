# Sky Trader
The Sky Trader is a rare trader who soars the skies aboard their friendly Happy Ghast. Purchase a Skyfare Ticket and hitch a ride to the nearest village, or call a taxi using a Skyflare!
![The Sky Trader](https://cdn.modrinth.com/data/cached_images/e97720b46eb92e1ba0bd4f7aca625abde28357d2_0.webp)

# Features
- Sky Trader: Spawns randomly in the sky with similar frequency to Wandering Traders. Offers up to 3 players a one-way trip to the nearest village, and sells flight/sky and nether-themed items. They also sell snacks during your flight!
- Skyflare: A purchasable item that lets you summon a Sky Trader on demand. No more waiting around
- Smooth & Configurable Flight: The Sky Trader's Ghast has several phases, like takeoff, cruising, or landing, all of which smoothly follow the terrain, whether it's natural or even player-built. Properties such as flight speed, height, and more can be customized via the config (see below)
- Advancements: A dedicated advancements tab with 15+ advancements. Can you discover the hidden ones?
- Statistics: Track the number of flights you've taken with Farlands Airlines as a custom statistic
- ???: Your actions have consequences.

![Flying toward a village](https://cdn.modrinth.com/data/cached_images/08ebffa31445f44ccd4e933dca3cb48c2bf1e49c.png)

# Config
### Game Rules
You can enable Sky Trader spawns with the `skytrader:spawn_sky_traders` game rule. There is also a game rule for the hidden feature that shall not be named. But yes if you don't like that, you can disable it.

### Spawn Rates & Flight Mechanics
Find the `skytrader.json` file in your Minecraft instance's config folder (your loader's default location). This file controls the spawning of the Sky Trader, flight of the Sky Trader's Ghast, and the village finder. Not recommended to change `terrainLookaheadDistances`, `terrainSampleInterval`, or `searchRadius`, unless you know what you're doing. Setting `terrainSampleInterval` to a higher value can help with lag if you have multiple flights on your server at once, but the Ghast may collide with obstacles more often. Setting it lower will make the Ghast follow terrain more smoothly, but can cause lag.

### Trades/Advancements
The mod's trades and advancements are datapacked under the `skytrader` namespace. You can make a datapack to edit them as you wish. This is not a tutorial, though - that's all you.

# Other Information
### Dependencies
- **Fabric:** Fabric API
- **NeoForge:** None
### Issues?
Report issues [on GitHub](https://github.com/StainlessStasis/SkyTrader/issues)
### Supported Versions
I will only support 26.1.2+ and have no plans to backport. Feel free to do this yourself, provided you follow the license and credit me accordingly
### Modpacks
Go ahead. Same as above - follow license, credit
