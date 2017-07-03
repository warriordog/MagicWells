MagicWells:    
A bukkit plugin that adds magic wells to the game
---
MagicWells adds naturally-generating "magic wells"
with special powers.  These wells can currently be used
to teleport or ~~wish for items~~ (not implemented).  Wells can be claimed by
a player, ~~who must invest rare resources to protect 
his or her claim~~ (also not implemented).

NOTE:  The default well spawn rate is 15%.  This makes
them spawn EVERYWHERE, which is good for judging but
would not be the default if the plugin was released.

Currently supported well features:
* Wells generate randomly in the world
* Inspect wells by clicking or right clicking on them
* Claim wells by jumping in
* Drop redstone in a well to teleport to your home well
* Drop (named) glowstone dust in a well to teleport to a named well
* Drop a diamond in a well to teleport to a random unclaimed well.

Currently supported commands:
* /mwinfo - list your claimed wells
* /mwsethome <name> - set your home well
* /mwreload - reload the plugin

Configuration options:
* config.yml - global plugin settings
* structure/well.structure - structure of wells that spawn
* syllables.lst - syllables / "word fragments" that are pieced together to name wells

Data storage:
* Option of internal or external SQL database
* Requires HSQLDB (HyperSQL) library to compile, but not to run.
* External database support is NOT YET IMPLEMENTED.