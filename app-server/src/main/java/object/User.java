package object;

import utilities.PasswordEncryptDecrypt;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

public class User {
    public static final String DEFAULT_GROUP = "default", ADMIN_GROUP = "admin";

    private String userId, firstName, lastName,
            emailAddress, imageUrl,
            userGroup = DEFAULT_GROUP, sessionId, password, locale = "en";
    private Date dateCreated, dateLastLogin;
    private boolean isVerified = false;
    private HashSet<String> eventsMadeIds, messageIds, eventsGoingIds;

    public User(String email, String password, String firstName, String lastName) throws GeneralSecurityException, UnsupportedEncodingException {
        this.emailAddress = email; this.firstName = firstName; this.lastName = lastName;
        this.password = PasswordEncryptDecrypt.encrypt(password);
        this.sessionId = UUID.randomUUID().toString().replace("-","");
        this.userId = UUID.randomUUID().toString().replace("-","");
        this.dateCreated = new Date();
    }

    public User(String email) {
        this.emailAddress = email;
        this.sessionId = UUID.randomUUID().toString().replace("-","");
        this.userId = UUID.randomUUID().toString().replace("-","");
        this.dateCreated = new Date();
    }

    public String getLocale() {
        return locale;
    }

    public HashSet<String> getEventMadesIds() {
        return eventsMadeIds;
    }

    public void setEventMadesIds(HashSet<String> eventMadesIds) {
        this.eventsMadeIds = eventMadesIds;
    }

    public HashSet<String> getEventsGoingIds() {
        return eventsGoingIds;
    }

    public void setEventsGoingIds(HashSet<String> eventsGoingIds) {
        this.eventsGoingIds = eventsGoingIds;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateLastLogin() {
        return dateLastLogin;
    }

    public void setDateLastLogin(Date dateLastLogin) {
        this.dateLastLogin = dateLastLogin;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    public HashSet<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(HashSet<String> messageIds) {
        this.messageIds = messageIds;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return emailAddress != null ? emailAddress.equals(user.emailAddress) : user.emailAddress == null;
    }

    @Override
    public int hashCode() {
        return emailAddress != null ? emailAddress.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", userGroup='" + userGroup + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", messageIds=" + messageIds +
                '}';
    }
}
