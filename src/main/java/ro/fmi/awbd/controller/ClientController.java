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
import ro.fmi.awbd.model.dto.request.ClientCreateRequest;
import ro.fmi.awbd.model.dto.request.ClientUpdateRequest;
import ro.fmi.awbd.model.dto.response.ClientResponse;
import ro.fmi.awbd.service.ClientService;
import ro.fmi.awbd.service.UserService;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final UserService userService;

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
                       Model model) {
        model.addAttribute("page", clientService.getClients(pageable));
        return "client/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.getClient(id));
        return "client/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ClientCreateRequest());
        populateUsers(model);
        return "client/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ClientCreateRequest form,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            populateUsers(model);
            return "client/form";
        }
        ClientResponse saved = clientService.createClient(form);
        ra.addFlashAttribute("flash", "Client created.");
        return "redirect:/clients/" + saved.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ClientResponse c = clientService.getClient(id);
        model.addAttribute("form", ClientCreateRequest.builder()
                .name(c.getName()).email(c.getEmail()).phone(c.getPhone()).notes(c.getNotes())
                .userId(c.getUserId()).build());
        model.addAttribute("editId", id);
        populateUsers(model);
        return "client/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") ClientCreateRequest form,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("editId", id);
            populateUsers(model);
            return "client/form";
        }
        clientService.updateClient(id, ClientUpdateRequest.builder()
                .name(form.getName()).email(form.getEmail()).phone(form.getPhone()).notes(form.getNotes())
                .userId(form.getUserId()).build());
        ra.addFlashAttribute("flash", "Client updated.");
        return "redirect:/clients/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        clientService.deleteClient(id);
        ra.addFlashAttribute("flash", "Client deleted.");
        return "redirect:/clients";
    }

    private void populateUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
    }
}
