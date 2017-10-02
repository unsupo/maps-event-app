package app;

import object.User;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import users.UserAdapter;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@EnableAutoConfiguration
@RequestMapping("/")
@CrossOrigin
public class LoginController {

    /**
     * login with email and password and return token
     * if login fail, throw exception
     *
     * @param email
     * @param password
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/login", method = POST)
    public String login(@RequestHeader(value = "email") String email,
                        @RequestHeader(value = "password") String password) throws Exception {
        return UserAdapter.getUser(email,password).getSessionId();
    }

    /**
     * If valid google login,
     * if user already exists then return internal user's token
     * else create a new user
     *
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "loginWithGoogle", method = POST)
    public String loginWithGoogle(@RequestHeader(value = "token") String token) throws Exception {
        return UserAdapter.googleUser(token).getSessionId();
    }

    /**
     * Check if token is valid, if it's not return false otherwise return true
     *
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/isValidToken", method = POST)
    public boolean isValidToken(@RequestHeader(value = "token") String token) throws Exception {
        return UserAdapter.checkToken(token);
    }

    /**
     * registers a new user with email/password/firstname/lastname
     *
     * if successful return new token associated with that user
     * otherwise throw an exception
     *
     * @param email
     * @param password
     * @param firstName
     * @param lastName
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/register", method = POST)
    public String register(@RequestHeader(value = "email") String email,
                            @RequestHeader(value = "password") String password,
                            @RequestHeader(value = "firstName") String firstName,
                            @RequestHeader(value = "lastName") String lastName) throws Exception {
        return UserAdapter.registerUser(new User(email,password,firstName,lastName));
    }

    /**
     * give a current token and this method will return the user data associated with it
     *
     * NOT SURE IF THIS IS SECURE, it won't return the password of the user for sure!
     *
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getUserData", method = POST)
    public User getUserData(@RequestHeader(value = "token") String token) throws Exception {
        return UserAdapter.getUserByToken(token);
    }

}
