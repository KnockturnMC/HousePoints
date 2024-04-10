package net.pandette.housepoints.config;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface PointsPlayerData {

    /**
     * Allows customizing the name of the House points player.
     * @return A String representation of a player's name.
     */
    Component getName(Player player);

}
