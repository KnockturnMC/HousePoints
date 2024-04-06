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
package net.pandette.housepoints.dtos;

import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

@Value
public class House {

    @NonFinal
    @Setter
    int points;
    String name;
    Material material;
    TextColor textColor;
    String shortcut;
    String customItemRename;
    Integer customItemID;

}
