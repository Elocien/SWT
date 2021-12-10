package tourable.travelguide;

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

@AutoConfigureMockMvc
public class TravelguideControllerTest extends AbstractIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	TravelguideController controller;

	@Autowired
	TravelguideRepository repo;

	@Test
	@WithMockUser(roles = "USER")
	public void testTravelguideOverview() {

		ExtendedModelMap model = new ExtendedModelMap();

		controller.travelguides(model);
		assertThat(model.get("travelguideList")).isNotNull();
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testTravelguideFiltering() throws Exception {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = controller.filterTravelguidesByCity(model, null);
		assertThat(site).isEqualTo("redirect:../travelguides");

		mockMvc.perform(get("/travelguides/filter").param("city", "")).andExpect(status().is3xxRedirection());

		mockMvc.perform(get("/travelguides/filter").param("city", "ImaginaryCity"))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(get("/travelguides/filter").param("city", "Dresden")).andExpect(status().isOk())
				.andExpect(model().attribute("travelguideList", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testTravelguideDetailsNull() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = controller.travelguideDetails(model, null);
		assertThat(site).isEqualTo("redirect:../travelguides");
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testTravelguideDetailsNonExistant() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = controller.travelguideDetails(model, 0l);
		assertThat(site).isEqualTo("redirect:../travelguides");
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testTravelguideDetails() throws Exception {

		Optional<Travelguide> optTravelguide = repo.stream().findAny();
		if (optTravelguide.isEmpty()) {
			fail();
		}

		Travelguide travelguide = optTravelguide.get();
		long id = travelguide.getId();

		mockMvc.perform(get("/travelguides/details").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("travelguide", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testTravelguideEditNull() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = controller.editTravelguide(model, new TravelguideDataForm(null, null, null), null);
		assertThat(site).isEqualTo("redirect:../travelguides");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testTravelguideEditNonExistant() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = controller.editTravelguide(model, new TravelguideDataForm(null, null, null), 0l);
		assertThat(site).isEqualTo("redirect:../travelguides");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testTravelguideEdit() throws Exception {

		Optional<Travelguide> optTravelguide = repo.stream().findAny();
		if (optTravelguide.isEmpty()) {
			fail();
		}

		Travelguide travelguide = optTravelguide.get();
		long id = travelguide.getId();

		mockMvc.perform(get("/travelguides/edit").param("name", "neuer_name").param("city", "Berlin")
				.param("price", "13.37").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("form", is(notNullValue())))
				.andExpect(model().attribute("cityList", is(notNullValue())))
				.andExpect(model().attribute("travelguide", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testTravelguideEditValidation() throws Exception {

		Optional<Travelguide> optTravelguide = repo.stream().findAny();
		if (!optTravelguide.isPresent()) {
			fail();
		}

		Travelguide travelguide = optTravelguide.get();
		long id = travelguide.getId();

		mockMvc.perform(post("/travelguides/edit").param("name", "neuer_name").param("city", "Berlin")
				.param("price", "13.37").param("id", String.valueOf(id))).andExpect(status().is3xxRedirection());

		Travelguide edited = repo.findById(id).get();
		assertEquals("neuer_name", edited.getName());
		assertEquals("Berlin", edited.getCity().getName());
		assertEquals(13.37d, edited.getPrice());

		mockMvc.perform(post("/travelguides/edit").param("name", "").param("city", "Berlin").param("price", "13.37")
				.param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("form", is(notNullValue())))
				.andExpect(model().attribute("travelguide", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testTravelguideCreationPage() {

		ExtendedModelMap model = new ExtendedModelMap();

		controller.createTravelguide(model, new TravelguideDataForm(null, null, null));
		assertThat(model.getAttribute("cityList")).isNotNull();
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testTravelguideCreateValidation() throws Exception {

		mockMvc.perform(post("/travelguides/create").param("name", "neuer_travelguide").param("city", "Berlin")
				.param("price", "99.99")).andExpect(status().is3xxRedirection());

		Optional<Travelguide> newTravelguide = repo.stream()
				.filter(travelguide -> travelguide.getName().equals("neuer_travelguide")).findAny();
		assertThat(newTravelguide.isPresent());

		mockMvc.perform(post("/travelguides/create").param("name", "").param("city", "Berlin").param("price", "99.99"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testTravelguideDeletion() throws Exception {

		Optional<Travelguide> optTravelguide = repo.stream().findAny();
		if (!optTravelguide.isPresent()) {
			fail();
		}

		Travelguide travelguide = optTravelguide.get();
		long id = travelguide.getId();

		mockMvc.perform(post("/travelguides/delete").param("id", String.valueOf(id)))
				.andExpect(status().is3xxRedirection());

		Optional<Travelguide> deleted = repo.findById(id);
		assertThat(deleted.isEmpty());
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testTravelguideSalePage() {

		ExtendedModelMap model = new ExtendedModelMap();

		controller.sellTravelguide(model);
		assertThat(model.getAttribute("cityList")).isNotNull();
		assertThat(model.getAttribute("travelguideList")).isNull();
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testTravelguideSale() throws Exception {

		mockMvc.perform(post("/travelguides/sale").param("city", "Berlin")).andExpect(status().isOk())
				.andExpect(model().attribute("cityList", is(notNullValue())));

		mockMvc.perform(post("/travelguides/sale").param("city", "")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testTravelguideSaleNull() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = controller.validateTravelguideSale(model, null);
		assertThat(site).isEqualTo("travelguidesale");
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testTravelguideSaleNonExistant() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = controller.validateTravelguideSale(model, 0l);
		assertThat(site).isEqualTo("redirect:../travelguides");
	}

}
