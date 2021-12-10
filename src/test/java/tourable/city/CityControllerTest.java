package tourable.city;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;

import tourable.AbstractIntegrationTest;
import tourable.travelguide.TravelguideDataForm;
import tourable.travelguide.TravelguideManagement;

@AutoConfigureMockMvc
public class CityControllerTest extends AbstractIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	CityController cityController;

	@Autowired
	TravelguideManagement travelguideManagement;

	@Autowired
	CityRepository cityRepo;

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityOverview() {

		ExtendedModelMap model = new ExtendedModelMap();

		cityController.cities(model);
		assertThat(model.get("cityList")).isNotNull();
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityDetailsNull() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = cityController.cityDetails(model, null);
		assertEquals("redirect:../cities", site);
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityDetailsNonExistant() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = cityController.cityDetails(model, 0l);
		assertEquals("redirect:../cities", site);
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityDetails() throws Exception {

		Optional<City> optCity = cityRepo.stream().findAny();
		if (optCity.isEmpty()) {
			fail();
		}

		City city = optCity.get();
		long id = city.getId();

		mockMvc.perform(get("/cities/details").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("city", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityDetailsDeleteFlag() throws Exception {
		City newCity = cityRepo.save(new City("new city"));
		long id2 = newCity.getId();

		mockMvc.perform(get("/cities/details").param("id", String.valueOf(id2))).andExpect(status().isOk())
				.andExpect(model().attribute("city", is(notNullValue())))
				.andExpect(model().attribute("deleteFlag", is(true)));

		travelguideManagement.createTravelguide(new TravelguideDataForm("new travelguide", "new city", 2d));

		mockMvc.perform(get("/cities/details").param("id", String.valueOf(id2))).andExpect(status().isOk())
				.andExpect(model().attribute("city", is(notNullValue())))
				.andExpect(model().attribute("deleteFlag", is(false)));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityEditNull() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = cityController.editCity(model, new CityDataForm(null), null);
		assertEquals("redirect:../cities", site);
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityEditNonExistant() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = cityController.editCity(model, new CityDataForm(null), 0l);
		assertEquals("redirect:../cities", site);
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityEdit() throws Exception {

		Optional<City> optCity = cityRepo.stream().findAny();
		if (optCity.isEmpty()) {
			fail();
		}

		City city = optCity.get();
		long id = city.getId();

		mockMvc.perform(get("/cities/edit").param("name", "neuer_stadtname").param("id", String.valueOf(id)))
				.andExpect(status().isOk()).andExpect(model().attribute("form", is(notNullValue())))
				.andExpect(model().attribute("city", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityEditValidation() throws Exception {

		Optional<City> optCity = cityRepo.stream().findAny();
		if (optCity.isEmpty()) {
			fail();
		}

		City city = optCity.get();
		long id = city.getId();

		mockMvc.perform(post("/cities/edit").param("name", "neuer_stadtname").param("id", String.valueOf(id)))
				.andExpect(status().is3xxRedirection());

		City edited = cityRepo.findById(id).get();
		assertEquals("neuer_stadtname", edited.getName());

		mockMvc.perform(post("/cities/edit").param("name", "").param("id", String.valueOf(id)))
				.andExpect(status().isOk()).andExpect(model().attribute("city", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityCreationPage() {

		ExtendedModelMap model = new ExtendedModelMap();

		cityController.createCity(model, new CityDataForm(null));
		assertThat(model.getAttribute("form")).isNotNull();
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityCreateValidation() throws Exception {

		mockMvc.perform(post("/cities/create").param("name", "neue_city")).andExpect(status().is3xxRedirection());

		Optional<City> newCity = cityRepo.stream().filter(city -> city.getName().equals("neue_city")).findAny();
		assertThat(newCity.isPresent());

		mockMvc.perform(post("/cities/create").param("name", "")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testCityDeletion() throws Exception {

		City cityToDelete = new City("city_to_delete");
		long id = cityRepo.save(cityToDelete).getId();

		mockMvc.perform(post("/cities/delete").param("id", String.valueOf(id))).andExpect(status().is3xxRedirection());

		Optional<City> deleted = cityRepo.findById(id);
		assertThat(deleted.isEmpty());
	}

}
