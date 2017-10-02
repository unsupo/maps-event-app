package object;


public class Location{
    double[] coordinates = new double[]{0,0};
    String type = "circle", radius = "10m";

    public double getLat() {
        return this.coordinates[1];
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public void setLat(double lat) {
        this.coordinates[1] = lat;
    }

    public double getLon() {
        return this.coordinates[0];
    }

    public void setLon(double lon) {
        this.coordinates[0] = lon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }
}