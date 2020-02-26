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
package net.pandette.housepoints.commands;

import net.pandette.housepoints.PointsPlugin;
import net.pandette.housepoints.config.Configuration;
import net.pandette.housepoints.config.LanguageHook;
import net.pandette.housepoints.config.Permission;
import net.pandette.housepoints.config.PointRepresentation;
import net.pandette.housepoints.dtos.House;
import net.pandette.housepoints.events.HousePointsEvent;
import net.pandette.housepoints.managers.HouseManager;
import net.pandette.housepoints.managers.SignManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

@Singleton
public class HousePointsCommand implements CommandExecutor {

    private static final String BREAK = ": ";
    private static final String DEFAULT_NO_PERMISSION = "&cYou do not have permission to perform this command!";
    private static final String DEFAULT_SYNTAX = "&7Correct House Points Syntax is /points [+/-] [house] [points] (player) (reason)";
    private static final String DEFAULT_NOT_A_HOUSE = "&cThat doesn't appear to be a house nor a shortcut for a house!";
    private static final String DEFAULT_EVENT_CANCELLED = "&cYour event got cancelled by another plugin!";

    private final Configuration configuration;
    private final HouseManager houseManager;
    private final Permission permission;
    private final SignManager signManager;

    @Inject
    public HousePointsCommand(Configuration configuration, HouseManager houseManager, Permission permission,
                              SignManager signManager) {
        this.configuration = configuration;
        this.houseManager = houseManager;
        this.permission = permission;
        this.signManager = signManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        LanguageHook languageHook = PointsPlugin.getInstance().getLanguageHook();
        Player senderPlayer = null;
        if (sender instanceof Player) {
            senderPlayer = (Player) sender;
        }

        if (args.length == 0) {
            return sendHouseStandings(sender, languageHook, senderPlayer);
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            return reloadData(sender, languageHook, senderPlayer);
        }

        if (args.length < 3) {
            sender.sendMessage(languageHook.getMessage("command.syntax", senderPlayer, DEFAULT_SYNTAX));
            return false;
        }

        final String name;
        if (sender instanceof Player) {
            name = PointsPlugin.getInstance().getPointsPlayerData().getName(((Player) sender));
        } else name = PointsPlugin.getInstance().getConfig().getString("consoleSender");

        House house = null;
        for (House h : houseManager.getHouses()) {
            if (args[1].equalsIgnoreCase(h.getName()) || args[1].equalsIgnoreCase(h.getShortcut())) {
                house = h;
                break;
            }
        }

        if (house == null) {
            sender.sendMessage(languageHook.getMessage("command.not_house", senderPlayer, DEFAULT_NOT_A_HOUSE));
            return false;
        }

        final int points;
        try {
            points = Integer.parseInt(args[2]);
        } catch (Exception e) {
            sender.sendMessage(languageHook.getMessage("command.syntax", senderPlayer, DEFAULT_SYNTAX));
            return false;
        }

        final String path;
        final String playerName;

        if (args.length > 3) {
            final Player player = Bukkit.getPlayer(args[3]);
            if (player == null) {
                playerName = null;
            } else {
                playerName = PointsPlugin.getInstance().getPointsPlayerData().getName(player);
            }
        } else {
            playerName = null;
        }

        final HousePointsEvent event;
        if (configuration.getPositive().contains(args[0].toLowerCase())) {
            if (!sender.hasPermission(permission.getGive()) && !sender.hasPermission(permission.getGive() + "." + house.getName().toUpperCase())) {
                sender.sendMessage(languageHook.getMessage("permission.no_permission_command", senderPlayer,
                        DEFAULT_NO_PERMISSION));
                return false;
            }

            event = new HousePointsEvent(house, points, name, playerName);
            if (validateEvent(sender, event, languageHook, senderPlayer)) return false;
            path = "give";
        } else if (configuration.getNegative().contains(args[0].toLowerCase())) {
            if (!sender.hasPermission(permission.getTake()) && !sender.hasPermission(permission.getTake() + "." + house.getName().toUpperCase())) {
                sender.sendMessage(languageHook.getMessage("permission.no_permission_command", senderPlayer,
                        DEFAULT_NO_PERMISSION));
                return false;
            }

            event = new HousePointsEvent(house, -points, name, playerName);
            if (validateEvent(sender, event, languageHook, senderPlayer)) return false;
            path = "take";
        } else {
            sender.sendMessage(languageHook.getMessage("command.syntax", senderPlayer, DEFAULT_SYNTAX));
            return false;
        }


        String finalMessage = getMessage(args, languageHook, senderPlayer, path, playerName, event);

        languageHook.broadCastMessage(finalMessage);

        for (Location location : signManager.getLocations()) {
            changeHouseSign(house, location);
        }

        return true;
    }

    private String getMessage(String[] args, LanguageHook languageHook, Player senderPlayer, String path, String playerName, HousePointsEvent event) {
        String message;
        String reason = "";
        if (args.length == 3) {
            message = languageHook.getMessage(path + ".houseOnly", senderPlayer,
                    "&e{giver}&r : {hc}{house}&r - &e{points}&r!");
        } else if (args.length == 4 && playerName != null) {
            message = languageHook.getMessage(path + ".playerNoReason", senderPlayer,
                    "&e{giver}&r : &e{player}&r {hc}{house}&r - &e{points}&r!");
        } else if (args.length == 4) {
            message = languageHook.getMessage(path + ".reasonOnly", senderPlayer,
                    "&e{giver}&r : {hc}{house}&r - &e{points}&r for {reason}!");
        } else if (playerName != null) {
            message = languageHook.getMessage(path + ".playerWithReason", senderPlayer,
                    "&e{giver}&r : &e{player}&r {hc}{house}&r - &e{points}&r for {reason)!");
            String[] sub = Arrays.copyOfRange(args, 4, args.length + 1);
            reason = StringUtils.join(sub, " ");
        } else {
            message = languageHook.getMessage(path + ".reasonOnly", senderPlayer,
                    "&e{giver}&r : {hc}{house}&r - &e{points}&r for {reason}!");
            String[] sub = Arrays.copyOfRange(args, 3, args.length + 1);
            reason = StringUtils.join(sub, " ");
        }

        return formatMessage(message, event, reason);
    }

    private boolean reloadData(CommandSender sender, LanguageHook languageHook, Player senderPlayer) {
        if (!sender.hasPermission(permission.getReload())) {
            sender.sendMessage(languageHook.getMessage("permission.no_permission_command", senderPlayer,
                    DEFAULT_NO_PERMISSION));
            return false;
        }

        houseManager.reload();
        signManager.reload();
        return true;
    }

    private boolean sendHouseStandings(CommandSender sender, LanguageHook languageHook, Player senderPlayer) {
        if (!sender.hasPermission(permission.getSee())) {
            sender.sendMessage(languageHook.getMessage("permission.no_permission_command", senderPlayer,
                    DEFAULT_NO_PERMISSION));
            return false;
        }

        sender.sendMessage(configuration.getStandingsTitle());
        for (House house : houseManager.getHouses()) {
            sender.sendMessage(house.getChatColor() + house.getName() + configuration.getTitleColor()
                    + BREAK + house.getPoints());
        }
        return true;
    }

    private boolean validateEvent(CommandSender sender, HousePointsEvent event, LanguageHook languageHook, Player senderPlayer) {
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            sender.sendMessage(languageHook.getMessage("command.event_cancelled", senderPlayer, DEFAULT_EVENT_CANCELLED));
            return true;
        }
        return false;
    }

    private static String formatMessage(String message, HousePointsEvent event, String reason) {
        String pName = event.getReceiver();
        if (pName == null) pName = "";
        return message
                .replace("{player}", pName)
                .replace("{points}", String.valueOf(Math.abs(event.getPoints())))
                .replace("{reason}", reason)
                .replace("{house}", event.getHouse().getName())
                .replace("{hc}", event.getHouse().getChatColor() + "")
                .replace("{giver}", event.getGiver());
    }

    private void changeHouseSign(House house, Location loc) {
        if (!Tag.WALL_SIGNS.isTagged(loc.getBlock().getType())) {
            signManager.removeLocation(loc);
            return;
        }

        Chunk chunk = loc.getChunk();
        if (!chunk.isLoaded()) {
            chunk.load();
        }

        Sign sign = (Sign) loc.getBlock().getState();

        if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(house.getName())) {
            sign.setLine(0, house.getChatColor() + house.getName());
            sign.setLine(2, String.valueOf(house.getPoints()));
            sign.update();
        }

        List<House> positions = houseManager.getHousePositions();

        if (!configuration.isShowingPointsRepresentation()) return;

        House houseType = null;
        for (House h : houseManager.getHouses()) {
            if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(h.getName())) continue;
            houseType = h;
            break;
        }
        if (houseType == null) return;

        Block block = loc.getBlock();
        WallSign s = (WallSign) block.getState().getBlockData();
        BlockFace facing = s.getFacing();
        Block connected = block.getRelative(facing.getOppositeFace());
        int position = positions.indexOf(houseType);

        PointRepresentation representation = configuration.getRepresentationType();

        if (representation == PointRepresentation.BLOCK) {

            for (int i = 1; i < positions.size() + 1; i++) {
                setBlock(connected, i);
            }

            if (house.getPoints() == 0 && configuration.isShowingNoPoints()) return;

            for (int i = 1; i < positions.size() + 1 - position; i++) {
                setBlock(connected, houseType, i);
            }
            return;
        }
        ItemStack stack = new ItemStack(configuration.getCustomItemMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        Location above = block.getLocation().clone();
        above.setY(above.getY() + 1);
        above.setX(above.getX() + .5);
        above.setZ(above.getZ() + .5);
        ArmorStand e = above.getWorld().spawn(above, ArmorStand.class);
        PersistentDataContainer container = e.getPersistentDataContainer();
        container.set(PointsPlugin.getInstance().getNamespacedKey(), PersistentDataType.BYTE, (byte) 0);

        if (representation == PointRepresentation.ITEM_RENAME) {
            String name = house.getCustomItemRename();
            if (house.getPoints() == 0 && configuration.isShowingNoPoints()) {
                name = configuration.getCustomNoPointsRename();
            } else {
                name = name.replace("{rank}", String.valueOf(position));
            }
            meta.setDisplayName(name);
        } else if (representation == PointRepresentation.ITEM_NBT) {
            if (house.getPoints() == 0 && configuration.isShowingNoPoints()) {
                meta.setCustomModelData(configuration.getCustomItemNoPointsID());
            } else {
                meta.setCustomModelData(house.getCustomItemID() + (position - 1));
            }
        }

        stack.setItemMeta(meta);
        e.setHelmet(stack);
        e.setInvulnerable(true);
        e.setVisible(false);
    }


    private void setBlock(Block connected, int i) {
        Location location = connected.getLocation();
        location.setY(connected.getLocation().getY() + i);
        location.getBlock().setType(Material.GLASS);
    }

    private void setBlock(Block connected, House house, int i) {
        Location location = connected.getLocation();
        location.setY(connected.getLocation().getY() + i);
        location.getBlock().setType(house.getMaterial());
    }
}
