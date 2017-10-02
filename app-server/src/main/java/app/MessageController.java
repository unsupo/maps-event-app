package app;

import com.google.gson.reflect.TypeToken;
import object.Event;
import object.Message;
import object.User;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import users.UserAdapter;
import utilities.FileOptions;

import java.io.IOException;
import java.util.HashMap;

@Controller
@CrossOrigin
public class MessageController {
    @MessageMapping("/messages")
    @SendTo("/topic/messaging")
    public String message(String e){
        return e;
    }

}
