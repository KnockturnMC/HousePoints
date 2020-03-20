package net.pandette.housepoints.config;

import net.pandette.housepoints.dtos.House;
import net.pandette.housepoints.managers.HouseManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultHousePointsModifier implements HousePointsModifier {

    HouseManager manager;

    @Inject
    DefaultHousePointsModifier(HouseManager houseManager) {
        this.manager = houseManager;
    }

    @Override
    public void addPoints(String house, int points) {
        House hou = manager.getHouse(house);
        hou.setPoints(hou.getPoints() + points);
    }

    @Override
    public void removePoints(String house, int points) {
        House hou = manager.getHouse(house);
        hou.setPoints(hou.getPoints() - points);
    }

    @Override
    public int getPoints(String house) {
        House hou = manager.getHouse(house);
        return hou.getPoints();
    }
}
