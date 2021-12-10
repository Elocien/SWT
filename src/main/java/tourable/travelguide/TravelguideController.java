package tourable.travelguide;

import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tourable.city.City;
import tourable.city.CityManagement;

/**
 * {@linkplain Controller} class that handles front end logic for all travel
 * guide components
 * 
 * @author Pascal Juppe
 */
@Controller
public class TravelguideController {

	private final TravelguideManagement travelguideManagement;
	private final CityManagement cityManagement;

	TravelguideController(TravelguideManagement travelguideManagement, CityManagement cityManagement) {
		this.travelguideManagement = Objects.requireNonNull(travelguideManagement);
		this.cityManagement = Objects.requireNonNull(cityManagement);
	}

	@GetMapping("/travelguides")
	@PreAuthorize("hasAnyRole('USER', 'BOSS')")
	public String travelguides(Model model) {

		model.addAttribute("travelguideList", travelguideManagement.findAll());
		model.addAttribute("cityList", cityManagement.findAll());
		return "travelguides";
	}

	@GetMapping("/travelguides/filter")
	@PreAuthorize("hasAnyRole('USER','BOSS')")
	public String filterTravelguidesByCity(Model model, @RequestParam(required = false) String city) {

		if (city == null || city.isEmpty()) {
			return "redirect:../travelguides";
		}

		Optional<City> optCity = cityManagement.findByName(city);
		if (optCity.isPresent()) {
			model.addAttribute("travelguideList", travelguideManagement.findByCity(optCity.get()));
			model.addAttribute("cityList", cityManagement.findAll());
			return "travelguides";
		}

		return "redirect:../travelguides";
	}

	@GetMapping("/travelguides/details")
	@PreAuthorize("hasAnyRole('USER', 'BOSS')")
	public String travelguideDetails(Model model, @RequestParam(required = false) Long id) {

		if (id == null) {
			return "redirect:../travelguides";
		}

		Optional<Travelguide> travelguide = travelguideManagement.findById(id);
		if (travelguide.isPresent()) {
			model.addAttribute("travelguide", travelguide.get());
			return "travelguidedetails";
		} else {
			return "redirect:../travelguides";
		}
	}

	@GetMapping("/travelguides/edit")
	@PreAuthorize("hasRole('BOSS')")
	public String editTravelguide(Model model, TravelguideDataForm form, @RequestParam(required = false) Long id) {

		if (id == null) {
			return "redirect:../travelguides";
		}

		Optional<Travelguide> travelguide = travelguideManagement.findById(id);
		if (travelguide.isPresent()) {
			model.addAttribute("form", form);
			model.addAttribute("travelguide", travelguide.get());
			model.addAttribute("cityList", cityManagement.findAll());
			return "travelguideedit";
		} else {
			return "redirect:../travelguides";
		}
	}

	@PostMapping("/travelguides/edit")
	@PreAuthorize("hasRole('BOSS')")
	public String editTravelguideValidation(Model model, @Valid TravelguideDataForm form, Errors result,
			@RequestParam Long id) {

		if (result.hasErrors()) {
			model.addAttribute("form", form);
			model.addAttribute("travelguide", travelguideManagement.findById(id).get());
			return "travelguideedit";
		}

		travelguideManagement.updateTravelguide(id, form);
		return "redirect:../travelguides";
	}

	@GetMapping("travelguides/create")
	@PreAuthorize("hasRole('BOSS')")
	public String createTravelguide(Model model, TravelguideDataForm form) {

		model.addAttribute("form", form);
		model.addAttribute("cityList", cityManagement.findAll());
		return "travelguidecreate";
	}

	@PostMapping("travelguides/create")
	@PreAuthorize("hasRole('BOSS')")
	public String createTravelguideValidation(Model model, @Valid TravelguideDataForm form, Errors result) {

		if (result.hasErrors()) {
			return "travelguidecreate";
		}

		travelguideManagement.createTravelguide(form);
		return "redirect:../travelguides";
	}

	@PostMapping("travelguides/delete")
	@PreAuthorize("hasRole('BOSS')")
	public String deleteTravelguide(@RequestParam Long id) {

		travelguideManagement.deleteTravelguide(id);
		return "redirect:../travelguides";
	}

	@GetMapping("travelguides/sale")
	@PreAuthorize("hasAnyRole('USER', 'BOSS')")
	public String sellTravelguide(Model model) {
		model.addAttribute("travelguideList", null);
		model.addAttribute("cityList", cityManagement.findAll());
		return "travelguidesale";
	}

	@PostMapping("travelguides/sale")
	@PreAuthorize("hasAnyRole('USER', 'BOSS')")
	public String sellTravelguide(Model model, @RequestParam String city) {

		if (city != null && !city.isEmpty()) {
			City c = cityManagement.findByName(city).get();
			Iterable<Travelguide> travelguideList = travelguideManagement.findByCity(c);
			model.addAttribute("travelguideList", travelguideList);
			model.addAttribute("cityList", cityManagement.findAll());
		}

		return "travelguidesale";
	}

	@PostMapping("travelguides/validateSale")
	@PreAuthorize("hasAnyRole('USER', 'BOSS')")
	public String validateTravelguideSale(Model model, @RequestParam Long saleId) {

		if (saleId != null) {
			travelguideManagement.buyTravelguide(saleId);
			return "redirect:../travelguides";
		}

		return sellTravelguide(model);
	}
}
