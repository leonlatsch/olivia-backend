package dev.leonlatsch.kolibriserver.controller;

import dev.leonlatsch.kolibriserver.constants.FormatsAndFiles;
import dev.leonlatsch.kolibriserver.model.dto.rabbitmq.ResourceCheck;
import dev.leonlatsch.kolibriserver.service.AdminService;
import dev.leonlatsch.kolibriserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Rest Controller for authenticating rabbitmq users
 *
 * @author Leon Latsch
 * @since 1.0.0
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("api/v1/auth/mq")
public class MQAuthController extends BaseController {

    private static final String ALLOW = "allow";
    private static final String DENY = "deny";
    private static final String ALLOW_ADMIN = "allow administrator";

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @RequestMapping(value = "user", method = RequestMethod.POST)
    public String user(@RequestParam("username") String username, @RequestParam("password") String password) {
        if (adminService.rawAuth(username, password)) {
            return ALLOW_ADMIN;
        }
        return userService.authWithToken(username, password) ? ALLOW : DENY;
    }

    @RequestMapping(value = "resource", method = RequestMethod.POST)
    public String resource(ResourceCheck check) {
        if (check.getUsername().equals(adminService.getUsername())) {
            return ALLOW;
        }

        return check.getUsername().equals(check.getName().replace(FormatsAndFiles.USER_QUEUE_PREFIX, "")) // Check if the user accesses its own queue
                && check.getPermission().equals("read") && check.getResource().equals("queue") ? ALLOW : DENY; // Only allow read access to queues
    }

    @RequestMapping(value = "vhost", method = RequestMethod.POST)
    public String vhost() {
        return ALLOW; // Allow access to all vhosts for all users
    }

    @RequestMapping(value = "topic", method = RequestMethod.POST)
    public String topic() {
        return DENY; // Deny topic messages
    }
}
