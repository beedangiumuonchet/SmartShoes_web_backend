package com.ds.project.application.startup;

import com.ds.project.application.startup.data.ProductStartup;
import com.ds.project.application.startup.data.RoleStartup;
import com.ds.project.application.startup.data.SettingStartup;
import com.ds.project.application.startup.data.UserStartup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(0)
public class AppStartup implements CommandLineRunner {

    private final RoleStartup roleStartup;
    private final UserStartup userStartup;
    private final SettingStartup settingStartup;
//    private final ProductStartup productStartup;

    @Override
    public void run(String... args) {
        log.info("Starting application startup process...");

        try {
            // Tuần tự chạy
            initializeRoles();
            initializeUsers();
            initializeSettings();
//            initializeProducts();

            log.info("Application startup process completed successfully");
        } catch (Exception e) {
            log.error("Application startup process failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Async
    public void initializeRoles() {
        roleStartup.initializeRoles();
        log.info("Roles initialized");
    }

    @Async
    public void initializeUsers() {
        userStartup.initializeUsers();
        log.info("Users initialized");
    }

    @Async
    public void initializeSettings() {
        settingStartup.initializeSettings();
        log.info("Settings initialized");
    }

//    @Async
//    public void initializeProducts() {
//        productStartup.initializeProductBaseData();
//        log.info("Products initialized");
//    }
}
