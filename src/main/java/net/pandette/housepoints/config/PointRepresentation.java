package net.pandette.housepoints.config;

public enum PointRepresentation {
    ITEM_RENAME, ITEM_NBT, BLOCK, NONE;

    public static PointRepresentation getRepresentation(String config) {
        try {
            return PointRepresentation.valueOf(config.toUpperCase());
        } catch (Exception e) {
            return PointRepresentation.NONE;
        }
    }
}
