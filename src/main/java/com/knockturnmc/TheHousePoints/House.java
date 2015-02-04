package com.knockturnmc.TheHousePoints;

import org.bukkit.ChatColor;

public enum House {
	
	SLYTHERIN("Slytherin", ChatColor.DARK_GREEN), 
	GRYFFINDOR("Gryffindor", ChatColor.DARK_RED), 
	HUFFLEPUFF("Hufflepuff", ChatColor.GOLD), 
	RAVENCLAW("Ravenclaw", ChatColor.DARK_RED);
	
	private String name;
	private ChatColor color;
	
	House(String name, ChatColor color){
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}
	
	public ChatColor color(){
		return color;
	}
}
