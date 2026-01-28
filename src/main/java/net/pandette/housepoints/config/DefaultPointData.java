package net.pandette.housepoints.config;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.pandette.housepoints.PointsPlugin;
import net.pandette.housepoints.dtos.House;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DefaultPointData implements PointData {

    @Override
    public Map<House, Integer> getHouseRank(Collection<House> houses) {
        HousePointsModifier modifier = PointsPlugin.getInstance().getHousePointsModifier();
        record HouseToPoints(
            House house,
            int points
        ) {

        }

        final List<HouseToPoints> houseToPoints = new ObjectArrayList<>();
        for (final House house : houses) {
            houseToPoints.add(new HouseToPoints(house, modifier.getPoints(house.getName().toUpperCase(Locale.ROOT))));
        }

        houseToPoints.sort(Comparator.comparingInt(HouseToPoints::points));

        Map<House, Integer> rankMap = new HashMap<>();

        for (int i = 0; i < houseToPoints.size(); i++) {
            rankMap.put(houseToPoints.get(i).house(), i);
        }
        return rankMap;
    }
}
