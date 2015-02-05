package com.knockturnmc.TheHousePoints;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PointsEvent extends Event implements Cancellable{
	
	private static HandlerList handlers = new HandlerList();
	private House house;
	private int points;
	private boolean isPositive;
	
	public PointsEvent(House house, int points, boolean isPositive){
		this.house = house;
		this.points = points;
		this.isPositive = isPositive;
	}
	
	public House getHouse(){
		return house;
	}
	
	public int getPoints(){
		return points;
	}
	
	public boolean getPositive(){
		return isPositive;
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
