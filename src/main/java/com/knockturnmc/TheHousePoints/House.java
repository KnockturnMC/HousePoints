package com.knockturnmc.TheHousePoints;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

public enum House {
	
	SLYTHERIN("Slytherin", ChatColor.DARK_GREEN, DyeColor.GREEN), 
	GRYFFINDOR("Gryffindor", ChatColor.DARK_RED, DyeColor.RED), 
	HUFFLEPUFF("Hufflepuff", ChatColor.GOLD, DyeColor.YELLOW), 
	RAVENCLAW("Ravenclaw", ChatColor.DARK_BLUE, DyeColor.BLUE);
	
	private String name;
	private ChatColor color;
	private DyeColor material;
	
	House(String name, ChatColor color, DyeColor material){
		this.name = name;
		this.color = color;
		this.material = material;
	}

	public String getName() {
		return name;
	}
	
	public ChatColor color(){
		return color;
	}
	
	public DyeColor material(){
		return material;
	}
}
