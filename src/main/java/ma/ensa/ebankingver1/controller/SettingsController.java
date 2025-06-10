package ma.ensa.ebankingver1.controller;

import jakarta.validation.Valid;
import ma.ensa.ebankingver1.model.GlobalSetting;
import ma.ensa.ebankingver1.service.SettingService;
import ma.ensa.ebankingver1.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
public class SettingsController {

    @Autowired
    private SettingService settingService;

    @Autowired
    private AuditService auditService;

    public SettingsController() {
        System.out.println("SettingsController initialized");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GlobalSetting>> getAllSettings() {
        System.out.println("Received GET request for /api/admin/settings");
        List<GlobalSetting> settings = settingService.getAllSettings();
        auditService.logAction("GET_ALL_SETTINGS", "SETTINGS", null, Map.of(), true);
        return ResponseEntity.ok(settings);
    }

    @PostMapping
    public ResponseEntity<GlobalSetting> addSetting(@RequestBody GlobalSetting setting) {
        try {
            System.out.println("Received POST request for /api/admin/settings");
            GlobalSetting newSetting = settingService.addSetting(setting);
            auditService.logAction("ADD_SETTING", "SETTINGS", null, Map.of("key", setting.getKey(), "value", setting.getValue()), true);
            return ResponseEntity.status(HttpStatus.CREATED).body(newSetting);
        } catch (IllegalArgumentException e) {
            auditService.logAction("ADD_SETTING_FAILED", "SETTINGS", null, Map.of("key", setting.getKey(), "error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GlobalSetting> updateSetting(@PathVariable("id") Long id, @Valid @RequestBody GlobalSetting setting) {
        try {
            System.out.println("Received PUT request for /api/admin/settings/" + id);
            setting.setId(id);
            GlobalSetting updatedSetting = settingService.updateSetting(setting);
            auditService.logAction("UPDATE_SETTING", "SETTINGS", id.toString(), Map.of("key", setting.getKey(), "value", setting.getValue()), true);
            return ResponseEntity.ok(updatedSetting);
        } catch (IllegalArgumentException e) {
            System.err.println("Setting not found for id: " + id);
            auditService.logAction("UPDATE_SETTING_FAILED", "SETTINGS", id.toString(), Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            System.err.println("Error updating setting: " + e.getMessage());
            e.printStackTrace();
            auditService.logAction("UPDATE_SETTING_FAILED", "SETTINGS", id.toString(), Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSetting(@PathVariable("id") Long id) {
        try {
            System.out.println("Received DELETE request for /api/admin/settings/" + id);
            settingService.deleteSetting(id);
            auditService.logAction("DELETE_SETTING", "SETTINGS", id.toString(), Map.of(), true);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            System.err.println("Setting not found for id: " + id);
            auditService.logAction("DELETE_SETTING_FAILED", "SETTINGS", id.toString(), Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error deleting setting: " + e.getMessage());
            e.printStackTrace();
            auditService.logAction("DELETE_SETTING_FAILED", "SETTINGS", id.toString(), Map.of("error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/by-key/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getSettingValue(@PathVariable("key") String key) {
        try {
            String value = settingService.getSettingValue(key);
            auditService.logAction("GET_SETTING_BY_KEY", "SETTINGS", null, Map.of("key", key), true);
            return ResponseEntity.ok(Map.of("value", value));
        } catch (IllegalArgumentException e) {
            auditService.logAction("GET_SETTING_BY_KEY_FAILED", "SETTINGS", null, Map.of("key", key, "error", "Setting not found"), false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Setting not found"));
        }
    }

    @PutMapping("/by-key/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalSetting> updateSettingByKey(@PathVariable("key") String key, @Valid @RequestBody GlobalSetting setting) {
        try {
            System.out.println("Received PUT request for /api/admin/settings/by-key/" + key);
            GlobalSetting updatedSetting = settingService.updateSettingByKey(key, setting.getValue());
            auditService.logAction("UPDATE_SETTING_BY_KEY", "SETTINGS", null, Map.of("key", key, "value", setting.getValue()), true);
            return ResponseEntity.ok(updatedSetting);
        } catch (IllegalArgumentException e) {
            System.err.println("Setting not found for key: " + key);
            auditService.logAction("UPDATE_SETTING_BY_KEY_FAILED", "SETTINGS", null, Map.of("key", key, "error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            System.err.println("Error updating setting by key: " + e.getMessage());
            auditService.logAction("UPDATE_SETTING_BY_KEY_FAILED", "SETTINGS", null, Map.of("key", key, "error", e.getMessage()), false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
//avant audit
/*
import jakarta.validation.Valid;
import ma.ensa.ebankingver1.model.GlobalSetting;
import ma.ensa.ebankingver1.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
public class SettingsController {

    @Autowired
    private SettingService settingService;

    public SettingsController() {
        System.out.println("SettingsController initialized");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GlobalSetting>> getAllSettings() {
        System.out.println("Received GET request for /api/admin/settings");
        List<GlobalSetting> settings = settingService.getAllSettings();
        return ResponseEntity.ok(settings);
    }

    @PostMapping
    public ResponseEntity<GlobalSetting> addSetting(@RequestBody GlobalSetting setting) {
        try {
            System.out.println("Received POST request for /api/admin/settings");
            GlobalSetting newSetting = settingService.addSetting(setting);
            return ResponseEntity.status(HttpStatus.CREATED).body(newSetting);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GlobalSetting> updateSetting(@PathVariable("id") Long id, @Valid @RequestBody GlobalSetting setting) {
        try {
            System.out.println("Received PUT request for /api/admin/settings/" + id);
            setting.setId(id);
            GlobalSetting updatedSetting = settingService.updateSetting(setting);
            return ResponseEntity.ok(updatedSetting);
        } catch (IllegalArgumentException e) {
            System.err.println("Setting not found for id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            System.err.println("Error updating setting: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSetting(@PathVariable("id") Long id) {
        try {
            System.out.println("Received DELETE request for /api/admin/settings/" + id);
            settingService.deleteSetting(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            System.err.println("Setting not found for id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error deleting setting: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/by-key/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getSettingValue(@PathVariable("key") String key) {
        try {
            String value = settingService.getSettingValue(key);
            return ResponseEntity.ok(Map.of("value", value));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Setting not found"));
        }
    }

    @PutMapping("/by-key/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalSetting> updateSettingByKey(@PathVariable("key") String key, @Valid @RequestBody GlobalSetting setting) {
        try {
            System.out.println("Received PUT request for /api/admin/settings/by-key/" + key);
            GlobalSetting updatedSetting = settingService.updateSettingByKey(key, setting.getValue());
            return ResponseEntity.ok(updatedSetting);
        } catch (IllegalArgumentException e) {
            System.err.println("Setting not found for key: " + key);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            System.err.println("Error updating setting by key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

 */