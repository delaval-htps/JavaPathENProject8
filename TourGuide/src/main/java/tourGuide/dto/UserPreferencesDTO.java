package tourGuide.dto;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

/**
 * DTO to represent the preferences of a user
 */
public class UserPreferencesDTO {

    @PositiveOrZero(message = "the distance must be a positive positive")
    @Digits(integer = 4, fraction = 0, message = "the distance must be a positive integer strictly inferior to 10000 miles")
    private Integer attractionProximity;

    @NotBlank(message = "the currencyUnit must be not null or blank")
    private String currencyUnit;

    @PositiveOrZero(message = "the lowerPricePoint must be a positive positive")
    @Digits(integer = 4, fraction = 0, message = "the lowerPricePoint must be a positive integer strictly inferior to 10000 USD")
    private Integer lowerPricePoint;

    @PositiveOrZero(message = "the highPricePoint must be a positive positive")
    @Digits(integer = 4, fraction = 0, message = "the highPricePoint must be a positive integer strictly inferior to 10000 USD")
    private Integer highPricePoint;

    @PositiveOrZero(message = "the tripDuration must be a positive positive")
    @Digits(integer = 4, fraction = 0, message = "the tripDuration must be a positive integer strictly inferior to 10000 nights")
    private Integer tripDuration;

    @PositiveOrZero(message = "the tripDuration must be a positive positive")
    @Digits(integer = 4, fraction = 0, message = "the tripDuration must be a positive integer strictly inferior to 10000 tickets")
    private Integer ticketQuantity = 1;

    @PositiveOrZero(message = "the numberOfAdults must be a positive positive")
    @Digits(integer = 4, fraction = 0, message = "the numberOfAdults must be a positive integer strictly inferior to 10000 adults")
    private Integer numberOfAdults;

    @PositiveOrZero(message = "the numberOfChildren must be a positive positive")
    @Digits(integer = 4, fraction = 0, message = "the numberOfChildren must be a positive integer strictly inferior to 10000 children")
    private Integer numberOfChildren;

    /**
     * All Arguments contructor
     * 
     * @param attractionProximity
     * @param currencyUnit
     * @param lowerPricePoint
     * @param highPricePoint
     * @param tripDuration
     * @param ticketQuantity
     * @param numberOfAdults
     * @param numberOfChildren
     */
    public UserPreferencesDTO(int attractionProximity, String currencyUnit, int lowerPricePoint, int highPricePoint, int tripDuration, int ticketQuantity, int numberOfAdults, int numberOfChildren) {

        this.attractionProximity = attractionProximity;
        this.currencyUnit = currencyUnit;
        this.lowerPricePoint = lowerPricePoint;
        this.highPricePoint = highPricePoint;
        this.tripDuration = tripDuration;
        this.ticketQuantity = ticketQuantity;
        this.numberOfAdults = numberOfAdults;
        this.numberOfChildren = numberOfChildren;
    }

    /**
     * No Argument constructor
     */
    public UserPreferencesDTO() {
    }

    /**
     * @return the attractionProximity
     */
    public int getAttractionProximity() {
        return attractionProximity;
    }

    /**
     * @param attractionProximity the attractionProximity to set
     */
    public void setAttractionProximity(int attractionProximity) {
        this.attractionProximity = attractionProximity;
    }

    /**
     * @return the currencyUnit
     */
    public String getCurrencyUnit() {
        return currencyUnit;
    }

    /**
     * @param currencyUnit the currencyUnit to set
     */
    public void setCurrencyUnit(String currencyUnit) {
        this.currencyUnit = currencyUnit;
    }

    /**
     * @return the lowerPricePoint
     */
    public int getLowerPricePoint() {
        return lowerPricePoint;
    }

    /**
     * @param lowerPricePoint the lowerPricePoint to set
     */
    public void setLowerPricePoint(int lowerPricePoint) {
        this.lowerPricePoint = lowerPricePoint;
    }

    /**
     * @return the highPricePoint
     */
    public int getHighPricePoint() {
        return highPricePoint;
    }

    /**
     * @param highPricePoint the highPricePoint to set
     */
    public void setHighPricePoint(int highPricePoint) {
        this.highPricePoint = highPricePoint;
    }

    /**
     * @return the tripDuration
     */
    public int getTripDuration() {
        return tripDuration;
    }

    /**
     * @param tripDuration the tripDuration to set
     */
    public void setTripDuration(int tripDuration) {
        this.tripDuration = tripDuration;
    }

    /**
     * @return the ticketQuantity
     */
    public int getTicketQuantity() {
        return ticketQuantity;
    }

    /**
     * @param ticketQuantity the ticketQuantity to set
     */
    public void setTicketQuantity(int ticketQuantity) {
        this.ticketQuantity = ticketQuantity;
    }

    /**
     * @return the numberOfAdults
     */
    public int getNumberOfAdults() {
        return numberOfAdults;
    }

    /**
     * @param numberOfAdults the numberOfAdults to set
     */
    public void setNumberOfAdults(int numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    /**
     * @return the numberOfChildren
     */
    public int getNumberOfChildren() {
        return numberOfChildren;
    }

    /**
     * @param numberOfChildren the numberOfChildren to set
     */
    public void setNumberOfChildren(int numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }

    @Override
    public String toString() {
        return "UserPreferencesDTO [attractionProximity=" + attractionProximity + ", currencyUnit=" + currencyUnit + ", lowerPricePoint=" + lowerPricePoint + ", highPricePoint=" + highPricePoint + ", tripDuration=" + tripDuration
                + ", ticketQuantity=" + ticketQuantity + ", numberOfAdults=" + numberOfAdults + ", numberOfChildren=" + numberOfChildren + "]";
    }

}