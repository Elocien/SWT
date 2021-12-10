package tourable.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;

import tourable.AbstractIntegrationTest;
import tourable.accommodation.Accommodation;
import tourable.accommodation.AccommodationRepository;
import tourable.customer.Customer;
import tourable.customer.CustomerRepository;
import tourable.travelguide.Travelguide;
import tourable.travelguide.TravelguideRepository;

@AutoConfigureMockMvc
public class BookingControllerTest extends AbstractIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	BookingController bookingController;

	@Autowired
	BookingManagement bookingManagement;

	@Autowired
	BookingRepository bookings;

	@Autowired
	AccommodationRepository accommodations;

	@Autowired
	CustomerRepository customers;

	@Autowired
	TravelguideRepository travelguides;

	@Test
	@WithMockUser(roles = "USER")
	public void testBookingOverview() {

		ExtendedModelMap model = new ExtendedModelMap();

		bookingController.bookings(model);
		assertThat(model.get("bookingList")).isNotNull();
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testBookingFilterByStatus() throws Exception {

		mockMvc.perform(get("/bookings/filter")).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:../bookings"));
		mockMvc.perform(get("/bookings/filter").param("status", "")).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:../bookings"));

		mockMvc.perform(get("/bookings/filter").param("status", "OPEN")).andExpect(status().isOk())
				.andExpect(model().attribute("bookingList", is(notNullValue()))).andExpect(view().name("bookings"));

	}

	@Test
	@WithMockUser(roles = "USER")
	public void testBookingDetailsNull() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = bookingController.bookingDetails(model, null);
		assertThat(site).isEqualTo("redirect:../bookings");
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testBookingDetailsNonExistant() {

		ExtendedModelMap model = new ExtendedModelMap();

		String site = bookingController.bookingDetails(model, 0l);
		assertThat(site).isEqualTo("redirect:../bookings");
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testBookingDetails() throws Exception {

		Optional<Booking> optBooking = bookings.stream().findAny();
		if (optBooking.isEmpty()) {
			fail();
		}

		Booking booking = optBooking.get();
		long id = booking.getId();

		mockMvc.perform(get("/bookings/details").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("booking", is(notNullValue())));

		bookingManagement.cancel(booking);

		mockMvc.perform(get("/bookings/details").param("id", String.valueOf(id))).andExpect(status().isOk())
				.andExpect(model().attribute("booking", is(notNullValue())));
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testPayBooking() throws Exception {
		var startDate1 = LocalDate.of(2020, 6, 11);
		var endDate1 = LocalDate.of(2020, 6, 12);
		var paymentMethod1 = PaymentMethod.TRANSFER;

		Accommodation accommodation = accommodations.stream().findAny().get();
		Customer customer = customers.stream().findAny().get();

		// id has no booking
		var booking1 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod1,
				Optional.empty());

		bookingManagement.delete(booking1);

		mockMvc.perform(post("/bookings/pay").param("id", String.valueOf(booking1.getId())))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:../bookings"));

		// booking gets paid
		var booking2 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod1,
				Optional.empty());

		mockMvc.perform(post("/bookings/pay").param("id", String.valueOf(booking2.getId()))).andExpect(status().isOk())
				.andExpect(view().name("bookingdetails"));
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testCancelBooking() throws Exception {
		var startDate1 = LocalDate.of(2020, 6, 11);
		var endDate1 = LocalDate.of(2020, 6, 12);
		var paymentMethod1 = PaymentMethod.TRANSFER;

		Accommodation accommodation = accommodations.stream().findAny().get();
		Customer customer = customers.stream().findAny().get();

		// id has no booking
		var booking1 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod1,
				Optional.empty());

		bookings.delete(booking1);

		mockMvc.perform(post("/bookings/cancel").param("id", String.valueOf(booking1.getId())))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:../bookings"));

		// booking gets cancelled
		var booking2 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod1,
				Optional.empty());

		mockMvc.perform(post("/bookings/cancel").param("id", String.valueOf(booking2.getId())))
				.andExpect(status().isOk()).andExpect(view().name("bookingdetails"));

		assertEquals(BookingStatus.CANCELLED, bookingManagement.findById(booking2.getId()).get().getStatus());
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testDeleteBooking() throws Exception {
		var startDate1 = LocalDate.of(2020, 6, 11);
		var endDate1 = LocalDate.of(2020, 6, 12);
		var paymentMethod1 = PaymentMethod.TRANSFER;

		Accommodation accommodation = accommodations.stream().findAny().get();
		Customer customer = customers.stream().findAny().get();

		// id has no booking
		var booking1 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod1,
				Optional.empty());
		bookings.delete(booking1);
		mockMvc.perform(post("/bookings/delete").param("id", String.valueOf(booking1.getId())))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:../bookings"));

		// booking gets deleted
		var booking2 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod1,
				Optional.empty());
		mockMvc.perform(post("/bookings/delete").param("id", String.valueOf(booking2.getId())))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:../bookings"));

		// booking can't be deleted
		var booking3 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod1,
				Optional.empty());
		bookingManagement.cancel(booking3);
		mockMvc.perform(post("/bookings/delete").param("id", String.valueOf(booking3.getId())))
				.andExpect(status().isOk()).andExpect(view().name("bookingdetails"));
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testBookingCreation() throws Exception {

		Accommodation acm = accommodations.stream().findAny().get();
		long acmId = acm.getId();

		Customer customer = customers.stream().findAny().get();
		long customerId = customer.getId();

		Travelguide travelguide = travelguides.stream().findAny().get();
		long travelguideId = travelguide.getId();

		mockMvc.perform(post("/bookings/create")).andExpect(status().isOk())
				.andExpect(model().attribute("cityList", is(notNullValue())));

		mockMvc.perform(
				post("/bookings/create").param("city", acm.getCity().getName()).param("type", acm.getType().name())
						.param("location", acm.getLocation().name()).param("dateRange", "2019-12-02 - 2019-12-03"))
				.andExpect(status().isOk()).andExpect(model().attribute("accommodationList", is(notNullValue())));

		mockMvc.perform(post("/bookings/create").param("city", "Dresden").param("type", "SIMPLE")
				.param("location", "CITY_CENTRE").param("dateRange", "2020-12-02 - 2020-12-04"))
				.andExpect(status().isOk()).andExpect(model().attribute("accommodationList", is(notNullValue())));

		mockMvc.perform(post("/bookings/create").param("city", "Berlin").param("type", "SIMPLE")
				.param("location", "CITY_CENTRE").param("dateRange", "2020-12-02 - 2020-12-04"))
				.andExpect(status().isOk()).andExpect(model().attribute("accommodationList", is(notNullValue())))
				.andExpect(model().attribute("usingAlternativesFlag", is(notNullValue())));

		HttpSession session = mockMvc.perform(post("/bookings/create").param("accommodationId", "-1")
				.param("bookingDateRange", "2019-12-02 - 2019-12-03")).andExpect(status().isOk()).andReturn()
				.getRequest().getSession();
		assertNull(session.getAttribute("accommodationId"));
		assertNull(session.getAttribute("bookingDateRange"));

		session = mockMvc
				.perform(post("/bookings/create").param("accommodationId", String.valueOf(acmId))
						.param("bookingDateRange", "2019-12-02 - 2019-12-03"))
				.andExpect(status().isOk()).andReturn().getRequest().getSession();
		assertEquals(acmId, (long) session.getAttribute("accommodationId"));
		assertEquals("2019-12-02 - 2019-12-03", (String) session.getAttribute("bookingDateRange"));

		mockMvc.perform(post("/bookings/create").sessionAttr("accommodationId", acmId)).andExpect(status().isOk());

		mockMvc.perform(post("/bookings/create").sessionAttr("accommodationId", acmId).param("name", "Jana"))
				.andExpect(status().isOk()).andExpect(model().attribute("customerList", is(notNullValue())));

		session = mockMvc
				.perform(post("/bookings/create").sessionAttr("accommodationId", acmId).param("customerId", "-1"))
				.andExpect(status().isOk()).andReturn().getRequest().getSession();
		assertNull(session.getAttribute("customerId"));

		session = mockMvc
				.perform(post("/bookings/create").sessionAttr("accommodationId", acmId).param("customerId",
						String.valueOf(customerId)))
				.andExpect(status().isOk()).andExpect(model().attribute("travelguideList", is(notNullValue())))
				.andReturn().getRequest().getSession();
		assertEquals(customerId, (long) session.getAttribute("customerId"));

		session = mockMvc
				.perform(post("/bookings/create").sessionAttr("accommodationId", acmId)
						.sessionAttr("bookingDateRange", "2019-12-02 - 2019-12-03")
						.sessionAttr("customerId", customerId).param("travelguideId", "-1"))
				.andExpect(status().isOk()).andExpect(model().attribute("accommodation", is(notNullValue())))
				.andExpect(model().attribute("customer", is(notNullValue())))
				.andExpect(model().attribute("travelguide", is(nullValue()))).andReturn().getRequest().getSession();
		assertEquals(-1, (long) session.getAttribute("travelguideId"));

		session = mockMvc
				.perform(post("/bookings/create").sessionAttr("accommodationId", acmId)
						.sessionAttr("bookingDateRange", "2019-12-02 - 2019-12-03")
						.sessionAttr("customerId", customerId).param("travelguideId", String.valueOf(travelguideId)))
				.andExpect(status().isOk()).andExpect(model().attribute("accommodation", is(notNullValue())))
				.andExpect(model().attribute("totalPrice", is(notNullValue())))
				.andExpect(model().attribute("customer", is(notNullValue())))
				.andExpect(model().attribute("travelguide", is(notNullValue()))).andReturn().getRequest().getSession();
		assertEquals(travelguideId, (long) session.getAttribute("travelguideId"));

		mockMvc.perform(post("/bookings/create/book").param("paymentMethod", "CASH")).andExpect(status().isOk())
				.andExpect(model().attribute("cityList", is(notNullValue())));

		mockMvc.perform(
				post("/bookings/create/book").sessionAttr("accommodationId", acmId).param("paymentMethod", "CASH"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/bookings/create/book").sessionAttr("accommodationId", acmId)
				.sessionAttr("bookingDateRange", "2019-12-02 - 2019-12-03").param("paymentMethod", "CASH"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/bookings/create/book").sessionAttr("accommodationId", acmId)
				.sessionAttr("bookingDateRange", "2019-12-02 - 2019-12-03").sessionAttr("customerId", customerId)
				.param("paymentMethod", "CASH")).andExpect(status().isOk())
				.andExpect(model().attribute("travelguideList", is(notNullValue())));

		long numberOfBookings = bookings.count();
		session = mockMvc
				.perform(post("/bookings/create/book").sessionAttr("accommodationId", acmId)
						.sessionAttr("bookingDateRange", "2019-12-02 - 2019-12-03")
						.sessionAttr("customerId", customerId).sessionAttr("travelguideId", travelguideId)
						.param("paymentMethod", "CASH"))
				.andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
		assertNull(session.getAttribute("accommodationId"));
		assertNull(session.getAttribute("bookingDateRange"));
		assertNull(session.getAttribute("customerId"));
		assertNull(session.getAttribute("travelguideId"));
		assertEquals(numberOfBookings + 1, bookings.count());
	}

	@Test
	@WithMockUser(roles = "USER")
	public void testBookingReport() throws Exception {

		ResponseEntity<?> response = bookingController.bookingReport(null);
		assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/bookings");

		response = bookingController.bookingReport(0l);
		assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/bookings");

		Optional<Booking> optBooking = bookings.stream().findAny();
		if (optBooking.isEmpty()) {
			fail();
		}

		long id = optBooking.get().getId();

		mockMvc.perform(get("/bookings/pdfreport").param("id", String.valueOf(id))
				.contentType(MediaType.APPLICATION_PDF).accept(MediaType.APPLICATION_PDF_VALUE))
				.andExpect(status().isOk());

		bookingManagement.cancel(optBooking.get());

		mockMvc.perform(get("/bookings/pdfreport").param("id", String.valueOf(id))
				.contentType(MediaType.APPLICATION_PDF).accept(MediaType.APPLICATION_PDF_VALUE))
				.andExpect(status().isOk());

		Optional<Booking> optUnpaid = bookings.stream().filter(b -> b.getStatus() == BookingStatus.UNPAID).findAny();
		if (optUnpaid.isEmpty()) {
			fail();
		}

		id = optUnpaid.get().getId();

		mockMvc.perform(get("/bookings/pdfreport").param("id", String.valueOf(id))
				.contentType(MediaType.APPLICATION_PDF).accept(MediaType.APPLICATION_PDF_VALUE))
				.andExpect(status().isOk());

	}

	@Test
	@WithMockUser(roles = "USER")
	public void testBookingAbortion() throws Exception {
		mockMvc.perform(post("/bookings/create/abort").sessionAttr("accommodationId", "123")
				.sessionAttr("bookingDateRange", "2019-12-02 - 2019-12-03").sessionAttr("customerId", "124")
				.sessionAttr("travelguideId", "125")).andExpect(status().is3xxRedirection());
	}
}
