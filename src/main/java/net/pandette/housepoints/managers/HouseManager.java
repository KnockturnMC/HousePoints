package net.pandette.housepoints.managers;

import net.pandette.housepoints.config.Configuration;
import net.pandette.housepoints.dtos.House;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class HouseManager {

    private final Map<String, House> houseMap;
    private final Configuration configuration;

    @Inject
    public HouseManager(Configuration configuration) {
        houseMap = new HashMap<>();
        this.configuration = configuration;
    }

    public List<House> getHouses() {
        return new ArrayList<>(houseMap.values());
    }

    public List<House> getHousePositions() {
        List<House> houses = new ArrayList<>(houseMap.values());
        houses.sort((a, b) -> {
            int A = a.getPoints();
            int B = b.getPoints();
            return Integer.compare(B, A);
        });

        return houses;
    }

    public void load() {
        configuration.loadKeys("Houses", (c, s) -> houseMap.put(c.getName(), configuration.getHouse(c, s)));
    }

    public void reload() {
        houseMap.clear();
        load();
    }

    public void save() {
        configuration.saveHouses(houseMap.values());
    }


}
