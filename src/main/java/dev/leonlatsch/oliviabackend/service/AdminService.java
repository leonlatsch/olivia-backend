package dev.leonlatsch.oliviabackend.service;

import antlr.ASdebug.IASDebugStream;
import dev.leonlatsch.oliviabackend.entity.Admin;
import dev.leonlatsch.oliviabackend.repository.AdminRepository;
import dev.leonlatsch.oliviabackend.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service to initialize and authenticate the admin
 *
 * @author Leon Latsch
 * @since 1.0.0
 */
@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private Environment env;

    @Autowired
    private AdminRepository adminRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String auth(String username, String password) {
        initAdmin();
        Optional<Admin> admin = adminRepository.findById(username);
        return admin.isPresent() && passwordEncoder.matches(password, admin.get().getPassword()) ? admin.get().getToken() : null;
    }

    public boolean auth(String token) {
        return adminRepository.findByToken(token).isPresent();
    }

    @EventListener(ContextRefreshedEvent.class)
    private void initAdmin() {
        if (adminRepository.findAll().isEmpty()) {
            String username = env.getProperty("admin.initial-username");
            String password = env.getProperty("admin.initial-password");
            if (username != null && password != null) {
                String hashedPassword = passwordEncoder.encode(password);
                adminRepository.save(new Admin(username, hashedPassword, CommonUtils.genSafeAccessToken()));
                log.info("Generated new admin user from initial config");
            }
        }
    }
}
