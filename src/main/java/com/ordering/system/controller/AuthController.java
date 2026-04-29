package com.ordering.system.controller;

import com.ordering.system.service.ReportService;
import com.ordering.system.repository.OrderRepository;
import com.ordering.system.repository.ItemRepository;
import com.ordering.system.repository.UserRepository;
import com.ordering.system.repository.LaborRepository;
import com.ordering.system.entity.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.*;

@Controller
public class AuthController {

    private final ReportService    reportService;
    private final OrderRepository  orderRepository;
    private final ItemRepository   itemRepository;
    private final UserRepository   userRepository;
    private final LaborRepository  laborRepository;

    public AuthController(ReportService reportService,
                          OrderRepository orderRepository,
                          ItemRepository itemRepository,
                          UserRepository userRepository,
                          LaborRepository laborRepository) {
        this.reportService    = reportService;
        this.orderRepository  = orderRepository;
        this.itemRepository   = itemRepository;
        this.userRepository   = userRepository;
        this.laborRepository  = laborRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        LocalDate today        = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        // Revenue summary (cached in ReportService)
        Map<String, Object> revenueSummary = reportService.getRevenueSummary(thirtyDaysAgo, today);
        model.addAttribute("totalSales",   revenueSummary.get("totalSales"));
        model.addAttribute("totalOrders",  revenueSummary.get("totalOrders"));

        // Counts via DB — no findAll()
        model.addAttribute("totalItems", itemRepository.count());
        model.addAttribute("totalStaff", userRepository.count());

        // Labor stats via DB queries
        model.addAttribute("activeLabor",   laborRepository.countActive());
        model.addAttribute("inactiveLabor", laborRepository.countInactive());
        model.addAttribute("totalPayroll",  laborRepository.sumActiveSalary());

        // Recent 5 orders — DB sorted, no stream
        List<Order> recentOrders = orderRepository.findTop5ByOrderByOrderDateDesc();
        model.addAttribute("recentOrders", recentOrders);

        // Order status counts via DB
        model.addAttribute("completedCount", orderRepository.countByStatus("Completed"));
        model.addAttribute("pendingCount",   orderRepository.countByStatus("Pending"));
        model.addAttribute("cancelledCount", orderRepository.countByStatus("Cancelled"));
        model.addAttribute("confirmedCount", orderRepository.countByStatus("Confirmed"));

        // Top 5 selling items — DB grouped query
        List<Object[]> topRaw = orderRepository.findTopSellingItems(PageRequest.of(0, 5));
        List<Map<String, Object>> topSellingItems = new ArrayList<>();
        for (Object[] row : topRaw) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("itemOrdered", row[0]);
            entry.put("quantity",    row[1]);
            topSellingItems.add(entry);
        }
        model.addAttribute("topSellingItems", topSellingItems);

        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        model.addAttribute("currentDate", new Date());

        return "dashboard";
    }
}