package com.knockturnmc.TheHousePoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class PointsListener implements Listener {
	
	HousePoints housepoints;
	
	PointsListener(HousePoints housepoints){
		this.housepoints = housepoints;
		housepoints.getServer().getPluginManager().registerEvents(this, housepoints);
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
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
	
	@EventHandler (priority = EventPriority.NORMAL)
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
		House house = House.valueOf(houseName.toUpperCase());
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
		
		Sign sign = (Sign) loc.getBlock().getState();
		House house = event.getHouse();
		
		if(ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase(house.getName())){
			sign.setLine(0, house.color() + house.getName());
			sign.setLine(2, String.valueOf(housepoints.getPoints().get(house)));
			sign.update();
		
			Block block = loc.getBlock();
			org.bukkit.material.Sign s = (org.bukkit.material.Sign) block.getState().getData();
			Block connected = block.getRelative(s.getAttachedFace());
			
			Material material = house.material();
			int position = getHousePosition(house);
		
			switch(position){
		
			case 0:
				for(int i = 1; i < 5; i++){
				setBlock(connected, material, i);
				}
				break;
			case 1:
				for(int i = 1; i < 4; i++){
				setBlock(connected, material, i);
				}
				break;
			case 2:
				for(int i = 1; i < 3; i++){
				setBlock(connected, material, i);
				}
				break;
			case 3:
				for(int i = 1; i < 2; i++){
				setBlock(connected, material, i);
				}
				break;
			}
			event.setCancelled(true);
		}
		
	}

	public void setBlock(Block connected, Material material, int i) {
		Location location = connected.getLocation();
		location.setY(connected.getLocation().getY() + i);
		location.getBlock().setType(material);
	}
	
	@SuppressWarnings("rawtypes")
	public int getHousePosition(House house){
		HashMap<House, Integer> points = housepoints.getPoints();
		 
		List<Map.Entry<House, Integer>> entries = new ArrayList<Map.Entry<House, Integer>>(
				points.entrySet());
		Collections.sort(entries, new Comparator<Entry>() {
			public int compare(Entry a, Entry b) {
				Integer A = (Integer) a.getValue();
				Integer B = (Integer) b.getValue();
				if (A > B) {
					return -1;
				} else if (A == B) {
					return 0;
				} else {
					return 1;
				}
			}
		});
		
		int position = 0;
		for(int i = 0; i < entries.size(); i++){
			if(entries.get(i).getKey() == house){
				position = i;
			}
		}
		
		return position;
	}

}
