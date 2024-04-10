package net.pandette.housepoints.config;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class DefaultPointsPlayerData implements PointsPlayerData {

    public DefaultPointsPlayerData(){}

    @Override
    public Component getName(Player player) {
        return player.displayName();
    }

}
