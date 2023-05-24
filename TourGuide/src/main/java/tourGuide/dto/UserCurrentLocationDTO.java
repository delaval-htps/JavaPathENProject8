package tourGuide.dto;

import java.util.UUID;

public class UserCurrentLocationDTO {
    private UUID userUuid;
    private double longitude;
    private double latitude;

    /**
     * NoArgumentConstructor
     */
    public UserCurrentLocationDTO() {
    }

    /**
     * AllArgumentConstructor
     * @param userUuid  the id of user
     * @param longitude the longitude of last visited location of user
     * @param latitude  the latitude of last visited location of user
     */
    public UserCurrentLocationDTO(UUID userUuid, double longitude, double latitude) {
        this.userUuid = userUuid;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * @return the userUuid
     */
    public UUID getUserUuid() {
        return userUuid;
    }

    /**
     * @param userUuid the userUuid to set
     */
    public void setUserUuid(UUID userUuid) {
        this.userUuid = userUuid;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

}
