package net.pandette.housepoints.config;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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

        return configuration.getLanguageMessage(messageId, translate);
    }

    @Override
    public void broadCastMessage(Component message) {
        Bukkit.broadcast(message);
    }

}
