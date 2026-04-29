package com.ordering.system.controller;

import com.ordering.system.entity.Item;
import com.ordering.system.repository.ItemRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/items")
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public String listItems(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String category,
                            Model model) {
        List<Item> items;
        if (search != null && !search.isBlank()) {
            items = itemRepository.findByNameContainingIgnoreCase(search);
        } else if (category != null && !category.isBlank()) {
            items = itemRepository.findByCategory(category);
        } else {
            items = itemRepository.findAll();
        }

        // Summary stats via DB queries — no in-memory calculation
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalItems",     itemRepository.count());
        summary.put("lowStockItems",  itemRepository.countLowStockItems());
        summary.put("inventoryValue", itemRepository.calculateTotalInventoryValue());
        summary.put("averagePrice",   itemRepository.averagePrice());

        model.addAttribute("items",            items);
        model.addAttribute("search",           search);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("inventorySummary", summary);
        return "items";
    }

    private boolean hasPermission(Authentication auth) {
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF") ||
                           a.getAuthority().equals("ROLE_ADMIN"));
    }

    @PostMapping("/add")
    public String addItem(@RequestParam String name,
                          @RequestParam String sku,
                          @RequestParam String category,
                          @RequestParam Double price,
                          @RequestParam Integer quantity,
                          @RequestParam(required = false) String description,
                          Authentication authentication,
                          RedirectAttributes ra) {
        if (!hasPermission(authentication)) {
            ra.addFlashAttribute("error", "You don't have permission to add items!");
            return "redirect:/items";
        }
        if (itemRepository.findBySku(sku).isPresent()) {
            ra.addFlashAttribute("error", "SKU already exists!");
            return "redirect:/items";
        }
        Item item = new Item();
        item.setName(name);
        item.setSku(sku);
        item.setCategory(category);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setDescription(description);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);
        ra.addFlashAttribute("success", "Item added successfully!");
        return "redirect:/items";
    }

    @PostMapping("/edit/{id}")
    public String editItem(@PathVariable Long id,
                           @RequestParam String name,
                           @RequestParam String sku,
                           @RequestParam String category,
                           @RequestParam Double price,
                           @RequestParam Integer quantity,
                           @RequestParam(required = false) String description,
                           Authentication authentication,
                           RedirectAttributes ra) {
        if (!hasPermission(authentication)) {
            ra.addFlashAttribute("error", "You don't have permission to edit items!");
            return "redirect:/items";
        }
        Item item = itemRepository.findById(id).orElseThrow();
        if (!item.getSku().equals(sku) && itemRepository.findBySku(sku).isPresent()) {
            ra.addFlashAttribute("error", "SKU already exists!");
            return "redirect:/items";
        }
        item.setName(name);
        item.setSku(sku);
        item.setCategory(category);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setDescription(description);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);
        ra.addFlashAttribute("success", "Item updated successfully!");
        return "redirect:/items";
    }

    @PostMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes ra) {
        if (!hasPermission(authentication)) {
            ra.addFlashAttribute("error", "You don't have permission to delete items!");
            return "redirect:/items";
        }
        itemRepository.deleteById(id);
        ra.addFlashAttribute("success", "Item deleted successfully!");
        return "redirect:/items";
    }
}