package org.hni.provider.om;

public class LocationQueryParams {
    
    private double customerLattitude;  
    private double customerLongitude;
    private double maxDistance;
    private Integer resultCount;
    public double getCustomerLattitude() {
        return customerLattitude;
    }
    public void setCustomerLattitude(double customerLattitude) {
        this.customerLattitude = customerLattitude;
    }
    public double getCustomerLongitude() {
        return customerLongitude;
    }
    public void setCustomerLongitude(double customerLongitude) {
        this.customerLongitude = customerLongitude;
    }
    public double getMaxDistance() {
        return maxDistance;
    }
    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }
    public Integer getResultCount() {
        return resultCount;
    }
    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }

    
}
