package ro.fmi.awbd.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.fmi.awbd.model.dto.request.ShootCreateRequest;
import ro.fmi.awbd.model.dto.request.ShootUpdateRequest;
import ro.fmi.awbd.model.dto.response.ShootResponse;
import ro.fmi.awbd.model.enums.ShootStatus;
import ro.fmi.awbd.service.GearService;
import ro.fmi.awbd.service.ClientService;
import ro.fmi.awbd.service.InvoiceService;
import ro.fmi.awbd.service.LocationService;
import ro.fmi.awbd.service.MediaService;
import ro.fmi.awbd.service.ShootService;
import ro.fmi.awbd.service.UserService;

@Controller
@RequestMapping("/shoots")
@RequiredArgsConstructor
public class ShootController {

    private final ShootService shootService;
    private final LocationService locationService;
    private final GearService gearService;
    private final UserService userService;
    private final MediaService mediaService;
    private final InvoiceService invoiceService;
    private final ClientService clientService;

    @ModelAttribute("statuses")
    public ShootStatus[] statuses() {
        return ShootStatus.values();
    }

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "startAt", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model, Authentication authentication) {
        model.addAttribute("page", isClient(authentication)
                ? shootService.getShootsForClient(authentication.getName(), pageable)
                : shootService.getShoots(pageable));
        model.addAttribute("clientView", isClient(authentication));
        return "shoot/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("shoot", isClient(authentication)
                ? shootService.getShootByIdForClient(id, authentication.getName())
                : shootService.getShootById(id));
        model.addAttribute("media", mediaService.listMedia(id));
        model.addAttribute("hasInvoice", invoiceService.existsForShoot(id));
        if (invoiceService.existsForShoot(id)) {
            model.addAttribute("invoice", invoiceService.getInvoice(id));
        }
        return "shoot/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ShootCreateRequest());
        populateOptions(model);
        return "shoot/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ShootCreateRequest form,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            populateOptions(model);
            return "shoot/form";
        }
        ShootResponse saved = shootService.createShoot(form);
        ra.addFlashAttribute("flash", "Shoot created.");
        return "redirect:/shoots/" + saved.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ShootResponse s = shootService.getShootById(id);
        model.addAttribute("form", ShootCreateRequest.builder()
                .title(s.getTitle()).status(s.getStatus()).startAt(s.getStartAt()).endAt(s.getEndAt())
                .notes(s.getNotes()).ownerId(s.getOwnerId()).locationId(s.getLocationId())
                .clientId(s.getClientId())
                .gearItemIds(s.getGearItemIds()).build());
        model.addAttribute("editId", id);
        populateOptions(model);
        return "shoot/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") ShootCreateRequest form,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("editId", id);
            populateOptions(model);
            return "shoot/form";
        }
        shootService.updateShoot(id, ShootUpdateRequest.builder()
                .title(form.getTitle()).status(form.getStatus()).startAt(form.getStartAt()).endAt(form.getEndAt())
                .notes(form.getNotes()).ownerId(form.getOwnerId()).locationId(form.getLocationId())
                .clientId(form.getClientId())
                .gearItemIds(form.getGearItemIds()).build());
        ra.addFlashAttribute("flash", "Shoot updated.");
        return "redirect:/shoots/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        shootService.deleteShoot(id);
        ra.addFlashAttribute("flash", "Shoot deleted.");
        return "redirect:/shoots";
    }

    private void populateOptions(Model model) {
        model.addAttribute("locations", locationService.getAllLocations());
        model.addAttribute("gearItems", gearService.getAllGear());
        model.addAttribute("users", userService.getPhotographers());
        model.addAttribute("clients", clientService.getAllClients());
    }

    private boolean isClient(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean client = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()));
        return client && !admin;
    }
}
