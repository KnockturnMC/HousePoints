package net.pandette.housepoints.config;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface LanguageHook {

    /**
     * Gets a message that is going to be sent to a player, and allows a representation to be used instead for the
     * same message. This can allow outside plugins adding customized versions of specific messages / other languages
     * @param messageId the id of the message that is desired.
     */
    String getMessage(String messageId, Player player, String defaultMessage);

    /**
     * Performs a broadcast message which can be adapted in other plugins.
     * @param message Message to broadcast
     */
    void broadCastMessage(Component message);

}
