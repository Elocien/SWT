package tourable.accommodation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;

import tourable.AbstractIntegrationTest;
import tourable.booking.BookingManagement;

@AutoConfigureMockMvc
public class AccommodationControllerTest extends AbstractIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	AccommodationRepository accommodations;

	@Autowired
	AccommodationController controller;

	@Autowired
	AccommodationRepository repo;

	@Autowired
	BookingManagement bookingManagement;

	@Test
	@WithMockUser(roles = "BOSS")
	public void testAccommodationList() {
		ExtendedModelMap model = new ExtendedModelMap();

		controller.accommodations(model);
		assertThat(model.get("accommodationList")).isNotNull();
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testAccommodationDetailsNull() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.accommodationDetails(model, null);
		assertThat(page).isEqualTo("redirect:../accommodations");
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testAccommodationDetailsNonExistent() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.accommodationDetails(model, 0l);
		assertThat(page).isEqualTo("redirect:../accommodations");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testAccommodationDetails() throws Exception {

		Optional<Accommodation> optAccommodation = repo.stream().findAny();
		if (optAccommodation.isEmpty()) {
			fail();
		}

		Accommodation accommodation = optAccommodation.get();
		long id = accommodation.getId();

		mockMvc.perform(get("/accommodations/details").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("accommodation", is(notNullValue())))
				.andExpect(model().attribute("image", is(notNullValue())));

		Optional<Accommodation> optAcmWithBooking = repo.stream()
				.filter(acm -> bookingManagement.hasOpenBookingsForAccommodation(acm)).findAny();
		if (optAcmWithBooking.isEmpty()) {
			fail();
		}

		id = optAcmWithBooking.get().getId();

		mockMvc.perform(get("/accommodations/details").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("accommodation", is(notNullValue())))
				.andExpect(model().attribute("image", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testAccommodationCreationPage() {

		ExtendedModelMap model = new ExtendedModelMap();

		controller.createAccommodation(model,
				new AccommodationDataForm(null, null, null, null, null, null, null, null, null, null));
		assertThat(model.getAttribute("cityList")).isNotNull();
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testAccommodationCreation() throws Exception {

		final MockMultipartFile image = new MockMultipartFile("image", "filename.jpg", "image/jpeg",
				"some data".getBytes());

		final MockMultipartFile notAnImage = new MockMultipartFile("image", "paper.pdf", "application/pdf",
				"interesting paper".getBytes());

		mockMvc.perform(multipart("/accommodations/create").file(image).param("name", "test_name")
				.param("city", "Berlin").param("description", "test_description").param("price", "5.00")
				.param("bedNumber", "10").param("roomNumber", "20").param("provision", "0.5").param("type", "SIMPLE")
				.param("location", "SUBURBAN")).andExpect(status().is3xxRedirection());

		mockMvc.perform(multipart("/accommodations/create").file(notAnImage).param("name", "test_name")
				.param("city", "Berlin").param("description", "test_description").param("price", "5.00")
				.param("bedNumber", "10").param("roomNumber", "20").param("provision", "0.5").param("type", "SIMPLE")
				.param("location", "SUBURBAN")).andExpect(status().isOk());

		Optional<Accommodation> optAccommodation = accommodations.stream()
				.filter(accommodation -> accommodation.getName().equals("test_name")).findAny();
		assertThat(optAccommodation.isPresent());

		assertThat("cityList").isNotNull();
		assertThat("accommodationList").isNotNull();
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testAccommodationDeletion() throws Exception {

		Optional<Accommodation> optAccommodation = accommodations.stream().findAny();
		if (!optAccommodation.isPresent()) {
			fail();
		}

		Accommodation accommodation = optAccommodation.get();
		long id = accommodation.getId();

		mockMvc.perform(post("/accommodations/delete").param("id", String.valueOf(id)))
				.andExpect(status().is3xxRedirection());

		Optional<Accommodation> deleted = accommodations.findById(id);
		assertThat(deleted.isEmpty());
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testAccommodationEditPage() {

		Optional<Accommodation> optAccommodation = accommodations.stream().findAny();
		if (!optAccommodation.isPresent()) {
			fail();
		}

		long id = optAccommodation.get().getId();

		ExtendedModelMap model = new ExtendedModelMap();

		controller.editAccommodation(model,
				new AccommodationDataForm(null, null, null, null, null, null, null, null, null, null), id);
		assertThat(model.getAttribute("cityList")).isNotNull();

	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testAccommodationEdit() throws Exception {

		Optional<Accommodation> optAccommodation = accommodations.stream().findAny();
		if (!optAccommodation.isPresent()) {
			fail();
		}

		Accommodation accommodation = optAccommodation.get();
		long id = accommodation.getId();

		var name = accommodation.getName();
		var city = accommodation.getCity().getName();
		var desc = accommodation.getDescription();
		var price = String.valueOf(accommodation.getPrice());
		var bedNo = String.valueOf(accommodation.getBedNumber());
		var roomNo = String.valueOf(accommodation.getRoomNumber());
		var provision = String.valueOf(accommodation.getProvision());
		var type = accommodation.getType().name();
		var location = accommodation.getLocation().name();

		final MockMultipartFile image = new MockMultipartFile("image", "filename.jpg", "image/jpeg",
				"some data".getBytes());

		mockMvc.perform(multipart("/accommodations/edit").file(image).param("id", String.valueOf(id))
				.param("name", name).param("city", city).param("description", desc).param("price", price)
				.param("bedNumber", bedNo).param("roomNumber", roomNo).param("provision", provision).param("type", type)
				.param("location", location)).andExpect(status().is3xxRedirection());

		final MockMultipartFile notAnImage = new MockMultipartFile("image", "paper.pdf", "application/pdf",
				"interesting paper".getBytes());

		mockMvc.perform(multipart("/accommodations/edit").file(notAnImage).param("id", String.valueOf(id))
				.param("name", name).param("city", city).param("description", desc).param("price", price)
				.param("bedNumber", bedNo).param("roomNumber", roomNo).param("provision", provision).param("type", type)
				.param("location", location)).andExpect(status().isOk());

		assertEquals("some data", new String(accommodations.findById(id).get().getImage()));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testEditAccommodationNonExistent() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.editAccommodation(model,
				new AccommodationDataForm(null, null, null, null, null, null, null, null, null, null), 0l);
		assertThat(page).isEqualTo("redirect:../accommodations");
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void testEditAccommodationNull() {
		ExtendedModelMap model = new ExtendedModelMap();

		String page = controller.editAccommodation(model,
				new AccommodationDataForm(null, null, null, null, null, null, null, null, null, null), null);
		assertThat(page).isEqualTo("redirect:../accommodations");
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testAccommodationFilter() throws Exception {
		// ExtendedModelMap model = new ExtendedModelMap();

		// String page = acmController.accommodationsFilter(model, new
		// AccommodationFilterDataForm("Dresden", null, null));
		// assertThat(page).isEqualTo("redirect:../accommodations");

		mockMvc.perform(get("/accommodations/filter").param("city", "")).andExpect(status().isOk());

		mockMvc.perform(get("/accommodations/filter").param("city", "ImaginaryCity")).andExpect(status().isOk());

		mockMvc.perform(get("/accommodations/filter").param("city", "Dresden")).andExpect(status().isOk())
				.andExpect(model().attribute("accommodationList", is(notNullValue())));
	}

}
