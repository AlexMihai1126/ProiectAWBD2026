package ro.fmi.awbd.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.fmi.awbd.model.dto.request.MediaCreateRequest;
import ro.fmi.awbd.model.dto.request.MediaUpdateRequest;
import ro.fmi.awbd.model.dto.response.MediaResponse;
import ro.fmi.awbd.model.enums.MediaType;
import ro.fmi.awbd.service.MediaService;

@Controller
@RequestMapping("/shoots/{shootId}/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @ModelAttribute("mediaTypes")
    public MediaType[] mediaTypes() {
        return MediaType.values();
    }

    @GetMapping("/new")
    public String createForm(@PathVariable Long shootId, Model model) {
        model.addAttribute("form", new MediaCreateRequest());
        model.addAttribute("shootId", shootId);
        return "media/form";
    }

    @PostMapping
    public String create(@PathVariable Long shootId, @Valid @ModelAttribute("form") MediaCreateRequest form,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("shootId", shootId);
            return "media/form";
        }
        mediaService.createMedia(shootId, form);
        ra.addFlashAttribute("flash", "Media added.");
        return "redirect:/shoots/" + shootId;
    }

    @GetMapping("/{mediaId}/edit")
    public String editForm(@PathVariable Long shootId, @PathVariable Long mediaId, Model model) {
        MediaResponse m = mediaService.getMedia(shootId, mediaId);
        model.addAttribute("form", MediaCreateRequest.builder()
                .mediaType(m.getMediaType()).fileRef(m.getFileRef()).takenAt(m.getTakenAt())
                .iso(m.getIso()).aperture(m.getAperture()).shutterSpeed(m.getShutterSpeed())
                .focalLength(m.getFocalLength()).focalLength35mm(m.getFocalLength35mm())
                .widthPx(m.getWidthPx()).heightPx(m.getHeightPx()).rating(m.getRating())
                .notes(m.getNotes()).durationSeconds(m.getDurationSeconds()).build());
        model.addAttribute("shootId", shootId);
        model.addAttribute("editId", mediaId);
        return "media/form";
    }

    @PostMapping("/{mediaId}")
    public String update(@PathVariable Long shootId, @PathVariable Long mediaId,
                        @Valid @ModelAttribute("form") MediaCreateRequest form,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("shootId", shootId);
            model.addAttribute("editId", mediaId);
            return "media/form";
        }
        mediaService.updateMedia(shootId, mediaId, MediaUpdateRequest.builder()
                .mediaType(form.getMediaType()).fileRef(form.getFileRef()).takenAt(form.getTakenAt())
                .iso(form.getIso()).aperture(form.getAperture()).shutterSpeed(form.getShutterSpeed())
                .focalLength(form.getFocalLength()).focalLength35mm(form.getFocalLength35mm())
                .widthPx(form.getWidthPx()).heightPx(form.getHeightPx()).rating(form.getRating())
                .notes(form.getNotes()).durationSeconds(form.getDurationSeconds()).build());
        ra.addFlashAttribute("flash", "Media updated.");
        return "redirect:/shoots/" + shootId;
    }

    @PostMapping("/{mediaId}/delete")
    public String delete(@PathVariable Long shootId, @PathVariable Long mediaId, RedirectAttributes ra) {
        mediaService.deleteMedia(shootId, mediaId);
        ra.addFlashAttribute("flash", "Media deleted.");
        return "redirect:/shoots/" + shootId;
    }
}
