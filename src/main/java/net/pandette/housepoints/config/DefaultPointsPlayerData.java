package net.pandette.housepoints.config;

import org.bukkit.entity.Player;

public class DefaultPointsPlayerData implements PointsPlayerData {

    public DefaultPointsPlayerData(){}

    @Override
    public String getName(Player player) {
        return player.getDisplayName();
    }

}
