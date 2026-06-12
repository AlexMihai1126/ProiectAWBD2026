package ro.fmi.awbd.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.fmi.awbd.model.dto.request.InvoiceCreateRequest;
import ro.fmi.awbd.model.dto.request.InvoiceUpdateRequest;
import ro.fmi.awbd.model.dto.response.InvoiceResponse;
import ro.fmi.awbd.model.enums.InvoiceStatus;
import ro.fmi.awbd.model.enums.PaymentType;
import ro.fmi.awbd.service.ClientService;
import ro.fmi.awbd.service.InvoiceService;

@Controller
@RequestMapping("/shoots/{shootId}/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final ClientService clientService;

    @ModelAttribute("paymentTypes")
    public PaymentType[] paymentTypes() {
        return PaymentType.values();
    }

    @ModelAttribute("invoiceStatuses")
    public InvoiceStatus[] invoiceStatuses() {
        return InvoiceStatus.values();
    }

    @GetMapping("/new")
    public String createForm(@PathVariable Long shootId, Model model) {
        model.addAttribute("form", new InvoiceCreateRequest());
        model.addAttribute("shootId", shootId);
        model.addAttribute("clients", clientService.getAllClients());
        return "invoice/form";
    }

    @PostMapping
    public String create(@PathVariable Long shootId, @Valid @ModelAttribute("form") InvoiceCreateRequest form,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("shootId", shootId);
            model.addAttribute("clients", clientService.getAllClients());
            return "invoice/form";
        }
        invoiceService.createInvoice(shootId, form);
        ra.addFlashAttribute("flash", "Invoice created.");
        return "redirect:/shoots/" + shootId;
    }

    @GetMapping("/edit")
    public String editForm(@PathVariable Long shootId, Model model) {
        InvoiceResponse inv = invoiceService.getInvoice(shootId);
        model.addAttribute("form", InvoiceCreateRequest.builder()
                .clientId(inv.getClientId()).paymentMethod(inv.getPaymentMethod()).amount(inv.getAmount())
                .paidAt(inv.getPaidAt()).status(inv.getStatus()).details(inv.getDetails()).build());
        model.addAttribute("shootId", shootId);
        model.addAttribute("editId", inv.getId());
        model.addAttribute("clients", clientService.getAllClients());
        return "invoice/form";
    }

    @PostMapping("/edit")
    public String update(@PathVariable Long shootId, @Valid @ModelAttribute("form") InvoiceCreateRequest form,
                        BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("shootId", shootId);
            model.addAttribute("editId", -1);
            model.addAttribute("clients", clientService.getAllClients());
            return "invoice/form";
        }
        invoiceService.updateInvoice(shootId, InvoiceUpdateRequest.builder()
                .clientId(form.getClientId()).paymentMethod(form.getPaymentMethod()).amount(form.getAmount())
                .paidAt(form.getPaidAt()).status(form.getStatus()).details(form.getDetails()).build());
        ra.addFlashAttribute("flash", "Invoice updated.");
        return "redirect:/shoots/" + shootId;
    }

    @PostMapping("/delete")
    public String delete(@PathVariable Long shootId, RedirectAttributes ra) {
        invoiceService.deleteInvoice(shootId);
        ra.addFlashAttribute("flash", "Invoice deleted.");
        return "redirect:/shoots/" + shootId;
    }
}
