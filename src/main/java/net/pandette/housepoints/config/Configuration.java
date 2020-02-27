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
import net.pandette.housepoints.PointsPlugin;
import net.pandette.housepoints.dtos.House;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

@Value
@Singleton
public class Configuration {

    @Inject
    public Configuration() {
    }

    public void saveLocations(List<Location> locationsList) {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        config.set("Locations", null);
        ConfigurationSection locations = config.getConfigurationSection("Locations");
        if (locations == null) {
            config.createSection("Locations");
            locations = config.getConfigurationSection("Locations");
        }

        for (Location location : locationsList) {
            setLocation(location, locations, String.valueOf(locationsList.indexOf(location)));
        }
        PointsPlugin.getInstance().saveConfig();
    }

    public void saveHouses(Collection<House> houses) {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        ConfigurationSection houseSection = config.getConfigurationSection("Houses");
        for (House house : houses) {
            setHouse(house, houseSection, house.getName());
        }
        PointsPlugin.getInstance().saveConfig();
    }

    public void loadKeys(String section, BiConsumer<ConfigurationSection, String> consumer) {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        ConfigurationSection configSection = config.getConfigurationSection(section);
        if (configSection == null) return;
        configSection.getKeys(false).forEach(k -> consumer.accept(configSection, k));
    }

    public House getHouse(ConfigurationSection section, String path) {
        return new House(section.getInt(path + ".points"), path,
                Material.valueOf(getEnumString(section, path + ".material")),
                ChatColor.valueOf(getEnumString(section, path + ".chatColor")),
                section.getString(path + ".shortcut"),
                section.getString(path + ".custom-item.rename"),
                section.getInt(path + ".custom-item.id"));
    }

    private void setHouse(House house, ConfigurationSection section, String path) {
        section.set(path + ".points", house.getPoints());
    }

    private String getEnumString(ConfigurationSection section, String path) {
        return section.getString(path).toUpperCase();
    }


    public Location getLocation(ConfigurationSection section, String path) {
        return new Location(Bukkit.getWorld(section.getString(path + ".world")),
                section.getDouble(path + ".x"),
                section.getDouble(path + ".y"),
                section.getDouble(path + ".z"));
    }

    public boolean isShowingPointsRepresentation() {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        return config.getBoolean("pointRepresentation");
    }

    public boolean isShowingNoPoints() {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        return config.getBoolean("noPointsRepresentation");
    }

    public PointRepresentation getRepresentationType() {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        return PointRepresentation.getRepresentation(config.getString("pointType"));
    }

    public String getLanguageMessage(String messageId, String defaultMessage) {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages." + messageId, defaultMessage));
    }

    public Material getCustomItemMaterial() {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        String mat = config.getString("custom-item.material", "DIRT");
        return Material.getMaterial(mat.toUpperCase().replace(" ", "_"));
    }

    public String getCustomNoPointsRename() {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        return config.getString("custom-item.rename", "no-points");
    }

    public Integer getCustomItemNoPointsID() {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        return config.getInt("custom-item.id");
    }

    public Double getCustomItemX() {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        return config.getDouble("custom-item.x", .5);
    }
    public Double getCustomItemY() {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        return config.getDouble("custom-item.y", 0);
    }
    public Double getCustomItemZ() {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        return config.getDouble("custom-item.z", .5);
    }

    public ChatColor getTitleColor() {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        String color = config.getString("MessageColor", "LIGHT_PURPLE");
        return ChatColor.valueOf(chatColorString(color));
    }

    public String getStandingsTitle() {
        FileConfiguration config = PointsPlugin.getInstance().getConfig();
        String title = config.getString("StandingsTitle", "   [Current House Standings]");
        return getTitleColor() + title;
    }

    public List<String> getPositive() {
        FileConfiguration configuration = PointsPlugin.getInstance().getConfig();
        List<String> positive = configuration.getStringList("positive");
        if (positive.isEmpty()) positive = Arrays.asList("add", "plus", "give", "+");
        return positive;
    }

    public List<String> getNegative() {
        FileConfiguration configuration = PointsPlugin.getInstance().getConfig();
        List<String> negative = configuration.getStringList("negative");
        if (negative.isEmpty()) negative = Arrays.asList("remove", "take", "subtract", "-");
        return negative;
    }

    private void setLocation(Location location, ConfigurationSection section, String path) {
        section.set(path + ".world", location.getWorld().getName());
        section.set(path + ".x", location.getBlockX());
        section.set(path + ".y", location.getBlockY());
        section.set(path + ".z", location.getBlockZ());

    }

    private static String chatColorString(String color) {
        return color.toUpperCase().replace(" ", "_");
    }
}
