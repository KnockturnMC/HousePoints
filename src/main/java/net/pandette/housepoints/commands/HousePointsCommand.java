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
import net.pandette.housepoints.config.HousePointsModifier;
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
import java.util.Map;

/**
 * This class runs the main commands hub allowing the points plugin to operate.
 */
@Singleton
public class HousePointsCommand implements CommandExecutor {

    /*
    Some default strings in case someone fumbles.
     */
    private static final String BREAK = ": ";
    private static final String DEFAULT_NO_PERMISSION = "&cYou do not have permission to perform this command!";
    private static final String DEFAULT_SYNTAX = "&7Correct House Points Syntax is /points [+/-] [house] [points] (player) (reason)";
    private static final String DEFAULT_NOT_A_HOUSE = "&cThat doesn't appear to be a house nor a shortcut for a house!";
    private static final String DEFAULT_EVENT_CANCELLED = "&cYour event got cancelled by another plugin!";

    /*
    Class dependencies
     */
    private final Configuration configuration;
    private final HouseManager houseManager;
    private final Permission permission;
    private final SignManager signManager;

    /*
    Use injection from Dagger to get dependencies.
     */
    @Inject
    public HousePointsCommand(Configuration configuration, HouseManager houseManager, Permission permission,
                              SignManager signManager) {
        this.configuration = configuration;
        this.houseManager = houseManager;
        this.permission = permission;
        this.signManager = signManager;
    }

    /**
     * Entry point for the command.
     *
     * @param sender  Sender
     * @param command Command
     * @param s       String
     * @param args    Args
     * @return Return if the command worked or not.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (configuration.isAsync()) {
            Bukkit.getScheduler().runTaskAsynchronously(PointsPlugin.getInstance(), () -> {
                runCommand(sender, args);
            });
            return true;
        }

        return runCommand(sender, args);
    }

    private boolean runCommand(CommandSender sender, String[] args) {
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

        House finalHouse = house;
        Bukkit.getScheduler().runTask(PointsPlugin.getInstance(), () -> {
            for (Location location : signManager.getLocations()) {
                changeHouseSign(finalHouse, location);
            }
        });

        return true;
    }

    /*
    This gets the message based on the command and prepares it to be broadcast.
     */
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
                    "&e{giver}&r : &e{player}&r {hc}{house}&r - &e{points}&r for {reason}!");
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

    /**
     * This method reloads data that can be reloaded.
     *
     * @param sender       Sender of the command
     * @param languageHook Language Hook to allow an external language adaptor.
     * @param senderPlayer Sender player to allow for customizing language based on player.
     * @return Returns whether the command was successful or not.
     */
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

    /**
     * Sends the house standings to the players.
     *
     * @param sender       Command sender
     * @param languageHook Language Hook to allow an external language adaptor.
     * @param senderPlayer Sender player to allow for customizing language based on player.
     * @return Returns whether the command was successful or not.
     */
    private boolean sendHouseStandings(CommandSender sender, LanguageHook languageHook, Player senderPlayer) {
        if (!sender.hasPermission(permission.getSee())) {
            sender.sendMessage(languageHook.getMessage("permission.no_permission_command", senderPlayer,
                    DEFAULT_NO_PERMISSION));
            return false;
        }

        sender.sendMessage(configuration.getStandingsTitle());
        for (House house : houseManager.getHouses()) {
            HousePointsModifier modifier = PointsPlugin.getInstance().getHousePointsModifier();
            sender.sendMessage(house.getChatColor() + house.getName() + configuration.getTitleColor()
                    + BREAK + modifier.getPoints(house.getName().toUpperCase()));
        }
        return true;
    }

    /**
     * This validates whether the event gets cancelled or not.
     *
     * @param sender       Command sender
     * @param event        House Points event
     * @param languageHook Language Hook to allow an external language adaptor.
     * @param senderPlayer Sender player to allow for customizing language based on player.
     * @return Whether the event went through or was cancelled.
     */
    private boolean validateEvent(CommandSender sender, HousePointsEvent event, LanguageHook languageHook, Player senderPlayer) {
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            sender.sendMessage(languageHook.getMessage("command.event_cancelled", senderPlayer, DEFAULT_EVENT_CANCELLED));
            return true;
        }
        return false;
    }

    /**
     * This formats the message in preparation for broadcast.
     *
     * @param message Message
     * @param event   House points event
     * @param reason  Reason for points change
     * @return Returns the message in a formatted form.
     */
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

    /**
     * This changes the house points sign. This will load the chunk first so the changes go through for blocks.
     *
     * @param house House to change for.
     * @param loc   Location to change
     */
    private void changeHouseSign(House house, Location loc) {
        if (!Tag.WALL_SIGNS.isTagged(loc.getBlock().getType())) {
            signManager.removeLocation(loc);
            return;
        }

        Chunk chunk = loc.getChunk();
        if (!chunk.isLoaded()) {
            chunk.load();
        }
        HousePointsModifier modifier = PointsPlugin.getInstance().getHousePointsModifier();
        Sign sign = (Sign) loc.getBlock().getState();

        if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(house.getName())) {
            sign.setLine(0, house.getChatColor() + house.getName());
            sign.setLine(2, String.valueOf(modifier.getPoints(house.getName().toUpperCase())));
            sign.update();
        }

        Map<House, Integer> positions = PointsPlugin.getInstance().getPointData()
                .getHouseRank(houseManager.getHouses());

        if (!configuration.isShowingPointsRepresentation()) return;

        House houseType = houseManager.getHouse(ChatColor.stripColor(sign.getLine(0)));
        if (houseType == null) return;

        Block block = loc.getBlock();
        WallSign s = (WallSign) block.getState().getBlockData();
        BlockFace facing = s.getFacing();
        Block connected = block.getRelative(facing.getOppositeFace());
        int position = positions.get(houseType);

        PointRepresentation representation = configuration.getRepresentationType();

        if (representation == PointRepresentation.BLOCK) {

            for (int i = 1; i < positions.size() + 1; i++) {
                setBlock(connected, i);
            }

            if (modifier.getPoints(house.getName().toUpperCase()) == 0 && configuration.isShowingNoPoints()) return;

            for (int i = 1; i < positions.size() + 1 - position; i++) {
                setBlock(connected, houseType, i);
            }
            return;
        }


        setupArmorStand(positions, block, facing, representation, houseType);

    }

    private void setupArmorStand(Map<House, Integer> positions, Block block, BlockFace facing, PointRepresentation representation, House h) {
        signManager.removeArmorstands(block.getLocation());
        Location above = block.getLocation().clone();
        above.setY(above.getY() + 1 + configuration.getCustomItemY());
        above.setX(above.getX() + configuration.getCustomItemX());
        above.setZ(above.getZ() + configuration.getCustomItemZ());
        above.setDirection(facing.getDirection());
        above.setPitch(0);

        int hposition = positions.get(h);
        ItemStack stack = new ItemStack(configuration.getCustomItemMaterial());

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        ArmorStand e = above.getWorld().spawn(above, ArmorStand.class);

        PersistentDataContainer container = e.getPersistentDataContainer();
        container.set(PointsPlugin.getInstance().getNamespacedKey(), PersistentDataType.BYTE, (byte) 0);
        HousePointsModifier modifier = PointsPlugin.getInstance().getHousePointsModifier();
        if (representation == PointRepresentation.ITEM_RENAME) {
            String name = h.getCustomItemRename();
            if (modifier.getPoints(h.getName().toUpperCase()) == 0 && configuration.isShowingNoPoints()) {
                name = configuration.getCustomNoPointsRename();
            } else {
                name = name.replace("{rank}", String.valueOf(hposition + 1));
            }
            meta.setDisplayName(name);
        } else if (representation == PointRepresentation.ITEM_NBT) {
            if (modifier.getPoints(h.getName().toUpperCase()) == 0 && configuration.isShowingNoPoints()) {
                meta.setCustomModelData(configuration.getCustomItemNoPointsID());
            } else {
                meta.setCustomModelData(h.getCustomItemID() + hposition);
            }
        }

        stack.setItemMeta(meta);
        e.setHelmet(stack);
        e.setInvulnerable(true);
        e.setVisible(false);
        e.setGravity(false);
    }


    /**
     * This sets the block with glass if we have a block change type.
     *
     * @param connected The location its connected to.
     * @param i         - number to go up
     */
    private void setBlock(Block connected, int i) {
        Location location = connected.getLocation();
        location.setY(connected.getLocation().getY() + i);
        location.getBlock().setType(Material.GLASS);
    }

    private void setBlock(Block connected, House house, int i) {
        Location location = connected.getLocation();
        location.setY(connected.getLocation().getY() + i);
        if (!house.getMaterial().isBlock()) {
            throw new IllegalArgumentException("The material provided for the house is only for when using " +
                    "PointType: BLOCK & the material provided is not a block. Please fix this or swap to " +
                    "PointType: ITEM_RENAME or ITEM_NBT, and set custom-item under custom-item.material.");
        }
        location.getBlock().setType(house.getMaterial());
    }
}
