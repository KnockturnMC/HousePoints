package net.pandette.housepoints;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

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
public class PointsListener implements Listener {

    public PointsListener() {
        Bukkit.getPluginManager().registerEvents(this, HousePoints.getInstance());
    }

    private static final String WALL_SIGN = ChatColor.RED + "Only wall signs are supported.";
    private static final String NOT_A_HOUSE = ChatColor.RED + "That is not a supported house!";


    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignChangeEvent(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission(Permission.SIGN)) {
            player.sendMessage(Permission.NO_PERMISSION_COMMAND);
            return;
        }

        for (House house : HousePoints.getHouses()) {
            if (ChatColor.stripColor(event.getLine(0)).equalsIgnoreCase("[" + house.getName() + "]")) {
                createHouseSign(event, house.getName());
            }
        }
    }


    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if(!HousePoints.getSignLocations().contains(event.getBlock().getLocation())) return;

        if (player.hasPermission(Permission.DELETE_SIGN)) {
            HousePoints.getSignLocations().remove(event.getBlock().getLocation());
            return;
        }

        player.sendMessage(Permission.NO_PERMISSION_ACTION);
        event.setCancelled(true);
    }

    private void createHouseSign(SignChangeEvent event, String houseName) {
        Sign sign = (Sign) event.getBlock().getState();

        if (sign.getType() == Material.SIGN_POST) {
            event.getPlayer().sendMessage(WALL_SIGN);
            event.setCancelled(true);
            return;
        }

        Location loc = event.getBlock().getLocation();

        House house = null;
        for (House h : HousePoints.getHouses()) {
            if (h.getName().equalsIgnoreCase(houseName)) {
                house = h;
                break;
            }
        }

        if (house == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(NOT_A_HOUSE);
            return;
        }

        sign.setLine(0, house.getChatColor() + house.getName());
        sign.setLine(2, String.valueOf(house.getPoints()));
        sign.update();
        event.setCancelled(true);
        HousePoints.getSignLocations().add(loc);
    }


}
