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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.pandette.housepoints.PointsPlugin;
import net.pandette.housepoints.config.HousePointsModifier;
import net.pandette.housepoints.config.LanguageHook;
import net.pandette.housepoints.config.Permission;
import net.pandette.housepoints.dtos.House;
import net.pandette.housepoints.events.HousePointsEvent;
import net.pandette.housepoints.managers.HouseManager;
import net.pandette.housepoints.managers.SignManager;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PointsListener implements Listener {

    private final Permission permission;
    private final HouseManager houseManager;
    private final SignManager signManager;

    @Inject
    PointsListener(Permission permission, HouseManager houseManager, SignManager signManager) {
        this.permission = permission;
        this.houseManager = houseManager;
        this.signManager = signManager;
    }

    private static final String DEFAULT_WALL_SIGN = "&cOnly wall signs are supported.";
    private static final String DEFAULT_NO_PERMISSION_ACTION = "&cYou do not have permission to do that!";


    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignChangeEvent(SignChangeEvent event) {
        Player player = event.getPlayer();
        LanguageHook languageHook = PointsPlugin.getInstance().getLanguageHook();

        if (!player.hasPermission(permission.getSign())) return;

        Sign sign = (Sign) event.getBlock().getState();
        Component line = event.line(0);
        if (line == null) return;

        @NotNull String linePlainText = LegacyComponentSerializer.legacyAmpersand().serialize(line);

        Location loc = sign.getLocation();
        HousePointsModifier modifier = PointsPlugin.getInstance().getHousePointsModifier();
        for (House house : houseManager.getHouses()) {
            if (linePlainText.equalsIgnoreCase("[" + house.getName() + "]")) {
                if (!Tag.WALL_SIGNS.isTagged(event.getBlock().getType())) {
                    event.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(languageHook.getMessage("listener.wallsign", player,
                            DEFAULT_WALL_SIGN)));
                    return;
                }

                event.line(0, Component.text(house.getName(), house.getTextColor()));
                event.line(2, Component.text(modifier.getPoints(house.getName().toUpperCase())));
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

        player.sendMessage(MiniMessage.miniMessage().deserialize(languageHook.getMessage("permission.no_permission_action", player,
                DEFAULT_NO_PERMISSION_ACTION)));
        event.setCancelled(true);
    }

    /**
     * This is here, because if someone else calls a house points event it will automatically increment in the plugin's
     * representation as well.
     *
     * @param event Event
     */
    @EventHandler
    public void onHouseEventIncrement(HousePointsEvent event) {
        HousePointsModifier modifier = PointsPlugin.getInstance().getHousePointsModifier();
        House house = event.getHouse();
        modifier.addPoints(house.getName().toUpperCase(), event.getPoints());
    }
}
