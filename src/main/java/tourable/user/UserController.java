package tourable.user;

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

@Controller
class UserController {

	final String passwordValidationRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

	private final UserManagement userManagement;

	public UserController(UserManagement userManagement) {
		this.userManagement = Objects.requireNonNull(userManagement);
	}

	@PostMapping("/users/create")
	String createUserValidation(Model model, @Valid @ModelAttribute("form") UserDataForm form, BindingResult result) {

		if (!userManagement.isMailUnique(0l, form.getEmail())) {
			result.rejectValue("email", "errors.mailAlreadyUsed");
		}

		if (!form.getPassword().matches(passwordValidationRegex)) {
			result.rejectValue("password", "errors.passwordTooWeak");
		}

		if (result.hasErrors()) {
			return createUser(model, form);
		}

		userManagement.createUser(form);
		return "redirect:../users";
	}

	@GetMapping("/users/create")
	String createUser(Model model, UserDataForm form) {
		model.addAttribute("form", form);
		return "usercreate";
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('BOSS')")
	public String users(Model model) {

		model.addAttribute("userList", userManagement.findAll());
		return "users";
	}

	@GetMapping("/users/details")
	@PreAuthorize("hasRole('BOSS')")
	public String userDetails(Model model, @RequestParam(required = false) Long id) {

		if (id == null) {
			return "redirect:../users";
		}

		Optional<User> user = userManagement.findById(id);
		if (user.isPresent()) {
			model.addAttribute("user", user.get());
			return "userdetails";
		} else {
			return "redirect:../users";
		}
	}

	@GetMapping("/users/edit")
	@PreAuthorize("hasRole('BOSS')")
	public String editUser(@RequestParam(required = false) Long id, UserDataForm form, Model model) {

		if (id == null) {
			return "redirect:../users";
		}

		Optional<User> user = userManagement.findById(id);
		if (user.isPresent()) {
			model.addAttribute("form", form);
			model.addAttribute("user", user.get());
			return "useredit";
		} else {
			return "redirect:../users";
		}
	}

	@PostMapping("/users/edit")
	@PreAuthorize("hasRole('BOSS')")
	public String editUserValidation(Model model, @Valid @ModelAttribute("form") UserDataForm form,
			BindingResult result, @RequestParam(required = false) Long id) {

		if (!userManagement.isMailUnique(id, form.getEmail())) {
			result.rejectValue("email", "errors.mailAlreadyUsed");
		}

		if (!form.getPassword().isEmpty() && !form.getPassword().matches(passwordValidationRegex)) {
			result.rejectValue("password", "errors.passwordTooWeak");
		}

		if (result.hasErrors()) {
			model.addAttribute("form", form);
			model.addAttribute("user", userManagement.findById(id).get());
			return "useredit";
		}

		userManagement.updateUser(form, id);
		return "redirect:../users";
	}

	@GetMapping("/users/delete")
	public String userDelete(Model model, @RequestParam(required = false) Long id) {

		userManagement.deleteUser(id);
		return "redirect:../users";
	}
}