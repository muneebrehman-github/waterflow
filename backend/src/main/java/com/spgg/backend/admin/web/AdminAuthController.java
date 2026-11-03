package com.spgg.backend.admin.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    @GetMapping({"", "/"})
    public String home() {
        return "redirect:/admin/shops";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Authentication authentication,
            Model model) {
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()))) {
            return "redirect:/admin/shops";
        }

        if (error != null) {
            model.addAttribute(
                    "error",
                    "role".equals(error)
                            ? "Only Super Admin accounts can access this portal."
                            : "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Signed out successfully.");
        }
        return "admin/login";
    }
}
