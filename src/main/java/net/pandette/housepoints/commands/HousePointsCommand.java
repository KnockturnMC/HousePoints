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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public HousePointsCommand(final Configuration configuration, final HouseManager houseManager, final Permission permission,
                              final SignManager signManager) {
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
    public boolean onCommand(final CommandSender sender, final Command command, final String s, final String[] args) {

        if (configuration.isAsync()) {
            Bukkit.getScheduler().runTaskAsynchronously(PointsPlugin.getInstance(), () -> runCommand(sender, args));
            return true;
        }

        return runCommand(sender, args);
    }

    private boolean runCommand(final CommandSender sender, final String[] args) {
        final LanguageHook languageHook = PointsPlugin.getInstance().getLanguageHook();
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
            sender.sendMessage(MiniMessage.miniMessage().deserialize(languageHook.getMessage("command.syntax", senderPlayer, DEFAULT_SYNTAX)));
            return false;
        }

        final Component name;
        if (sender instanceof Player) {
            name = PointsPlugin.getInstance().getPointsPlayerData().getName(((Player) sender));
        } else name = Component.text(PointsPlugin.getInstance().getConfig().getString("consoleSender"));

        House house = null;
        for (final House h : houseManager.getHouses()) {
            if (args[1].equalsIgnoreCase(h.getName()) || args[1].equalsIgnoreCase(h.getShortcut())) {
                house = h;
                break;
            }
        }

        if (house == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(languageHook.getMessage("command.not_house", senderPlayer, DEFAULT_NOT_A_HOUSE)));
            return false;
        }

        final int points;
        try {
            points = Integer.parseInt(args[2]);
        } catch (final Exception e) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(languageHook.getMessage("command.syntax", senderPlayer, DEFAULT_SYNTAX)));
            return false;
        }

        final String path;
        final @Nullable Component receiverName;
        final @Nullable UUID receiverUUID;

        final Player receiverPlayerInstance = args.length > 3 ? Bukkit.getPlayer(args[3]) : null;
        if (receiverPlayerInstance == null) {
            receiverName = null;
            receiverUUID = null;
        } else {
            receiverName = PointsPlugin.getInstance().getPointsPlayerData().getName(receiverPlayerInstance);
            receiverUUID = receiverPlayerInstance.getUniqueId();
        }

        final HousePointsEvent event;
        if (configuration.getPositive().contains(args[0].toLowerCase())) {
            if (!sender.hasPermission(permission.getGive()) && !sender.hasPermission(permission.getGive() + "." + house.getName().toUpperCase())) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(languageHook.getMessage("permission.no_permission_command", senderPlayer,
                    DEFAULT_NO_PERMISSION)));
                return false;
            }

            event = new HousePointsEvent(house, points, name, receiverName, receiverUUID);
            if (validateEvent(sender, event, languageHook, senderPlayer)) return false;
            path = "give";
        } else if (configuration.getNegative().contains(args[0].toLowerCase())) {
            if (!sender.hasPermission(permission.getTake()) && !sender.hasPermission(permission.getTake() + "." + house.getName().toUpperCase())) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(languageHook.getMessage("permission.no_permission_command", senderPlayer,
                    DEFAULT_NO_PERMISSION)));
                return false;
            }

            event = new HousePointsEvent(house, -points, name, receiverName, receiverUUID);
            if (validateEvent(sender, event, languageHook, senderPlayer)) return false;
            path = "take";
        } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(languageHook.getMessage("command.syntax", senderPlayer, DEFAULT_SYNTAX)));
            return false;
        }


        final String finalMessage = getMessage(args, languageHook, senderPlayer, path, receiverName, event);
        @NotNull final Component component = MiniMessage.miniMessage().deserialize(finalMessage);

        languageHook.broadCastMessage(component);

        final House finalHouse = house;
        Bukkit.getScheduler().runTask(PointsPlugin.getInstance(), () -> {
            for (final Location location : signManager.getLocations()) {
                changeHouseSign(finalHouse, location);
            }
        });

        return true;
    }

    /*
    This gets the message based on the command and prepares it to be broadcast.
     */
    private String getMessage(final String[] args, final LanguageHook languageHook, final Player senderPlayer, final String path, final Component playerName, final HousePointsEvent event) {
        final String message;
        String reason = "";
        if (args.length == 3) {
            message = languageHook.getMessage(path + ".houseOnly", senderPlayer,
                "<yellow>{giver}<white> : {hc}{house}<white> - <yellow>{points}<white>!");
        } else if (args.length == 4 && playerName != null) {
            message = languageHook.getMessage(path + ".playerNoReason", senderPlayer,
                "<yellow>{giver}<white> : <yellow>{player}<white> {hc}{house}<white> - <yellow>{points}<white>!");
        } else if (args.length == 4) {
            message = languageHook.getMessage(path + ".reasonOnly", senderPlayer,
                "<yellow>{giver}<white> : {hc}{house}<white> - <yellow>{points}<white> for {reason}!");
        } else if (playerName != null) {
            message = languageHook.getMessage(path + ".playerWithReason", senderPlayer,
                "<yellow>{giver}<white> : <yellow>{player}<white> {hc}{house}<white> - <yellow>{points}<white> for {reason}!");
            final String[] sub = Arrays.copyOfRange(args, 4, args.length + 1);
            reason = StringUtils.join(sub, " ");
        } else {
            message = languageHook.getMessage(path + ".reasonOnly", senderPlayer,
                "<yellow>{giver}<white> : {hc}{house}<white> - <yellow>{points}<white> for {reason}!");
            final String[] sub = Arrays.copyOfRange(args, 3, args.length + 1);
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
    private boolean reloadData(final CommandSender sender, final LanguageHook languageHook, final Player senderPlayer) {
        if (!sender.hasPermission(permission.getReload())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(languageHook.getMessage("permission.no_permission_command", senderPlayer,
                DEFAULT_NO_PERMISSION)));
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
    private boolean sendHouseStandings(final CommandSender sender, final LanguageHook languageHook, final Player senderPlayer) {
        if (!sender.hasPermission(permission.getSee())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(languageHook.getMessage("permission.no_permission_command", senderPlayer,
                DEFAULT_NO_PERMISSION)));
            return false;
        }

        sender.sendMessage(configuration.getStandingsTitle());
        for (final House house : houseManager.getHouses()) {
            final HousePointsModifier modifier = PointsPlugin.getInstance().getHousePointsModifier();
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<" + house.getTextColor().asHexString() + ">"
                + house.getName() + "<" + configuration.getTitleColor().asHexString() + ">"
                + BREAK + modifier.getPoints(house.getName().toUpperCase())));
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
    private boolean validateEvent(final CommandSender sender, final HousePointsEvent event, final LanguageHook languageHook, final Player senderPlayer) {
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(languageHook.getMessage("command.event_cancelled", senderPlayer, DEFAULT_EVENT_CANCELLED)));
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
    private static String formatMessage(final String message, final HousePointsEvent event, final String reason) {
        Component pName = event.getReceiver();
        if (pName == null) pName = Component.text("");

        return message
            .replace("{player}", MiniMessage.miniMessage().serialize(pName))
            .replace("{points}", String.valueOf(Math.abs(event.getPoints())))
            .replace("{reason}", reason)
            .replace("{house}", event.getHouse().getName())
            .replace("{hc}", "<" + event.getHouse().getTextColor() + ">")
            .replace("{giver}", MiniMessage.miniMessage().serialize(event.getGiver()));
    }

    /**
     * This changes the house points sign. This will load the chunk first so the changes go through for blocks.
     *
     * @param house House to change for.
     * @param loc   Location to change
     */
    private void changeHouseSign(final House house, final Location loc) {
        if (!Tag.WALL_SIGNS.isTagged(loc.getBlock().getType())) {
            PointsPlugin.getInstance().getComponentLogger().warn(
                "Removing sign at {} from runtime as no sign can be found there", loc
            );
            signManager.removeLocation(loc);
            return;
        }

        final int cx = loc.getBlockX() / 16;
        final int cz = loc.getBlockZ() / 16;
        loc.getWorld().addPluginChunkTicket(cx, cz, PointsPlugin.getInstance());

        final HousePointsModifier modifier = PointsPlugin.getInstance().getHousePointsModifier();
        final Sign sign = (Sign) loc.getBlock().getState();
        @NotNull final SignSide side = sign.getSide(Side.FRONT);
        final String plainTextHouse = ((TextComponent) side.line(0)).content();

        if (plainTextHouse.equalsIgnoreCase(house.getName())) {
            side.line(0, Component.text(house.getName(), house.getTextColor()));
            side.line(2, Component.text(modifier.getPoints(house.getName().toUpperCase())));
            sign.update();
        }

        final Map<House, Integer> positions = PointsPlugin.getInstance().getPointData()
            .getHouseRank(houseManager.getHouses());

        if (!configuration.isShowingPointsRepresentation()) return;

        final House houseType = houseManager.getHouse(plainTextHouse);
        if (houseType == null) return;

        final Block block = loc.getBlock();
        final WallSign s = (WallSign) block.getState().getBlockData();
        final BlockFace facing = s.getFacing();
        final Block connected = block.getRelative(facing.getOppositeFace());
        final int position = positions.get(houseType);

        final PointRepresentation representation = configuration.getRepresentationType();

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

    private void setupArmorStand(final Map<House, Integer> positions, final Block block, final BlockFace facing, final PointRepresentation representation, final House h) {
        final Location above = block.getLocation().clone();
        above.setY(above.getY() + 1 + configuration.getCustomItemY());
        above.setX(above.getX() + configuration.getCustomItemX());
        above.setZ(above.getZ() + configuration.getCustomItemZ());
        above.setDirection(facing.getDirection());
        above.setPitch(0);
        signManager.removeVisualizingEntity(above);

        final int hposition = positions.get(h);

        setupEntity(representation, h, above, hposition);

        if (representation == PointRepresentation.ITEM_NBT_V2) {
            final double maxYSize = configuration.getCustomItemScaleY() * configuration.getItemNBTV2FractionForInnerY();
            final double maxPosition = 10;
            final double ySizeMul = Math.clamp(hposition, 0.1, maxPosition) * (maxYSize / maxPosition);
            final double xzScaleFactor = configuration.getItemNBTV2FractionForXZ();
            above.getWorld().spawn(above, BlockDisplay.class, b -> {
                b.getPersistentDataContainer().set(PointsPlugin.getInstance().getNamespacedKey(), PersistentDataType.BYTE, (byte) 0);
                b.setBlock(h.getMaterial().createBlockData());
                final Transformation transformation = b.getTransformation();
                transformation.getScale().set(
                    configuration.getCustomItemScaleX() * xzScaleFactor,
                    ySizeMul,
                    configuration.getCustomItemScaleZ() * xzScaleFactor
                );
                transformation.getTranslation().set(
                    -configuration.getCustomItemScaleX() * xzScaleFactor / 2,
                    -configuration.getCustomItemScaleY() * -configuration.getItemNBTV2FractionForInnerYOffset(),
                    -configuration.getCustomItemScaleZ() * xzScaleFactor / 2
                );
                b.setTransformation(transformation);
            });
        }
    }

    private Entity setupEntity(final PointRepresentation representation, final House h, final Location above, final int hposition) {
        final ItemStack stack = new ItemStack(configuration.getCustomItemMaterial());

        final ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;

        final Entity e;
        if (representation == PointRepresentation.ITEM_NBT_V2) {
            e = above.getWorld().spawn(above, ItemDisplay.class, a -> {
                final Transformation transformation = a.getTransformation();
                transformation.getScale().set(
                    configuration.getCustomItemScaleX(),
                    configuration.getCustomItemScaleY(),
                    configuration.getCustomItemScaleZ()
                );
                a.setTransformation(transformation);
            });
        } else {
            e = above.getWorld().spawn(above, ArmorStand.class, a -> {
                a.setInvulnerable(true);
                a.setVisible(false);
                a.setGravity(false);
            });
        }

        final PersistentDataContainer container = e.getPersistentDataContainer();
        container.set(PointsPlugin.getInstance().getNamespacedKey(), PersistentDataType.BYTE, (byte) 0);
        final HousePointsModifier modifier = PointsPlugin.getInstance().getHousePointsModifier();
        if (representation == PointRepresentation.ITEM_RENAME) {
            String name = h.getCustomItemRename();
            if (modifier.getPoints(h.getName().toUpperCase()) == 0 && configuration.isShowingNoPoints()) {
                name = configuration.getCustomNoPointsRename();
            } else {
                name = name.replace("{rank}", String.valueOf(hposition + 1));
            }

            meta.displayName(Component.text(name));
        } else if (representation == PointRepresentation.ITEM_NBT) {
            if (modifier.getPoints(h.getName().toUpperCase()) == 0 && configuration.isShowingNoPoints()) {
                meta.setCustomModelData(configuration.getCustomItemNoPointsID());
            } else {
                meta.setCustomModelData(h.getCustomItemID() + hposition);
            }
        } else if (representation == PointRepresentation.ITEM_NBT_V2) {
            final CustomModelDataComponent customModelDataComponent = meta.getCustomModelDataComponent();
            customModelDataComponent.setColors(List.of(Color.fromRGB(h.getTextColor().value())));
            customModelDataComponent.setFloats(List.of((float) hposition));
            meta.setCustomModelDataComponent(customModelDataComponent);
        }

        final String customItemItemModel = configuration.getCustomItemItemModel();
        if (customItemItemModel != null && !customItemItemModel.isEmpty()) {
            meta.setItemModel(NamespacedKey.fromString(customItemItemModel));
        }

        stack.setItemMeta(meta);
        if (e instanceof final ArmorStand armorStand) {
            armorStand.getEquipment().setHelmet(stack);
        } else {
            final ItemDisplay itemDisplay = (ItemDisplay) e;
            itemDisplay.setItemStack(stack);
        }

        return e;
    }

    /**
     * This sets the block with glass if we have a block change type.
     *
     * @param connected The location its connected to.
     * @param i         - number to go up
     */
    private void setBlock(final Block connected, final int i) {
        final Location location = connected.getLocation();
        location.setY(connected.getLocation().getY() + i);
        location.getBlock().setType(Material.GLASS);
    }

    private void setBlock(final Block connected, final House house, final int i) {
        final Location location = connected.getLocation();
        location.setY(connected.getLocation().getY() + i);
        if (!house.getMaterial().isBlock()) {
            throw new IllegalArgumentException("The material provided for the house is only for when using " +
                "PointType: BLOCK & the material provided is not a block. Please fix this or swap to " +
                "PointType: ITEM_RENAME or ITEM_NBT, and set custom-item under custom-item.material.");
        }
        location.getBlock().setType(house.getMaterial());
    }
}
