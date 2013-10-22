package com.wxd.bean;

public class Position {
    
    private String naem;
    
    private double swlng;
    
    private double swlat;
    
    private double nelng;
    
    private double nelat;
    
    public Position(String naem, double swlng, double swlat, double nelng, double nelat) {
        super();
        this.naem = naem;
        this.swlng = swlng;
        this.swlat = swlat;
        this.nelng = nelng;
        this.nelat = nelat;
    }
    
    public String getNaem() {
        return naem;
    }
    
    public void setNaem(String naem) {
        this.naem = naem;
    }
    
    public double getSwlng() {
        return swlng;
    }
    
    public void setSwlng(double swlng) {
        this.swlng = swlng;
    }
    
    public double getSwlat() {
        return swlat;
    }
    
    public void setSwlat(double swlat) {
        this.swlat = swlat;
    }
    
    public double getNelng() {
        return nelng;
    }
    
    public void setNelng(double nelng) {
        this.nelng = nelng;
    }
    
    public double getNelat() {
        return nelat;
    }
    
    public void setNelat(double nelat) {
        this.nelat = nelat;
    }
    
}
