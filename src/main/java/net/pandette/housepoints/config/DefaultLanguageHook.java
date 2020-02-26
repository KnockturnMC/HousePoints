package net.pandette.housepoints.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DefaultLanguageHook implements LanguageHook {

    private final Configuration configuration;

    public DefaultLanguageHook(Configuration configuration){
        this.configuration = configuration;
    }


    @Override
    public String getMessage(String messageId, Player player, String defaultMessage) {
        String translate = defaultMessage;
        if (translate == null) {
            translate = "";
        }

        return ChatColor.translateAlternateColorCodes('&', configuration.getLanguageMessage(messageId, translate));
    }

    @Override
    public void broadCastMessage(String message) {
        Bukkit.broadcastMessage(message);
    }

}
