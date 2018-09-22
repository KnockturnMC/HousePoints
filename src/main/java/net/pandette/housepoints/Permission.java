package net.pandette.housepoints;

import org.bukkit.ChatColor;

/*
    House points is a plugin for house points created for Minecraft
    Copyright (C) 2018 Kimberly Boynton

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

    To request information, make an issue on the github page for this
    plugin.
 */
class Permission {

    private static final String HOUSE_POINTS = "HousePoints.";
    static final String SEE = HOUSE_POINTS + "see";
    static final String GIVE = HOUSE_POINTS + "give";
    static final String TAKE = HOUSE_POINTS + "take";
    static final String SIGN = HOUSE_POINTS + "create.sign";
    static final String DELETE_SIGN = HOUSE_POINTS + "delete.sign";

    static final String NO_PERMISSION_COMMAND = ChatColor.RED + "You do not have permission to perform this command!";
    static final String NO_PERMISSION_ACTION = ChatColor.RED + "You do not have permission to do that!";

}
