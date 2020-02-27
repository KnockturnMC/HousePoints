# HousePoints

![banner](https://i.imgur.com/WekXaRn.png "House Points Banner")

## Dependencies

Spigot 1.12

Java 8

## Note

This plugin was originally intended for a Harry Potter themed server, but I have recently modified it so that you could also use it on another server and put any house name you like.

## Command
`/points [give/take] [house] [points] (username) (reason)`
* Give/Take can change depending on config
* House is either the name of the house or the shortcut in the configuration
* points are the amount
* Username is a player you wish to display earning the points
* Reason is why the user deserves those points.

## Permissions

HousePoints.see - Permission to view house standings

HousePoints.give - Permission to give house points to all houses

HousePoints.take - Permission to take housepoints from all houses

HousePoints.give.<housename> - Permission to give housepoints to a specific house.
  
HousePoints.take.<housename> - Permission to take housepoints from a specific house.
  
HousePoints.create.sign - Permission to create a housepoints sign

HousePoints.delete.sign - Permission to delete a housepoints sign
  
## Configuration


```yaml
# This section is configuration to setup houses.  They can be any name you would like, 
# but whatever you name them must also appear on the sign for points. 

# If you name them something too long to fit on a minecraft sign, then you will not be 
# able to make a points sign.

# The points portion of this updates on load and unload.

# The block can be set to anything in the Material enum, but should be a block, if it is set to wool, 
# setting a DyeColor from the spigot enum will change the color of the wool.

# ChatColor is the color that the house will appear when in chat.  Pick this from ChatColor enum.

# Short cut is a short cut you can use when doing the /house command to replace the 
# house so you don't have to type it out.
#
# custom-item is the ability to use custom models with an armor stand. Since there are two ways of doing this,
# I have set it up so you can do it both ways.
#
# The first way uses Optifine where it renames a block and places it as a helmet
# For this, {rank} needs to be located where you will rename it and the rank will be:
# 1 = 1st place, 2 = 2nd etc...
#
# The second way uses the Custom Model Data on Item Meta and sets the id.
# For the Custom Item Meta you will set the original ID for 1st place, and you need
# at least 3 more slots (All IDs must be 7 digits long)
# ie: 1000001 = 1st gryffindor, 1000001 = 2nd gryffindor, 1000003 = 3rd gryffindor, 1000004 = 4th gryffindor,
#     1000005 = 1st ravenclaw, 1000006 = 2nd ravenclaw ... etc
# You only need to put the 1st place number, the rest will be done for you, this is the id that you put into the
# resource pack json for the specific block type.
# https://www.planetminecraft.com/forums/communities/texturing/new-1-14-custom-item-models-tuto-578834/
Houses:
  House:
    points: 0
    material: RED_WOOL
    chatColor: DARK_RED
    shortcut: g
    custom-item:
      rename: gryffindor-{rank}
      id: 1000001

# Use this for the customized item. Material is what material to set it as, and the no-points is for the id, and rename is for renaming. x, y, z, is how far to offset from the 0,0,0 of the block. .5 .5 on x and y will center them.
custom-item:
  material: STONE
  no-points: 1000000
  rename: no-points
  x: .5
  y: 0
  z: .5
# This section is a representation of all the strings used in the plugin, and will allow users to alter as they see fit,
# messages, it will also allow them to hook in with the message ids to a custom language. The messageids are the path
# except for messages. as it would be in a bukkit config.
messages:
  # Give/Take sections of messages have the ability to add variables, and you can rearrange them at will.
  # Variables will only be filled in when present. Otherwise they will fill with an empty string.
  # Here are the variables you can use in the string.
  # {player} = player who receives points
  # {points} = points given/taken
  # {reason} = reason
  # {house}= house
  # {hc}= house color
  # {giver}= person who altered the points
  # Also supports minecraft colors with & as the color variable.
  give:
    houseOnly: "&e{giver}&r has rewarded {hc}{house}&r with &e{points}&r!"
    playerNoReason: "&e{giver}&r has rewarded &e{player}&r {hc}{house}&r with &e{points}&r!"
    playerWithReason: "&e{giver}&r has rewarded &e{player}&r {hc}{house}&r with &e{points}&r for {reason}!"
    reasonOnly: "&e{giver}&r has rewarded {hc}{house}&r with &e{points}&r for {reason}!"
  take:
    houseOnly: "&e{giver}&r has taken &e{points}&r from {hc}{house}&r!"
    playerNoReason: "&e{giver}&r has taken &e{points}&r from &e{player}&r in {hc}{house}&r!"
    playerWithReason: "&e{giver}&r has taken &e{points}&r from &e{player}&r in {hc}{house}&r for {reason}!"
    reasonOnly: "&e{giver}&r has taken &e{points}&r from {hc}{house}&r for {reason}!"
  permission:
    no_permission_command: "&cYou do not have permission to perform this command!"
    no_permission_action: "&cYou do not have permission to do that!"
  command:
    syntax: "&7Correct House Points Syntax is /points [+/-] [house] [points] (player) (reason)"
    not_house: "&cThat doesn't appear to be a house nor a shortcut for a house!"
    event_cancelled: "&cYour event got cancelled by another plugin!"
  listener:
    wallsign: "&cOnly wall signs are supported."
  
# This list can be as long as you like, add this in the instance that another 
# language community would like to change the word for
# adding or subtracting points.
positive:
- add
- plus
- "+"
- give
negative:
- take
- remove
- subtract
- "-"

# If you send from the console, this is the 'person' it will send from.
consoleSender: The Gods Above

# Color of the message, this will turn into an enum value, but using spaces is okay, 
# the plugin translated this to LIGHT_PURPLE
MessageColor: light purple

# The title on top of /points
StandingsTitle: "   [Current House Standings]"

# If you would like a title before the message broadcast for house points.
UseTitle: true

# Block or Item_rename or Item_NBT. Block uses minecraft blocks to choose, and Item will place an item on an itemstand.
pointType: Block
# If points will be represented above the signs or not.
# Turn this to false if you just want signs.
pointRepresentation: true

# If this is false, 0 points will not be represented as no blocks / ahve a 0 representation for items.
# True to have 0 points have its own indicator.
noPointsRepresentation: true

# Title and color. Does not currently support colors, support may be added later.
Title:
  title: [House Points]
  color: dark purple

# Do not touch this, this records the location of signs.  When you delete a sign, it will 
# also remove from this list, generally though leave this alone.
Locations:
```
The following interfaces can be implemented in another plugin to modify the way the plugin operates:
LanguageHook
PointData
PointsPlayerData

Examples for how to implement them are being used as the Default concrete class of each. To set them, get the
JavaPlugin's instance and user the appropriate setter to set the interface to override the old interface.

## Sign
Placing a wall sign with [<housename>] will create a sign for the house. Behind the sign blocks will move about depending on rank. Requires that you have the number of blocks above the sign block as there are houses.
