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
package net.pandette.housepoints;

import lombok.Getter;
import lombok.Setter;
import net.pandette.housepoints.config.DefaultLanguageHook;
import net.pandette.housepoints.config.DefaultPointsPlayerData;
import net.pandette.housepoints.config.LanguageHook;
import net.pandette.housepoints.config.PointsPlayerData;
import net.pandette.housepoints.di.DaggerSingleComponent;
import net.pandette.housepoints.di.SingleComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PointsPlugin extends JavaPlugin {

    private SingleComponent component;

    @Getter
    @Setter
    private LanguageHook languageHook;

    @Getter
    @Setter
    private PointsPlayerData pointsPlayerData;

    @Getter
    @Setter
    private static PointsPlugin instance;

    @Getter
    private NamespacedKey namespacedKey;

    @Override
    public void onEnable() {
        component = DaggerSingleComponent.builder().build();
        namespacedKey = new NamespacedKey(this, "housepoints-stands");;


        languageHook = new DefaultLanguageHook(component.getConfiguration());
        pointsPlayerData = new DefaultPointsPlayerData();

        setInstance(this);

        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!getDataFolder().exists() || !configFile.exists()) {
            saveDefaultConfig();
        }

        component.getHouseManager().load();
        component.getSignManager().load();

        getCommand("points").setExecutor(component.getHousePointsCommand());
        Bukkit.getPluginManager().registerEvents(component.getPointsListener(), this);
    }

    @Override
    public void onDisable() {
        component.getHouseManager().save();
        component.getSignManager().save();
    }

}
