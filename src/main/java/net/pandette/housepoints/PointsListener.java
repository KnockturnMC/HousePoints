package net.pandette.housepoints;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.ArrayList;
import java.util.List;

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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPointsEvent(HousePointsEvent event) {
        for (Location location : HousePoints.getSignLocations()) {
            changeHouseSign(event, location);
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

    private void changeHouseSign(HousePointsEvent event, Location loc) {
        Material signMaterial = loc.getBlock().getType();

        if (signMaterial != Material.WALL_SIGN) {
            HousePoints.getSignLocations().remove(loc);
            return;
        }

        Sign sign = (Sign) loc.getBlock().getState();
        House house = event.getHouse();

        if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(house.getName())) {
            sign.setLine(0, house.getChatColor() + house.getName());
            sign.setLine(2, String.valueOf(house.getPoints()));
            sign.update();
        }

        List<House> positions = getHousePositions();
        for (House h : HousePoints.getHouses()) {
            if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(h.getName())) {
                Block block = loc.getBlock();
                org.bukkit.material.Sign s = (org.bukkit.material.Sign) block.getState().getData();
                Block connected = block.getRelative(s.getAttachedFace());


                int position = positions.indexOf(h);
                for (int i = 1; i < HousePoints.getHouses().size(); i++) {
                    setBlock(connected, Material.GLASS, i);
                }


                for(int i = 1; i < positions.size() + 1 - position; i++) {
                    setBlock(connected, h.getMaterial(), i);
                }
            }
        }
        event.setCancelled(true);

    }

    private void setBlock(Block connected, Material material, int i) {
        Location location = connected.getLocation();
        location.setY(connected.getLocation().getY() + i);
        location.getBlock().setType(material);
    }

    private void setBlock(Block connected, House house, int i) {
        Location location = connected.getLocation();
        location.setY(connected.getLocation().getY() + i);
        location.getBlock().setType(house.getMaterial());
        if(house.getMaterial() == Material.WOOL) {
            location.getBlock().setData(house.getColor().getDyeData());
        }
    }

    private List<House> getHousePositions() {
        List<House> houses = new ArrayList<>(HousePoints.getHouses());
        houses.sort((a, b) -> {
            int A = a.getPoints();
            int B = b.getPoints();
            return Integer.compare(B, A);
        });

        return houses;
    }

}
