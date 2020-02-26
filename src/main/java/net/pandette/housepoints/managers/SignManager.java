package net.pandette.housepoints.managers;

import net.pandette.housepoints.PointsPlugin;
import net.pandette.housepoints.config.Configuration;
import net.pandette.housepoints.config.PointRepresentation;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SignManager {

    private final List<Location> locationList;
    private final Configuration configuration;

    @Inject
    public SignManager(Configuration configuration) {
        locationList = new ArrayList<>();
        this.configuration = configuration;
    }

    public List<Location> getLocations() {
        return new ArrayList<>(locationList);
    }

    public void removeLocation(Location location) {
        locationList.remove(location);
        if (configuration.getRepresentationType() == PointRepresentation.ITEM_RENAME ||
                configuration.getRepresentationType() == PointRepresentation.ITEM_NBT) {
            Location above = location.clone();
            above.setY(above.getY() + 1);
            for (Entity e : above.getWorld().getEntitiesByClass(ArmorStand.class)) {
                if (!e.getLocation().getBlock().getLocation().equals(above)) continue;
                PersistentDataContainer container = e.getPersistentDataContainer();
                boolean exists = container
                        .has(PointsPlugin.getInstance().getNamespacedKey(), PersistentDataType.BYTE);
                if (!exists) continue;
                e.remove();
            }
        }
    }

    public void addLocation(Location location) {
        locationList.add(location);
    }

    public void load() {
        configuration.loadKeys("Locations", (c, s) -> locationList.add(configuration.getLocation(c, s)));
    }

    public void reload() {
        locationList.clear();
        load();
    }

    public void save() {
        configuration.saveLocations(locationList);
    }
}
