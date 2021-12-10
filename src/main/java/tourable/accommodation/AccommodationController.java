package tourable.accommodation;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import javax.activation.MimetypesFileTypeMap;
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
import tourable.city.CityManagement;

/**
 * {@linkplain Controller} class for {@linkplain Accommodation}s
 * 
 * @author Lucian McIntyre
 * @author Pascal Juppe
 */
@Controller
public class AccommodationController {

	private final AccommodationManagement accommodationManagement;
	private final CityManagement cityManagement;
	private final BookingManagement bookingManagement;

	MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

	/**
	 * Controller Constructor
	 * 
	 * @param accommodationManagement AccommodationManager which handles business
	 *                                logic for all Accommodations
	 * @param cityManagement          CityManagement which handles business logic
	 *                                for all Cities
	 * @param bookingManagement       manager that handles business logic for all
	 *                                bookings
	 */
	AccommodationController(AccommodationManagement accommodationManagement, CityManagement cityManagement,
			BookingManagement bookingManagement) {
		this.cityManagement = Objects.requireNonNull(cityManagement);
		this.accommodationManagement = Objects.requireNonNull(accommodationManagement);
		this.bookingManagement = Objects.requireNonNull(bookingManagement);
	}

	/**
	 * Mapping for initial Accommodation tab view. Only available to authenticated
	 * users of type {@code USER} and {@code BOSS}
	 * 
	 * @param model model interface
	 * @return list of all {@code Accommodation}s
	 */
	@GetMapping("/accommodations")
	@PreAuthorize("hasAnyRole('USER','BOSS')")
	public String accommodations(Model model) {

		model.addAttribute("form", new AccommodationFilterDataForm("", null, null));

		model.addAttribute("cityList", cityManagement.findAll());
		model.addAttribute("accommodationList", accommodationManagement.findAll());
		return "accommodations";
	}

	/**
	 * 
	 * @param model model interface
	 * @param form  the data form
	 * @return list of accommodations filtered by type, location and city given by
	 *         the form
	 */
	@GetMapping("/accommodations/filter")
	@PreAuthorize("hasAnyRole('USER','BOSS')")
	public String accommodationsFilter(Model model, @ModelAttribute("form") AccommodationFilterDataForm form) {

		model.addAttribute("cityList", cityManagement.findAll());
		model.addAttribute("form", form);

		model.addAttribute("accommodationList",
				accommodationManagement.findByFilter(form.getCity(), form.getLocation(), form.getType()));

		return "accommodations";
	}

	/**
	 * Mapping for details of {@code Accommodation}
	 * 
	 * @param model model interface
	 * @param id    the accommodation id
	 * @return Detailed view of single {@code Accommodation}
	 */
	@GetMapping("/accommodations/details")
	@PreAuthorize("hasAnyRole('USER','BOSS')")
	public String accommodationDetails(Model model, @RequestParam(required = false) Long id) {

		if (id == null) {
			return "redirect:../accommodations";
		}

		Optional<Accommodation> accommodation = accommodationManagement.findById(id);
		if (accommodation.isPresent()) {
			if (!bookingManagement.hasOpenBookingsForAccommodation(accommodation.get())) {
				model.addAttribute("deleteFlag", true);
			}
			model.addAttribute("accommodation", accommodation.get());
			model.addAttribute("image", Base64.getEncoder().encodeToString(accommodation.get().getImage()));
			return "accommodationdetails";
		} else {
			return "redirect:../accommodations";
		}
	}

	/**
	 * Mapping for creating an {@code Accommodation}
	 * 
	 * @param model model interface
	 * @param form  the data form
	 * @return created {@code Accommmodation} Object
	 */
	@GetMapping("accommodations/create")
	@PreAuthorize("hasRole('BOSS')")
	public String createAccommodation(Model model, AccommodationDataForm form) {

		model.addAttribute("cityList", cityManagement.findAll());
		model.addAttribute("form", form);
		return "accommodationcreate";
	}

	/**
	 * Validation of new {@code Accommodation}
	 * 
	 * @param model  model interface
	 * @param form   the data form
	 * @param result form validation results
	 * @return redirects to {@code /accommodations} if {@code AccommodationDataForm}
	 *         is invalid
	 */
	@PostMapping("accommodations/create")
	@PreAuthorize("hasRole('BOSS')")
	public String createAccommodationValidation(Model model, @Valid @ModelAttribute("form") AccommodationDataForm form,
			BindingResult result) {

		String mimeType = form.getImage().getContentType();
		if (!mimeType.equals("image/jpeg") && !mimeType.equals("image/jpg")) {
			result.rejectValue("image", "errors.fileNotImage");
		}

		if (result.hasErrors()) {
			return createAccommodation(model, form);
		}

		accommodationManagement.createAccommodation(form);
		return "redirect:../accommodations";
	}

	/**
	 * Mapping for deletion of {@code Accommodation}
	 * 
	 * @param id the accommodation id
	 * @return redirect to {@code /accommodations}
	 */
	@PostMapping("/accommodations/delete")
	@PreAuthorize("hasRole('BOSS')")
	public String accommodationsDelete(@RequestParam Long id) {
		accommodationManagement.deleteAccommodation(id);
		return "redirect:../accommodations";
	}

	/**
	 * Mapping for editing of {@code Accommodation}s
	 * 
	 * @param model model interface
	 * @param form  the data form
	 * @param id    the accommodation id
	 * @return redirect to {@code /accommodations}
	 */
	@GetMapping("/accommodations/edit")
	@PreAuthorize("hasRole('BOSS')")
	public String editAccommodation(Model model, AccommodationDataForm form, @RequestParam(required = false) Long id) {

		if (id == null) {
			return "redirect:../accommodations";
		}

		Optional<Accommodation> accommodation = accommodationManagement.findById(id);
		if (accommodation.isPresent()) {
			model.addAttribute("form", form);
			model.addAttribute("accommodation", accommodation.get());
			model.addAttribute("cityList", cityManagement.findAll());
			return "accommodationedit";
		} else {
			return "redirect:../accommodations";
		}
	}

	/**
	 * Validation of edited {@code Accommodation}
	 * 
	 * @param model  model interface
	 * @param form   the data form
	 * @param result form validation results
	 * @param id     the accommodation id
	 * @return redirects to {@code /accommodations} if {@code AccommodationDataForm}
	 *         is invalid
	 */
	@PostMapping("/accommodations/edit")
	public String editAccommodationValidation(Model model, @Valid @ModelAttribute("form") AccommodationDataForm form,
			BindingResult result, @RequestParam Long id) {

		String mimeType = form.getImage().getContentType();
		if (!form.getImage().isEmpty() && !mimeType.equals("image/jpeg") && !mimeType.equals("image/jpg")) {
			result.rejectValue("image", "errors.fileNotImage");
		}

		if (result.hasErrors()) {
			return editAccommodation(model, form, id);
		}

		accommodationManagement.updateAccommodation(id, form);
		return "redirect:../accommodations";
	}

}