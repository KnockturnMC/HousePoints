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
package net.pandette.housepoints.listeners;

import net.pandette.housepoints.PointsPlugin;
import net.pandette.housepoints.config.Configuration;
import net.pandette.housepoints.config.LanguageHook;
import net.pandette.housepoints.config.Permission;
import net.pandette.housepoints.dtos.House;
import net.pandette.housepoints.events.HousePointsEvent;
import net.pandette.housepoints.managers.HouseManager;
import net.pandette.housepoints.managers.SignManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PointsListener implements Listener {

    private final Permission permission;
    private final HouseManager houseManager;
    private final SignManager signManager;
    private final Configuration configuration;

    @Inject
    PointsListener(Permission permission, HouseManager houseManager, SignManager signManager,
                   Configuration configuration) {
        this.permission = permission;
        this.houseManager = houseManager;
        this.signManager = signManager;
        this.configuration = configuration;
    }

    private static final String DEFAULT_WALL_SIGN = "&cOnly wall signs are supported.";
    private static final String DEFAULT_NO_PERMISSION_ACTION = "&cYou do not have permission to do that!";


    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignChangeEvent(SignChangeEvent event) {
        Player player = event.getPlayer();
        LanguageHook languageHook = PointsPlugin.getInstance().getLanguageHook();

        if (!player.hasPermission(permission.getSign())) return;

        Sign sign = (Sign) event.getBlock().getState();

        if (!Tag.WALL_SIGNS.isTagged(event.getBlock().getType())) {
            event.getPlayer().sendMessage(languageHook.getMessage("listener.wallsign", player,
                    DEFAULT_WALL_SIGN));
            return;
        }

        Location loc = event.getBlock().getLocation();

        for (House house : houseManager.getHouses()) {
            if (ChatColor.stripColor(event.getLine(0)).equalsIgnoreCase("[" + house.getName() + "]")) {
                sign.setLine(0, house.getChatColor() + house.getName());
                sign.setLine(2, String.valueOf(house.getPoints()));
                sign.update();
                event.setCancelled(true);

                signManager.addLocation(loc);
                return;
            }
        }
    }


    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        LanguageHook languageHook = PointsPlugin.getInstance().getLanguageHook();

        if (!signManager.getLocations().contains(event.getBlock().getLocation())) return;

        if (player.hasPermission(permission.getDeleteSign())) {
            signManager.removeLocation(event.getBlock().getLocation());

            return;
        }

        player.sendMessage(languageHook.getMessage("permission.no_permission_action", player,
                DEFAULT_NO_PERMISSION_ACTION));
        event.setCancelled(true);
    }

    /**
     * This is here, because if someone else calls a house points event it will automatically increment in the plugin's
     * representation as well.
     * @param event Event
     */
    @EventHandler
    public void onHouseEventIncrement(HousePointsEvent event){
        House house = event.getHouse();
        house.setPoints(house.getPoints() + event.getPoints());
    }
}
