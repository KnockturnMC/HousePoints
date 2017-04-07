package com.knockturnmc.TheHousePoints;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PointsEvent extends Event implements Cancellable{
	
	private static HandlerList handlers = new HandlerList();
	private final House house;
	private final int points;
	private final String giver;
	private final String receiver;
	
	public PointsEvent(House house, int points, String giver, String receiver){
		this.house = house;
		this.points = points;
		this.giver = giver;
		this.receiver = receiver;
	}
	
	public House getHouse(){
		return house;
	}

	public int getPoints() {
		return points;
	}

	public String getGiver() {
		return giver;
	}

	public String getReceiver() {
		return receiver;
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
