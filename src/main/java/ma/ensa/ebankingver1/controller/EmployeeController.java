package ma.ensa.ebankingver1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ma.ensa.ebankingver1.service.EmployeeService;
/*
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestParam String newPassword, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        employeeService.changePassword(userId, newPassword);
        return new ResponseEntity<>("Mot de passe changé avec succès", HttpStatus.OK);
    }
}

 */