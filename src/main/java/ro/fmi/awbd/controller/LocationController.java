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
import ro.fmi.awbd.model.dto.request.LocationCreateRequest;
import ro.fmi.awbd.model.dto.request.LocationUpdateRequest;
import ro.fmi.awbd.model.dto.response.LocationResponse;
import ro.fmi.awbd.service.LocationService;

@Controller
@RequestMapping("/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
                       Model model) {
        model.addAttribute("page", locationService.getLocations(pageable));
        return "location/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("location", locationService.getLocation(id));
        return "location/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new LocationCreateRequest());
        return "location/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") LocationCreateRequest form,
                        BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "location/form";
        }
        LocationResponse saved = locationService.createLocation(form);
        ra.addFlashAttribute("flash", "Location created.");
        return "redirect:/locations/" + saved.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        LocationResponse l = locationService.getLocation(id);
        model.addAttribute("form", LocationCreateRequest.builder()
                .name(l.getName()).county(l.getCounty()).country(l.getCountry())
                .latitude(l.getLatitude()).longitude(l.getLongitude()).build());
        model.addAttribute("editId", id);
        return "location/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") LocationCreateRequest form,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("editId", id);
            return "location/form";
        }
        locationService.updateLocation(id, LocationUpdateRequest.builder()
                .name(form.getName()).county(form.getCounty()).country(form.getCountry())
                .latitude(form.getLatitude()).longitude(form.getLongitude()).build());
        ra.addFlashAttribute("flash", "Location updated.");
        return "redirect:/locations/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        locationService.deleteLocation(id);
        ra.addFlashAttribute("flash", "Location deleted.");
        return "redirect:/locations";
    }
}
