package net.pandette.housepoints;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.material.Wool;

import java.util.ArrayList;
import java.util.Arrays;
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
public class HousePointsCommand implements CommandExecutor {

    private static final ChatColor CHAT_COLOR;
    private static final String TITLE;
    private static final String BREAK = ": ";
    private static final String SYNTAX = ChatColor.GRAY + "Correct House Points Syntax is /points [+/-] [house] [points] (player) (reason)";
    private static final String NOT_A_HOUSE = ChatColor.RED + "That doesn't appear to be a house nor a shortcut for a house!";
    private static final List<String> POSITIVE;
    private static final List<String> NEGATIVE;
    private static final String EVENT_CANCELLED = ChatColor.RED + "Your event got cancelled by another plugin!";

    static {
        FileConfiguration configuration = HousePoints.getInstance().getConfig();
        String color = configuration.getString("MessageColor", "LIGHT_PURPLE");
        CHAT_COLOR = ChatColor.valueOf(chatColorString(color));
        String title = configuration.getString("StandingsTitle", "   [Current House Standings]");
        TITLE = CHAT_COLOR + title;

        List<String> positive = configuration.getStringList("positive");
        List<String> negative = configuration.getStringList("negative");
        if (positive.isEmpty()) positive = Arrays.asList("add", "plus", "give", "+");
        if (negative.isEmpty()) negative = Arrays.asList("remove", "take", "subtract", "-");
        POSITIVE = positive;
        NEGATIVE = negative;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission(Permission.SEE)) {
                sender.sendMessage(Permission.NO_PERMISSION_COMMAND);
                return false;
            }

            sender.sendMessage(TITLE);
            for (House house : HousePoints.getHouses()) {
                sender.sendMessage(house.getChatColor() + house.getName() + CHAT_COLOR + BREAK + house.getPoints());
            }
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(SYNTAX);
            return false;
        }

        final String name;
        if (sender instanceof Player) {
            name = ((Player) sender).getDisplayName();
        } else name = HousePoints.getInstance().getConfig().getString("consoleSender");

        House house = null;
        for (House h : HousePoints.getHouses()) {
            if (args[1].equalsIgnoreCase(h.getName()) || args[1].equalsIgnoreCase(h.getShortcut())) {
                house = h;
                break;
            }
        }

        if (house == null) {
            sender.sendMessage(NOT_A_HOUSE);
            return false;
        }

        final int points;
        try {
            points = Integer.parseInt(args[2]);
        } catch (Exception e) {
            sender.sendMessage(SYNTAX);
            return false;
        }

        final String path;
        final String playerName;

        if (args.length > 3) {
            final Player player = Bukkit.getPlayer(args[3]);
            if (player == null) {
                playerName = null;
            } else {
                playerName = player.getDisplayName();
            }
        } else {
            playerName = null;
        }

        final HousePointsEvent event;
        if (POSITIVE.contains(args[0].toLowerCase())) {
            if (!sender.hasPermission(Permission.GIVE) && !sender.hasPermission(Permission.GIVE + "." + house.getName().toUpperCase())) {
                sender.sendMessage(Permission.NO_PERMISSION_COMMAND);
                return false;
            }

            event = new HousePointsEvent(house, points, name, playerName);
            if (validateEvent(sender, event)) return false;

            house.setPoints(house.getPoints() + points);
            path = "give";
        } else if (NEGATIVE.contains(args[0].toLowerCase())) {
            if (!sender.hasPermission(Permission.TAKE) && !sender.hasPermission(Permission.TAKE + "." + house.getName().toUpperCase())) {
                sender.sendMessage(Permission.NO_PERMISSION_COMMAND);
                return false;
            }

            event = new HousePointsEvent(house, -points, name, playerName);
            if (validateEvent(sender, event)) return false;

            Bukkit.getPluginManager().callEvent(event);
            house.setPoints(house.getPoints() - points);
            path = "take";
        } else {
            sender.sendMessage(SYNTAX);
            return false;
        }


        final FileConfiguration config = HousePoints.getInstance().getConfig();
        String message;
        String reason = "";
        if (args.length == 3) {
            message = config.getString(path + ".houseOnly");
        } else if (args.length == 4 && playerName != null) {
            message = config.getString(path + ".playerNoReason");
        } else if (args.length == 4) {
            message = config.getString(path + ".reasonOnly");
        } else if (playerName != null) {
            message = config.getString(path + ".playerWithReason");
            String[] sub = Arrays.copyOfRange(args, 4, args.length + 1);
            reason = StringUtils.join(sub, " ");
        } else {
            message = config.getString(path + ".reasonOnly");
            String[] sub = Arrays.copyOfRange(args, 3, args.length + 1);
            reason = StringUtils.join(sub, " ");
        }

        message = ChatColor.translateAlternateColorCodes('&', message);

        Bukkit.broadcastMessage(formatMessage(message, event, reason));

        for (Location location : HousePoints.getSignLocations()) {
            changeHouseSign(house, location);
        }

        return true;
    }

    private boolean validateEvent(CommandSender sender, HousePointsEvent event) {
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            sender.sendMessage(EVENT_CANCELLED);
            return true;
        }
        return false;
    }

    private static String formatMessage(String message, HousePointsEvent event, String reason) {
        String pName = event.getReceiver();
        if (pName == null) pName = "";
        return message
                .replace("%player%", pName)
                .replace("%points%", String.valueOf(Math.abs(event.getPoints())))
                .replace("%reason%", reason)
                .replace("%house%", event.getHouse().getName())
                .replace("%hc%", event.getHouse().getChatColor() + "")
                .replace("%giver%", event.getGiver());
    }


    private static String chatColorString(String color) {
        return color.toUpperCase().replace(" ", "_");
    }

    private void changeHouseSign(House house, Location loc) {
        Material signMaterial = loc.getBlock().getType();

        if (signMaterial != Material.WALL_SIGN) {
            HousePoints.getSignLocations().remove(loc);
            return;
        }

        Sign sign = (Sign) loc.getBlock().getState();

        if (ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(house.getName())) {
            sign.setLine(0, house.getChatColor() + house.getName());
            sign.setLine(2, String.valueOf(house.getPoints()));
            sign.update();
        }

        List<House> positions = getHousePositions();

        for (House h : HousePoints.getHouses()) {
            if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(h.getName())) continue;

            Block block = loc.getBlock();
            org.bukkit.material.Sign s = (org.bukkit.material.Sign) block.getState().getData();
            Block connected = block.getRelative(s.getAttachedFace());

            int position = positions.indexOf(h);
            for (int i = 1; i < positions.size() + 1; i++) {
                setBlock(connected, i);
            }

            for (int i = 1; i < positions.size() + 1 - position; i++) {
                setBlock(connected, h, i);
            }

            return;
        }
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
        if (house.getMaterial() == Material.WOOL) {
            ((Wool) location.getBlock().getState()).setColor(house.getColor());
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
