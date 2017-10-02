package object;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Event{
    private String eventName, address, description, category, eventId, userIdCreated;
    private LocalDateTime startTime, endTime;
    private Location location = new Location();
    private boolean isPublic = false;
    private List<String> usersGoing = new ArrayList<>();

    public Event(String eventName, String address, String description, String userId) {
        this.eventName = eventName;
        this.address = address;
        this.description = description;
        this.userIdCreated = userId;
        this.eventId = UUID.randomUUID().toString().replace("-","");
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Event(){
        this.eventId = UUID.randomUUID().toString().replace("-","");
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getUserIdCreated() {
        return userIdCreated;
    }

    public void setUserIdCreated(String userIdCreated) {
        this.userIdCreated = userIdCreated;
    }

    public double getLat() {
        return this.location.getLat();
    }

    public void setLat(double lat) {
        this.location.setLat(lat);
    }

    public double getLng() {
        return this.location.getLon();
    }

    public void setLng(double lng) {
        this.location.setLon(lng);
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public List<String> getUsersGoing() {
        return usersGoing;
    }

    public void setUsersGoing(List<String> usersGoing) {
        this.usersGoing = usersGoing;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return eventId != null ? eventId.equals(event.eventId) : event.eventId == null;
    }

    @Override
    public int hashCode() {
        return eventId != null ? eventId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventName='" + eventName + '\'' +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", eventId='" + eventId + '\'' +
                ", isPublic=" + isPublic +
                ", usersGoing=" + usersGoing +
                '}';
    }
}
