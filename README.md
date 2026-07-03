# Sky Trader
The Sky Trader is a new type of trader who will rarely appear riding alongside their friendly Happy Ghast. Sky Traders sell Skyfare Tickets, which can be used to hitch a ride to the nearest village. Just don't be rude to your captain...
<img width="1919" height="1014" alt="image" src="https://github.com/user-attachments/assets/46ddf22b-af49-4187-9bcf-2aa793c3e8d8" />

# Features
- Sky Trader: Randomly spawns in the sky, with similar frequency to Wandering Traders. Offers players (up to 3 at once) a one-way trip to the nearest village, and sells flight/sky-themed, and nether-themed items
- Smooth & Configurable Flight: The Sky Trader's ghast has several phases, like takeoff, cruising, or landing. Properties such as flight speed, height, and more can be customized via the config (see below)
- Advancements: There are a few advancements in the Adventure tab, and one hidden one
- ???: Your actions have consequences.

# Config
### Spawn Rates & Flight Mechanics
Find the `skytrader.json` file in your Minecraft instance's config folder (your loader's default location). The values in this file control the spawning of the Sky Trader, flight of the Sky Trader's ghast, and the village finder. Not recommended to change `terrainLookaheadDistances`, `terrainSampleInterval`, or `searchRadius`, unless you know what you're doing. Setting `terrainSampleInterval` to a higher value can help with lag if you have multiple flights on your server at once, but the ghast may collide with obstacles more. Setting it lower will make the ghast follow terrain more smoothly, but can cause lag.

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
