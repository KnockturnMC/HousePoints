package net.pandette.housepoints.config;

public interface HousePointsModifier {

    /**
     * Add points to the house
     * @param house House
     * @param points Points to add
     */
    void addPoints(String house, int points);

    /**
     * Remove points from a house
     * @param house House
     * @param points Points to earn
     */
    void removePoints(String house, int points);

    /**
     * Get points from a house
     * @param house House to get the points for
     */
    int getPoints(String house);
}
