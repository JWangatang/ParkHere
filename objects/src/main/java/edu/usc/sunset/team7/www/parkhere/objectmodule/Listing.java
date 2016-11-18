package edu.usc.sunset.team7.www.parkhere.objectmodule;

import java.io.Serializable;

/**
 * Created by kunal on 10/12/16.
 */

public class Listing implements Serializable {

    private static final long serialVersionUID = 1L;

    private ParkingSpot parkingSpot;
    private String name;
    private String description;
    private long startTime;
    private long stopTime;
    private boolean refundable;
    private String listingID;
    private double price;
    private String providerID;
    //need to add functionality for can cancel policy
    //need to add picture functionality & list of pictures
    public Listing() {

    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return parkingSpot.getLatitude();
    }

    public double getLongitude() {
        return parkingSpot.getLongitude();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHandicap() {
        return parkingSpot.isHandicap();
    }

    public boolean isCovered() {
        return parkingSpot.isCovered();
    }

    public boolean isCompact() {
        return parkingSpot.isCompact();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStartTime(long startTime) { this.startTime = startTime; }

    public void setStopTime(long endTime) { this.stopTime = endTime; }

    public boolean isRefundable() {
        return refundable;
    }

    public void setRefundable(boolean refundable) {
        this.refundable = refundable;
    }

    public String getListingID() {
        return listingID;
    }

    public void setListingID(String listingID) {
        this.listingID = listingID;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageURL() {
        return parkingSpot.getImageURL();
    }

    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }
}
