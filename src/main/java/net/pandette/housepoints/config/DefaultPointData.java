package net.pandette.housepoints.config;

import net.pandette.housepoints.dtos.House;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultPointData implements PointData {

    @Override
    public Map<House, Integer> getHouseRank(Collection<House> houses) {
        List<House> houss = new ArrayList<>(houses);
        houss.sort((a, b) -> {
            int A = a.getPoints();
            int B = b.getPoints();
            return Integer.compare(B, A);
        });

        Map<House, Integer> rankMap = new HashMap<>();

        for (int i = 0; i < houss.size(); i++) {
            rankMap.put(houss.get(i), i);
        }
        return rankMap;
    }
}
