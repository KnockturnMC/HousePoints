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
package net.pandette.housepoints.config;

import lombok.Value;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Value
public class Permission {

    String housePoints = "PointsPlugin.";
    String see = housePoints + "see";
    String give = housePoints + "give";
    String take = housePoints + "take";
    String sign = housePoints + "create.sign";
    String deleteSign = housePoints + "delete.sign";
    String reload = housePoints + "reload";

    @Inject
    Permission(){}

}
