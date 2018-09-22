# HousePoints

## Dependencies

Spigot 1.12
Java 8

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
# This section is configuration to setup houses.  They can be any name you would like, but whatever you name them must also appear on
# the sign for points. If you name them something too long to fit on a minecraft sign, then you will not be able to make a points sign.
# The points portion of this updates on load and unload.
# The block can be set to anything in the Material enum, but should be a block, if it is set to wool, setting a DyeColor 
# from the spigot enum will change the color of the wool.
# ChatColor is the color that the house will appear when in chat.  Pick this from ChatColor enum.
# Short cut is a short cut you can use when doing the /house command to replace the house so you don't have to type it out.
Houses:
  HouseName:
    points: 0
    block:
      material: WOOL
      color: RED
    chatColor: DARK_RED
    shortcut: g
    
# These are messages set up depending on varaible. You can edit them to any language you would like.
# Variables will only be filled in when present. Otherwise they will fill with an empty string.
# Here are the variables you can use in the string.
# %player% = player who receives points
# %points% = points given/taken
# %reason% = reason
# %house% = house
# %hc% = house color
# %giver% = person who altered the points
# Also supports minecraft colors with & as the color variable.
give:
  houseOnly: "&e%giver%&r has rewarded %hc%%house%&r with &e%points%&r!"
  playerNoReason: "&e%giver%&r has rewarded &e%player%&r %hc%%house%&r with &e%points%&r!"
  playerWithReason: "&e%giver%&r has rewarded &e%player%&r %hc%%house%&r with &e%points%&r for %reason%!"
  reasonOnly: "&e%giver%&r has rewarded %hc%%house%&r with &e%points%&r for %reason%!"
take:
  houseOnly: "&e%giver%&r has taken &e%points%&r from %hc%%house%&r!"
  playerNoReason: "&e%giver%&r has taken &e%points%&r from &e%player%&r in %hc%%house%&r!"
  playerWithReason: "&e%giver%&r has taken &e%points%&r from &e%player%&r in %hc%%house%&r for %reason%!"
  reasonOnly: "&e%giver%&r has taken &e%points%&r from %hc%%house%&r for %reason%!"
  
# This list can be as long as you like, add this in the instance that another language community would like to change the word for
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

# Color of the message, this will turn into an enum value, but using spaces is okay, the plugin translated this to LIGHT_PURPLE
MessageColor: light purple

# The title on top of /points
StandingsTitle: "   [Current House Standings]"

# If you would like a title before the message broadcast for house points.
UseTitle: true

# Title and color. Does not currently support colors, support may be added later.
Title:
  title: [House Points]
  color: dark purple

# Do not touch this, this records the location of signs.  When you delete a sign, it will also remove from this list, generally though leave this alone.
Locations:
```
