package utilities.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import object.User;
import java.util.Collections;

public class GoogleLoginValidator {
    private static final String CLIENT_ID = "628531512920-5ch9465je33jersarg01eqd1147u7kkm.apps.googleusercontent.com";
    private static final JacksonFactory jacksonFactory = new JacksonFactory();

    public static User getUserFromToken(String token) throws Exception{
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), jacksonFactory)
                .setAudience(Collections.singletonList(CLIENT_ID))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

        GoogleIdToken idToken = verifier.verify(token);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            User user = new User(payload.getEmail());
            user.setUserId(payload.getSubject());
            user.setFirstName(payload.get("given_name").toString());
            user.setLastName(payload.get("family_name").toString());
            user.setImageUrl(payload.get("picture").toString());
            user.setLocale(payload.get("locale").toString());
            user.setVerified(payload.getEmailVerified());
            return user;
        } else
            throw new Exception("Invalid ID token.");
    }

}
