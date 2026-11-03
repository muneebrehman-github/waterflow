package com.spgg.backend.admin.web;

import com.spgg.backend.security.UserPrincipal;
import com.spgg.backend.shop.ShopResponse;
import com.spgg.backend.shop.ShopService;
import com.spgg.backend.shop.ShopStatus;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/shops")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;

    @GetMapping
    public String shops(
            @RequestParam(required = false) ShopStatus status,
            @AuthenticationPrincipal UserPrincipal principal,
            Model model) {
        List<ShopResponse> allShops = shopService.list(null);
        List<ShopResponse> shops = status == null ? allShops : shopService.list(status);

        model.addAttribute("pageTitle", "Shops");
        model.addAttribute("activeNav", "shops");
        model.addAttribute("userFullName", principal != null ? principal.getFullName() : "Admin");
        model.addAttribute("shops", shops);
        model.addAttribute("allShops", allShops);
        model.addAttribute("currentStatus", status);
        model.addAttribute("statuses", ShopStatus.values());
        model.addAttribute("tabs", buildTabs());
        model.addAttribute("stats", buildStats(allShops));
        return "admin/shops";
    }

    @PostMapping("/{id}/approve")
    public String approve(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes) {
        try {
            ShopResponse shop = shopService.approve(id);
            redirectAttributes.addFlashAttribute("toast", shop.name() + " approved");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return redirectBack(status);
    }

    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes) {
        try {
            shopService.reject(id, reason);
            redirectAttributes.addFlashAttribute("toast", "Shop rejected");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return redirectBack(status);
    }

    @PostMapping("/{id}/suspend")
    public String suspend(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes) {
        try {
            shopService.suspend(id, reason);
            redirectAttributes.addFlashAttribute("toast", "Shop suspended");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return redirectBack(status);
    }

    @PostMapping("/{id}/reinstate")
    public String reinstate(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            RedirectAttributes redirectAttributes) {
        try {
            ShopResponse shop = shopService.reinstate(id);
            redirectAttributes.addFlashAttribute("toast", shop.name() + " reinstated");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return redirectBack(status);
    }

    private String redirectBack(String status) {
        if (!StringUtils.hasText(status)) {
            return "redirect:/admin/shops";
        }
        return "redirect:/admin/shops?status=" + status.trim();
    }

    private List<Tab> buildTabs() {
        return Arrays.asList(
                new Tab("Pending Approval", "Pending", ShopStatus.PENDING_APPROVAL),
                new Tab("Active", "Active", ShopStatus.ACTIVE),
                new Tab("Suspended", "Suspended", ShopStatus.SUSPENDED),
                new Tab("Rejected", "Rejected", ShopStatus.REJECTED),
                new Tab("Terminated", "Ended", ShopStatus.TERMINATED),
                new Tab("All", "All", null));
    }

    private Stats buildStats(List<ShopResponse> all) {
        return new Stats(
                all.size(),
                all.stream().filter(s -> s.status() == ShopStatus.PENDING_APPROVAL).count(),
                all.stream().filter(s -> s.status() == ShopStatus.ACTIVE).count(),
                all.stream().filter(s -> s.status() == ShopStatus.SUSPENDED).count());
    }

    public record Tab(String label, String shortLabel, ShopStatus status) {}

    public record Stats(long total, long pending, long active, long suspended) {}
}
