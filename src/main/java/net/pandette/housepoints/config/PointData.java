package net.pandette.housepoints.config;

import net.pandette.housepoints.dtos.House;

import java.util.Collection;
import java.util.Map;

public interface PointData {

    /**
     * Returns a Map of the houses and their ranks.
     * @param houses Houses to rank
     * @return Map of house to Rank.
     */
    Map<House, Integer> getHouseRank(Collection<House> houses);
}
