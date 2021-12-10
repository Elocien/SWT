package tourable.customer;

import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tourable.booking.BookingManagement;

/**
 * {@linkplain Controller} class that handles frontend logic for all customer
 * components
 * 
 * @author Leon Augustat
 *
 */

@Controller
public class CustomerController {

	private final CustomerManagement customerManagement;
	private final BookingManagement bookingManagement;

	CustomerController(CustomerManagement customerManagement, BookingManagement bookingManagement) {
		this.customerManagement = Objects.requireNonNull(customerManagement);
		this.bookingManagement = Objects.requireNonNull(bookingManagement);
	}

	@GetMapping("/customers")
	@PreAuthorize("hasAnyRole('USER','BOSS')")
	public String customers(Model model) {

		customerManagement.deleteInactiveCustomers();
		model.addAttribute("customerList", customerManagement.findAllCustomers());
		return "customers";
	}

	@GetMapping("/customers/create")
	@PreAuthorize("hasAnyRole('USER','BOSS')")
	public String createCustomer(Model model, CustomerDataForm form) {

		model.addAttribute("form", form);
		return "customercreate";
	}

	@PostMapping("/customers/create")
	public String createCustomerValidation(Model model, @Valid @ModelAttribute("form") CustomerDataForm form,
			BindingResult result) {

		if (!customerManagement.isMailUnique(0l, form.getEmail())) {
			result.rejectValue("email", "errors.mailAlreadyUsed");
		}

		if (result.hasErrors()) {
			return createCustomer(model, form);
		}

		customerManagement.createCustomer(form);
		return "redirect:../customers";

	}

	@GetMapping("/customers/details")
	@PreAuthorize("hasAnyRole('USER','BOSS')")
	public String customerDetails(Model model, @RequestParam(required = false) Long id) {

		if (id == null) {
			return "redirect:../customers";
		}

		Optional<Customer> customer = customerManagement.findById(id);
		if (customer.isPresent()) {
			model.addAttribute("customer", customer.get());
			model.addAttribute("allBookings", bookingManagement.findByCustomer(customer.get()));
			model.addAttribute("deleteFlag", !bookingManagement.hasOpenBookingsForCustomer(customer.get()));
			return "customerdetails";
		} else {
			return "redirect:../customers";
		}
	}

	@GetMapping("/customers/edit")
	@PreAuthorize("hasAnyRole('USER','BOSS')")
	public String editCustomer(Model model, @RequestParam(required = false) Long id) {

		if (id == null) {
			return "redirect:../customers";
		}

		Optional<Customer> customer = customerManagement.findById(id);
		if (customer.isPresent()) {
			model.addAttribute("form", customerManagement.getFormForCustomer(customer.get()));
			return "customeredit";
		} else {
			return "redirect:../customers";
		}
	}

	@PostMapping("/customers/edit")
	@PreAuthorize("hasAnyRole('USER', 'BOSS')")
	public String editCustomerValidation(Model model, @Valid @ModelAttribute("form") CustomerDataForm form,
			BindingResult result, @RequestParam(required = false) Long id) {

		boolean mailAlreadyUsed = !customerManagement.isMailUnique(id, form.getEmail());

		if (mailAlreadyUsed) {
			result.rejectValue("email", "errors.mailAlreadyUsed");
		}
		if (result.hasErrors()) {
			model.addAttribute("form", form);
			return "customeredit";
		}

		customerManagement.updateCustomer(form, id);
		return "redirect:../customers";
	}
	
	@PostMapping("/customers/delete")
	@PreAuthorize("hasAnyRole('USER', 'BOSS')")
	public String deleteCustomerManually(@RequestParam Long id) {
		
		customerManagement.deleteCustomer(id);
		return "redirect:../customers";
	}
}
