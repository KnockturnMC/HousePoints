package com.knockturnmc.TheHousePoints;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class PointsListener implements Listener {
	
	HousePoints housepoints;
	
	PointsListener(HousePoints housepoints){
		this.housepoints = housepoints;
	}
	
	public void onSignChangeEvent(SignChangeEvent event){
		Player player = event.getPlayer();
		
		if(!player.hasPermission("HousePoints.createSign")){
			return;
		}
		
		for(House house : House.values()){
		if(event.getLine(0).equalsIgnoreCase("[" + house.getName() + "]")){
			createHouseSign(event, house.getName());
			}
		}		
	}
	
	public void onPointsEvent(PointsEvent event){
		HashMap<UUID, Location> signs = housepoints.getSigns();
		for(Entry<UUID, Location> e : signs.entrySet()){
			changeHouseSign(event, e.getValue());
		}
	}
	
	public void createHouseSign(SignChangeEvent event, String houseName){
		Sign sign = (Sign) event.getBlock().getState();
		
		if(sign.getType() == Material.SIGN_POST){
			event.getPlayer().sendMessage(ChatColor.RED + "Only wall signs are supported.");
			event.setCancelled(true);
			return;
		}
		
		Location loc = event.getBlock().getLocation();
		HashMap<UUID, Location> signs = housepoints.getSigns();
		House house = House.valueOf(houseName);
		sign.setLine(0, house.color() + house.getName());
		sign.setLine(2, String.valueOf(housepoints.getPoints().get(house)));
		sign.update();
		event.setCancelled(true);
		signs.put(UUID.randomUUID(), loc);
	}
	
	public void changeHouseSign(PointsEvent event, Location loc){
		Material signMaterial = loc.getBlock().getType();
		
		if(signMaterial != Material.WALL_SIGN){
			return;
		}
		
		Sign sign = (Sign) loc.getBlock();
		House house = event.getHouse();
		
		if(ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(house.getName())){
			sign.setLine(0, house.color() + house.getName());
			sign.setLine(2, String.valueOf(housepoints.getPoints().get(house)));
			sign.update();
			event.setCancelled(true);
		}
	}

}
