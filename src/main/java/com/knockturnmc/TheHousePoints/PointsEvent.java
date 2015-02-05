package com.knockturnmc.TheHousePoints;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PointsEvent extends Event implements Cancellable{
	
	private static HandlerList handlers = new HandlerList();
	private House house;
	
	public PointsEvent(House house){
		this.house = house;
	}
	
	public House getHouse(){
		return house;
	}

	public boolean isCancelled() {
		return false;
	}

	public void setCancelled(boolean arg0) {
		
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
