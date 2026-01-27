package net.pandette.housepoints.managers;

import net.pandette.housepoints.PointsPlugin;
import net.pandette.housepoints.config.Configuration;
import net.pandette.housepoints.config.PointRepresentation;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Singleton
public class SignManager {

    private static final Set<PointRepresentation> ENTITY_BASED_REPRESENTATIONS = EnumSet.of(
        PointRepresentation.ITEM_RENAME, PointRepresentation.ITEM_NBT, PointRepresentation.ITEM_NBT_V2
    );

    private final List<Location> locationList;
    private final Configuration configuration;

    @Inject
    public SignManager(final Configuration configuration) {
        locationList = new ArrayList<>();
        this.configuration = configuration;
    }

    public List<Location> getLocations() {
        return new ArrayList<>(locationList);
    }

    public void removeLocation(final Location location) {
        locationList.remove(location);
        removeVisualizingEntity(location);
    }

    public void removeVisualizingEntity(final Location location) {
        if (!ENTITY_BASED_REPRESENTATIONS.contains(this.configuration.getRepresentationType())) return;

        final Location above = location.clone().getBlock().getLocation();

        for (final Entity e : above.getChunk().getEntities()) {
            if (!e.getLocation().getBlock().getLocation().equals(above)) continue;

            final PersistentDataContainer container = e.getPersistentDataContainer();
            if (container.has(PointsPlugin.getInstance().getNamespacedKey(), PersistentDataType.BYTE)) e.remove();
        }

    }

    public void addLocation(final Location location) {
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
