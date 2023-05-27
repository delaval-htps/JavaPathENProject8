package tourGuide.dto;

import java.util.Date;

import gpsUtil.location.Location;

/**
 * Class to represente a visited location for a determinate user (ie without his
 * UUID just location and visited date)
 */
public class UserVisitedLocationDTO {
    private Date timeVisited;
    private Location location;

    /**
     * No Arguments constructor
     */
    public UserVisitedLocationDTO() {
    }

    /**
     * Arguments contructor
     * 
     * @param timeVisitedLocation
     * @param location
     */
    public UserVisitedLocationDTO(Date timeVisitedLocation, Location location) {
        this.timeVisited = timeVisitedLocation;
        this.location = location;
    }

    /**
     * @return the timeVisitedLocation
     */
    public Date getTimeVisited() {
        return timeVisited;
    }

    /**
     * @param timeVisitedLocation the timeVisitedLocation to set
     */
    public void setTimeVisited(Date timeVisitedLocation) {
        this.timeVisited = timeVisitedLocation;
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

}
