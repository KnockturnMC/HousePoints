package net.pandette.housepoints.di;

import dagger.Component;
import net.pandette.housepoints.config.Configuration;
import net.pandette.housepoints.commands.HousePointsCommand;
import net.pandette.housepoints.config.DefaultHousePointsModifier;
import net.pandette.housepoints.listeners.PointsListener;
import net.pandette.housepoints.managers.HouseManager;
import net.pandette.housepoints.managers.SignManager;

import javax.inject.Singleton;

@Singleton
@Component(modules = SingleModule.class)
public interface SingleComponent {

    Configuration getConfiguration();

    HousePointsCommand getHousePointsCommand();

    HouseManager getHouseManager();

    SignManager getSignManager();

    PointsListener getPointsListener();

    DefaultHousePointsModifier getDefaultModifier();

}
