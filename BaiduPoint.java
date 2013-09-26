package com.test;

public class BaiduPoint {
    
    private double lng;
    
    private double lat;
    
    public BaiduPoint(double lng, double lat) {
        super();
        this.lng = lng;
        this.lat = lat;
    }
    
    public double getLng() {
        return lng;
    }
    
    public void setLng(double lng) {
        this.lng = lng;
    }
    
    public double getLat() {
        return lat;
    }
    
    public void setLat(double lat) {
        this.lat = lat;
    }
    
}