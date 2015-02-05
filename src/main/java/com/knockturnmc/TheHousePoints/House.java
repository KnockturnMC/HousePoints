package com.knockturnmc.TheHousePoints;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum House {
	
	SLYTHERIN("Slytherin", ChatColor.DARK_GREEN, Material.EMERALD), 
	GRYFFINDOR("Gryffindor", ChatColor.DARK_RED, Material.REDSTONE_BLOCK), 
	HUFFLEPUFF("Hufflepuff", ChatColor.GOLD, Material.GOLD_BLOCK), 
	RAVENCLAW("Ravenclaw", ChatColor.DARK_BLUE, Material.LAPIS_BLOCK);
	
	private String name;
	private ChatColor color;
	private Material material;
	
	House(String name, ChatColor color, Material material){
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
	
	public Material material(){
		return material;
	}
}
