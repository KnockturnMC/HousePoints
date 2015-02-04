package com.knockturnmc.TheHousePoints;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HousePoints extends JavaPlugin{
	
	FileConfiguration config;
	private HashMap<House, Integer> points = new HashMap<House, Integer>();
	public HashMap<House, Integer> getPoints() {
		return points;
	}

	public HashMap<UUID, Location> getSigns() {
		return signs;
	}

	private HashMap<UUID, Location> signs = new HashMap<UUID, Location>();
	
	public void onEnable(){
		this.config = this.getConfig();
		points.put(House.GRYFFINDOR, config.getInt(House.GRYFFINDOR.getName()));
		points.put(House.RAVENCLAW, config.getInt(House.RAVENCLAW.getName()));
		points.put(House.HUFFLEPUFF, config.getInt(House.HUFFLEPUFF.getName()));
		points.put(House.SLYTHERIN, config.getInt(House.SLYTHERIN.getName()));
		
		ConfigurationSection locations = config.getConfigurationSection("Locations");
		for(String path : locations.getKeys(false)){
			signs.put(UUID.fromString(path),new Location(
					Bukkit.getWorld(locations.getString(path + ".world")),
					locations.getDouble(path + ".x"),
					locations.getDouble(path + ".y"),
					locations.getDouble(path + ".z")));
		}
	}
	
	public void onDisable(){
		for(Entry<House, Integer> e : points.entrySet()){
			config.set(e.getKey().getName(), e.getValue());
		}
		
		for(Entry<UUID, Location> e : signs.entrySet()){
			config.set("Locations." + e.getKey().toString() + ".world", e.getValue().getWorld().getName());
			config.set("Locations." + e.getKey().toString() + ".x", e.getValue().getX());
			config.set("Locations." + e.getKey().toString() + ".y", e.getValue().getY());
			config.set("Locations." + e.getKey().toString() + ".z", e.getValue().getZ());
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return true;}
	
	public String getMessage(boolean isPositive, boolean hasPlayer, boolean hasReason, String playername, 
			String reason, House house, Player giver, int amount){
		
		ChatColor positive = isPositive? ChatColor.GREEN : ChatColor.RED;
		ChatColor messageColor = ChatColor.valueOf(config.getString("MessageColor").toUpperCase().replace(" ", "_"));
		
		String result = null;
		String conjunction = null;
		if(isPositive){
			result = " given ";
			conjunction = " to ";
		}
		else{
			result = " taken ";
			conjunction = " from ";
		}
		
		String end = "";
		if(hasPlayer && hasReason){
			end = " for " + messageColor + playername + positive +  " for " + reason;
		}
		else if(hasReason && !hasPlayer){
			end = " for " + messageColor + positive + reason;
		}
		else if(!hasReason && hasPlayer && isPositive){
			end = " for " + messageColor + playername + positive + "'s triumphs";
		}
		else if(!hasReason && hasPlayer && !isPositive){
			end = " for " + messageColor + playername + positive + "'s wrongdoings";
		}
		
		String beginning = "";
		if(config.getBoolean("UseTitle")){
			beginning = ChatColor.valueOf(config.getString("Title.color").toUpperCase().replace(" ", "_")) +
					config.getString("Title.title") + " ";
		}
		
		
		String middle = messageColor + giver.getDisplayName() + " has" + result + amount + 
				conjunction + house.color() +  house.getName();
	
		return beginning + middle + end;
	}
	
	

}
