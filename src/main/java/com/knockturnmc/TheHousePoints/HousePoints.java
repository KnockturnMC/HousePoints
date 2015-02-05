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
import org.bukkit.event.Event;
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
		this.getLogger().info(config.getInt(House.GRYFFINDOR.getName()) + "");
		points.put(House.GRYFFINDOR, config.getInt(House.GRYFFINDOR.getName()));
		points.put(House.RAVENCLAW, config.getInt(House.RAVENCLAW.getName()));
		points.put(House.HUFFLEPUFF, config.getInt(House.HUFFLEPUFF.getName()));
		points.put(House.SLYTHERIN, config.getInt(House.SLYTHERIN.getName()));
		
		new PointsListener(this);
		
		ConfigurationSection locations = config.getConfigurationSection("Locations");
		if(locations != null){
		for(String path : locations.getKeys(false)){
			signs.put(UUID.fromString(path),new Location(
					Bukkit.getWorld(locations.getString(path + ".world")),
					locations.getDouble(path + ".x"),
					locations.getDouble(path + ".y"),
					locations.getDouble(path + ".z")));
		}}
		
		getLogger().info("House Points enabled");
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
		
		this.saveConfig();
		
		getLogger().info("House Points disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(!cmd.getName().equalsIgnoreCase("points")){
			return false;
		}
		if(args.length == 0){
			ChatColor messageColor = ChatColor.valueOf(config.getString("MessageColor").toUpperCase().replace(" ", "_"));
			sender.sendMessage(messageColor + "   [Current House Standings]");
			for(House house : House.values()){
				sender.sendMessage(house.color() + house.getName() + messageColor + ": " + points.get(house));
			}
			return false;
		}
		if(args.length > 0 && args.length < 3){
			sender.sendMessage(ChatColor.RED + "You didn't put in enough arguments for this command.");
			return false;
		}
		
		String name;
		if(sender instanceof Player){
			name = ((Player) sender).getDisplayName();
		}
		else{
			name = "The Gods Above";
		}
		
		House house = null;
		if(args[1].length() == 1){
			for(House h : House.values()){
				if(args[1].toUpperCase().charAt(0) == h.getName().charAt(0)){
					house = h;
				}
			}
		}
		else {
				house = House.valueOf(args[1].toUpperCase());
		}
		
		if(house == null){
			sender.sendMessage(ChatColor.RED + "That doesn't appear to be a house.");
			return false;
		}
		
		int pointChange = 0;
		try{ pointChange = Integer.parseInt(args[2]);}
		catch(Exception e){
			sender.sendMessage(ChatColor.RED + "That doesn't appear to be a number.");
			return false;
		}
		
		boolean isPositive = true;
		
		if(args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("+") || args[0].equalsIgnoreCase("add")){
			points.put(house, points.get(house) + pointChange);
			Event event = new PointsEvent(house, points.get(house), true);
			Bukkit.getPluginManager().callEvent(event);
			isPositive = true;
		}
		else if(args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("-") || args[0].equalsIgnoreCase("subtract")){
			points.put(house, points.get(house) - pointChange);
			Event event = new PointsEvent(house, points.get(house), false);
			Bukkit.getPluginManager().callEvent(event);
			isPositive = false;
		}
		
		if(args.length == 3){
			Bukkit.broadcastMessage(getMessage(isPositive, house, name, pointChange));
		}
		else{
			String playername = null;
			try{
				Player player = Bukkit.getPlayer(args[3]);
				playername = player.getPlayer().getDisplayName();
			}
			catch(Exception e){
				this.getLogger().info("House points attempted to find a player, but " + args[3] + " is not considered a player.");
			}
			
			if(args.length == 4 && playername != null){
				Bukkit.broadcastMessage(getMessage(isPositive, true, false, house, name, pointChange, playername, null));
			}
			else if(args.length == 4 && playername == null){
				Bukkit.broadcastMessage(getMessage(isPositive, false, true, house, name, pointChange, null, args[3]));
			}
			else if(args.length > 4 && playername != null){
				String reason = "";
				for (int i = 4; i < args.length; i++) {
					reason += args[i] + " ";
				}
				Bukkit.broadcastMessage(getMessage(isPositive, true, true, house, name, pointChange, playername, reason));
			}
			else{
				String reason = "";
				for (int i = 3; i < args.length; i++) {
					reason += args[i] + " ";
				}
				Bukkit.broadcastMessage(getMessage(isPositive, false, true, house, name, pointChange, null, reason));
			}
		}
		
		
		return true;}
	
	private String getMessage(boolean isPositive, House house, String giver, int amount){
		return getMessage(isPositive, false, false, house, giver, amount, null, null);
	}

	private String getMessage(boolean isPositive, boolean hasPlayer, boolean hasReason, House house, 
			String giver, int amount, String playername, String reason){
		
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
			end = positive + " for " + messageColor + playername + positive +  " for " + reason;
		}
		else if(hasReason && !hasPlayer){
			end = positive + " for " + messageColor + positive + reason;
		}
		else if(!hasReason && hasPlayer && isPositive){
			end = positive + " for " + messageColor + playername + positive + "'s triumphs";
		}
		else if(!hasReason && hasPlayer && !isPositive){
			end = positive + " for " + messageColor + playername + positive + "'s wrongdoings";
		}
		
		String beginning = "";
		if(config.getBoolean("UseTitle")){
			beginning = ChatColor.valueOf(config.getString("Title.color").toUpperCase().replace(" ", "_")) +
					config.getString("Title.title") + " ";
		}
		
		
		String middle = messageColor + giver + positive + " has" + result + amount + 
				conjunction + house.color() +  house.getName();
	
		return beginning + middle + end;
	}
	
	

}
