package tourable.city;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tourable.accommodation.AccommodationManagement;
import tourable.travelguide.TravelguideManagement;

/**
 * {@linkplain Controller} class that handles front end logic for all city
 * components
 * 
 * @author Pascal Juppe
 */
@Controller
public class CityController {

	private final CityManagement cityManagement;
	private final AccommodationManagement accommodationManagement;
	private final TravelguideManagement travelguideManagement;

	public CityController(CityManagement cityManagement, AccommodationManagement accommodationManagement,
			TravelguideManagement travelguideManagement) {
		this.cityManagement = cityManagement;
		this.accommodationManagement = accommodationManagement;
		this.travelguideManagement = travelguideManagement;
	}

	@GetMapping("/cities")
	@PreAuthorize("hasRole('BOSS')")
	public String cities(Model model) {

		model.addAttribute("cityList", cityManagement.findAll());
		return "cities";
	}

	@GetMapping("/cities/details")
	@PreAuthorize("hasRole('BOSS')")
	public String cityDetails(Model model, @RequestParam(required = false) Long id) {

		if (id == null) {
			return "redirect:../cities";
		}

		Optional<City> city = cityManagement.findById(id);
		if (city.isPresent()) {
			model.addAttribute("city", city.get());

			boolean deleteFlag = true;
			if (accommodationManagement.hasAccommodationsForCity(city.get())
					|| travelguideManagement.hasTravelguidesForCity(city.get())) {
				deleteFlag = false;
			}
			model.addAttribute("deleteFlag", deleteFlag);
			return "citydetails";
		} else {
			return "redirect:../cities";
		}
	}

	@GetMapping("/cities/edit")
	@PreAuthorize("hasRole('BOSS')")
	public String editCity(Model model, CityDataForm form, @RequestParam(required = false) Long id) {

		if (id == null) {
			return "redirect:../cities";
		}

		Optional<City> city = cityManagement.findById(id);
		if (city.isPresent()) {
			model.addAttribute("form", form);
			model.addAttribute("city", city.get());
			return "cityedit";
		} else {
			return "redirect:../cities";
		}
	}

	@PostMapping("/cities/edit")
	@PreAuthorize("hasRole('BOSS')")
	public String validateCityEdit(Model model, @Valid CityDataForm form, Errors result, @RequestParam Long id) {

		if (result.hasErrors()) {
			model.addAttribute("form", form);
			model.addAttribute("city", cityManagement.findById(id).get());
			return "cityedit";
		}

		cityManagement.updateCity(id, form);
		return "redirect:../cities";
	}

	@PostMapping("/cities/delete")
	@PreAuthorize("hasRole('BOSS')")
	public String deleteCity(@RequestParam Long id) {

		cityManagement.deleteCity(id);
		return "redirect:../cities";
	}

	@GetMapping("/cities/create")
	@PreAuthorize("hasRole('BOSS')")
	public String createCity(Model model, CityDataForm form) {

		model.addAttribute("form", form);
		return "citycreate";
	}

	@PostMapping("/cities/create")
	@PreAuthorize("hasRole('BOSS')")
	public String validateCityCreation(Model model, @Valid CityDataForm form, Errors result) {

		if (result.hasErrors()) {
			model.addAttribute("form", form);
			return "citycreate";
		}

		cityManagement.createCity(form);
		return "redirect:../cities";
	}

}
