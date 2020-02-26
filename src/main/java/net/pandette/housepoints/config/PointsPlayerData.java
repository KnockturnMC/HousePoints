package net.pandette.housepoints.config;

import org.bukkit.entity.Player;

public interface PointsPlayerData {

    /**
     * Allows customizing the name of the House points player.
     * @return A String representation of a player's name.
     */
    String getName(Player player);

}
