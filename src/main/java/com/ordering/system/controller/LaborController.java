package com.ordering.system.controller;

import com.ordering.system.entity.Labor;
import com.ordering.system.repository.LaborRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/labor")
@PreAuthorize("hasRole('ADMIN')")
public class LaborController {

    private final LaborRepository laborRepository;

    public LaborController(LaborRepository laborRepository) {
        this.laborRepository = laborRepository;
    }

    @GetMapping
    public String listLabor(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) String status,
                            Model model) {
        // All filtering done in DB — no findAll() + stream
        List<Labor> laborList;
        if (search != null && !search.isBlank()) {
            laborList = laborRepository.findByNameContainingIgnoreCaseOrderByIdDesc(search);
        } else if (role != null && !role.isBlank()) {
            laborList = laborRepository.findByRoleIgnoreCaseOrderByIdDesc(role);
        } else if (status != null && !status.isBlank()) {
            laborList = laborRepository.findByStatusIgnoreCaseOrderByIdDesc(status);
        } else {
            laborList = laborRepository.findAllByOrderByIdDesc();
        }

        // Stats via DB queries
        model.addAttribute("laborList",      laborList);
        model.addAttribute("search",         search);
        model.addAttribute("selectedRole",   role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("totalStaff",     laborRepository.count());
        model.addAttribute("activeCount",    laborRepository.countActive());
        model.addAttribute("inactiveCount",  laborRepository.countInactive());
        model.addAttribute("totalPayroll",   laborRepository.sumActiveSalary());

        return "labor";
    }

    @PostMapping("/add")
    public String addLabor(@RequestParam String name,
                           @RequestParam String role,
                           @RequestParam String shift,
                           @RequestParam String status,
                           @RequestParam Double salary,
                           @RequestParam(required = false) String contact,
                           @RequestParam(required = false) String hireDate,
                           RedirectAttributes ra) {
        Labor labor = new Labor();
        labor.setName(name);
        labor.setRole(role);
        labor.setShift(shift);
        labor.setStatus(status != null ? status : "Active");
        labor.setSalary(salary);
        labor.setContact(contact);
        if (hireDate != null && !hireDate.isBlank()) {
            labor.setHireDate(LocalDate.parse(hireDate));
        }
        laborRepository.save(labor);
        ra.addFlashAttribute("success", "Staff added successfully!");
        return "redirect:/labor";
    }

    @PostMapping("/edit/{id}")
    public String editLabor(@PathVariable Long id,
                            @RequestParam String name,
                            @RequestParam String role,
                            @RequestParam String shift,
                            @RequestParam String status,
                            @RequestParam Double salary,
                            @RequestParam(required = false) String contact,
                            @RequestParam(required = false) String hireDate,
                            RedirectAttributes ra) {
        Labor labor = laborRepository.findById(id).orElseThrow();
        labor.setName(name);
        labor.setRole(role);
        labor.setShift(shift);
        labor.setStatus(status != null ? status : "Active");
        labor.setSalary(salary);
        labor.setContact(contact);
        labor.setHireDate(hireDate != null && !hireDate.isBlank()
            ? LocalDate.parse(hireDate) : null);
        laborRepository.save(labor);
        ra.addFlashAttribute("success", "Staff updated successfully!");
        return "redirect:/labor";
    }

    @PostMapping("/delete/{id}")
    public String deleteLabor(@PathVariable Long id, RedirectAttributes ra) {
        laborRepository.deleteById(id);
        ra.addFlashAttribute("success", "Staff removed successfully!");
        return "redirect:/labor";
    }
}