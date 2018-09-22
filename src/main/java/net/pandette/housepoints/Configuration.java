package net.pandette.housepoints;

import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.function.BiConsumer;

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
@Value
public class Configuration {

    static void load() {
        loadLocations();
        loadHouses();
    }

    static void save() {
        saveHouses();
        saveLocations();
    }

    static void reload() {
        HousePoints.getHouses().clear();
        HousePoints.getSignLocations().clear();
        load();
    }

    private static void loadLocations() {
        loadKeys("Locations", (c, s) -> HousePoints.getSignLocations().add(getLocation(c, s)));
    }

    private static void saveLocations() {
        FileConfiguration config = HousePoints.getInstance().getConfig();
        config.set("Locations", null);
        ConfigurationSection locations = config.getConfigurationSection("Locations");
        if(locations == null) config.createSection("Locations");
        for (Location location : HousePoints.getSignLocations()) {
            setLocation(location, locations, String.valueOf(HousePoints.getSignLocations().indexOf(location)));
        }
        HousePoints.getInstance().saveConfig();
    }

    private static void loadHouses() {
        loadKeys("Houses", (c, s) -> HousePoints.getHouses().add(getHouse(c, s)));
    }

    private static void saveHouses() {
        FileConfiguration config = HousePoints.getInstance().getConfig();
        ConfigurationSection houseSection = config.getConfigurationSection("Houses");
        for (House house : HousePoints.getHouses()) {
            setHouse(house, houseSection, house.getName());
        }
        HousePoints.getInstance().saveConfig();
    }

    private static void loadKeys(String section, BiConsumer<ConfigurationSection, String> consumer) {
        FileConfiguration config = HousePoints.getInstance().getConfig();
        ConfigurationSection configSection = config.getConfigurationSection(section);
        if (configSection == null) return;
        configSection.getKeys(false).forEach(k -> consumer.accept(configSection, k));
    }

    private static House getHouse(ConfigurationSection section, String path) {
        return new House(path, section.getInt(path + ".points"),
                Material.valueOf(getEnumString(section, path + ".block.material")),
                DyeColor.valueOf(getEnumString(section, path + ".block.color")),
                ChatColor.valueOf(getEnumString(section, path + ".chatColor")),
                section.getString(path + ".shortcut"));
    }

    private static void setHouse(House house, ConfigurationSection section, String path) {
        section.set(path + ".points", house.getPoints());
    }

    private static String getEnumString(ConfigurationSection section, String path) {
        return section.getString(path).toUpperCase();
    }


    private static Location getLocation(ConfigurationSection section, String path) {
        return new Location(Bukkit.getWorld(section.getString(path + ".world")),
                section.getDouble(path + ".x"),
                section.getDouble(path + ".y"),
                section.getDouble(path + ".z"));
    }

    private static void setLocation(Location location, ConfigurationSection section, String path) {
        section.set(path + ".world", location.getWorld().getName());
        section.set(path + ".x", location.getBlockX());
        section.set(path + ".y", location.getBlockY());
        section.set(path + ".z", location.getBlockZ());

    }
}
