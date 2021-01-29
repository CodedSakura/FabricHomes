# FabricHomes
[![CurseForge downloads](http://cf.way2muchnoise.eu/short_441291.svg)](https://www.curseforge.com/minecraft/mc-mods/fabrichomes)
[![GitHub release version](https://img.shields.io/github/v/release/CodedSakura/FabricHomes)](https://github.com/CodedSakura/FabricHomes)  
A server-side Fabric mod that adds /home command-set.  
Works for Minecraft 1.16.2+ (snapshots not fully tested)  
Requires [FabricAPI](https://www.curseforge.com/minecraft/mc-mods/fabric-api)  

## Commands
`/home [<name>]` - Teleports you to the specified home (defaults to "main")  
`/sethome [<name>]` - Sets a new home with the specified name (defaults to "main")  

`/homes` - Alias for `/homes list`  
`/homes list` - Lists all homes  
`/homes delete` - Removes the specified home  

### OP level 2 permissions
`/homes list <player>` - Lists homes available to that player  
`/homes config [<name> [<value>]]` - sets or gets a config value  

## Configuration

Can be found in `config/FabricHomes.properties`.  
Configuring through commands automatically rewrites the file.

`cooldown` - The minimum time between warping. Default: 15 (seconds)  
`bossbar` - Whether to enable the boss bar indication for standing still, if set to false will use action bar for time. Default: true  
`stand-still` - How long should the player stand still for after accepting a tpa or tpahere request. Default: 5 (seconds)  
`max-homes` - How many homes can a player have at once. Default: 2  
