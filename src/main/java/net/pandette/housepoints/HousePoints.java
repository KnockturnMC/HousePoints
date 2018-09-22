package net.pandette.housepoints;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
public class HousePoints extends JavaPlugin {

    private static final List<Location> SIGN_LOCATIONS = new ArrayList<>();

    private static final List<House> HOUSES = new ArrayList<>();

    @Getter(value = AccessLevel.PACKAGE)
    @Setter(value = AccessLevel.PRIVATE)
    private static HousePoints instance;

    @Override
    public void onEnable(){
        setInstance(this);

        File configFile = new File(this.getDataFolder(), "config.yml");
        if(!getDataFolder().exists() || !configFile.exists()) {
            saveDefaultConfig();
        }

        new PointsListener();

        Configuration.load();
        getCommand("points").setExecutor(new HousePointsCommand());
    }

    @Override
    public void onDisable(){
        Configuration.save();
    }

    static List<Location> getSignLocations() {
        return SIGN_LOCATIONS;
    }

    static List<House> getHouses() {
        return HOUSES;
    }
}
