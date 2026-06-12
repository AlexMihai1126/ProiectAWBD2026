package ro.fmi.awbd.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.fmi.awbd.model.dto.request.GearCreateRequest;
import ro.fmi.awbd.model.dto.request.GearUpdateRequest;
import ro.fmi.awbd.model.dto.response.GearResponse;
import ro.fmi.awbd.model.dto.response.UserOptionResponse;
import ro.fmi.awbd.model.enums.GearType;
import ro.fmi.awbd.service.GearService;
import ro.fmi.awbd.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/gear")
@RequiredArgsConstructor
public class GearController {

    private final GearService gearService;
    private final UserService userService;

    @ModelAttribute("gearTypes")
    public GearType[] gearTypes() {
        return GearType.values();
    }

    @ModelAttribute("users")
    public List<UserOptionResponse> users() {
        return userService.getAllUsers();
    }

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "brand", direction = Sort.Direction.ASC) Pageable pageable,
                       Model model) {
        model.addAttribute("page", gearService.getGear(pageable));
        return "gear/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("gear", gearService.getGearById(id));
        return "gear/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new GearCreateRequest());
        return "gear/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") GearCreateRequest form,
                        BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "gear/form";
        }
        GearResponse saved = gearService.createGear(form);
        ra.addFlashAttribute("flash", "Gear item created.");
        return "redirect:/gear/" + saved.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        GearResponse g = gearService.getGearById(id);
        model.addAttribute("form", GearCreateRequest.builder()
                .type(g.getType()).brand(g.getBrand()).model(g.getModel())
                .notes(g.getNotes()).ownerId(g.getOwnerId()).build());
        model.addAttribute("editId", id);
        return "gear/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") GearCreateRequest form,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("editId", id);
            return "gear/form";
        }
        gearService.updateGear(id, GearUpdateRequest.builder()
                .type(form.getType()).brand(form.getBrand()).model(form.getModel())
                .notes(form.getNotes()).ownerId(form.getOwnerId()).build());
        ra.addFlashAttribute("flash", "Gear item updated.");
        return "redirect:/gear/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        gearService.deleteGear(id);
        ra.addFlashAttribute("flash", "Gear item deleted.");
        return "redirect:/gear";
    }
}
