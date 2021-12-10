package tourable.booking;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tourable.accommodation.Accommodation;
import tourable.accommodation.AccommodationManagement;
import tourable.booking.forms.AccommodationSearchForm;
import tourable.booking.forms.CustomerSearchForm;
import tourable.city.City;
import tourable.city.CityManagement;
import tourable.customer.CustomerManagement;
import tourable.travelguide.Travelguide;
import tourable.travelguide.TravelguideManagement;

/**
 * Controller class that manages frontend access to all booking components
 * 
 * @author Pascal Juppe
 * @author Florian Richter
 */
@Controller
public class BookingController {

	private final BookingManagement bookingManagement;
	private final AccommodationManagement accommodationManagement;
	private final CityManagement cityManagement;
	private final CustomerManagement customerManagement;
	private final TravelguideManagement travelguideManagement;
	private final BusinessTime businessTime;

	public BookingController(BookingManagement bookingManagement, AccommodationManagement accommodationManagement,
			CityManagement cityManagement, CustomerManagement customerManagement,
			TravelguideManagement travelguideManagement, BusinessTime businessTime) {
		this.bookingManagement = Objects.requireNonNull(bookingManagement);
		this.accommodationManagement = Objects.requireNonNull(accommodationManagement);
		this.cityManagement = Objects.requireNonNull(cityManagement);
		this.customerManagement = Objects.requireNonNull(customerManagement);
		this.travelguideManagement = Objects.requireNonNull(travelguideManagement);
		this.businessTime = Objects.requireNonNull(businessTime);
	}

	@GetMapping("/bookings")
	@PreAuthorize("hasAnyRole('USER','BOSS')")
	public String bookings(Model model) {

		model.addAttribute("bookingList", bookingManagement.findAll());
		return "bookings";
	}

	@GetMapping("/bookings/filter")
	@PreAuthorize("hasAnyRole('USER','BOSS')")
	public String bookingsByStatus(Model model, @RequestParam(required = false) String status) {

		if (status == null) {
			return "redirect:../bookings";
		}

		try {
			model.addAttribute("bookingList", bookingManagement.findByStatus(BookingStatus.valueOf(status)));
		} catch (IllegalArgumentException e) {
			return "redirect:../bookings";
		}

		return "bookings";
	}

	@GetMapping("bookings/details")
	@PreAuthorize("hasAnyRole('USER','BOSS')")
	public String bookingDetails(Model model, @RequestParam(required = false) Long id) {

		if (id == null) {
			return "redirect:../bookings";
		}

		var booking = bookingManagement.findById(id);
		if (booking.isPresent()) {
			var book = booking.get();
			model.addAttribute("booking", booking.get());

			boolean payFlag = ((booking.get().getStatus().equals(BookingStatus.OPEN)
					&& booking.get().getStart().toLocalDate().isBefore(businessTime.getTime().toLocalDate()))
					|| booking.get().getStatus().equals(BookingStatus.UNPAID));

			boolean cancelFlag = (!businessTime.getTime().toLocalDate().isAfter(book.getStart().toLocalDate())
					&& !book.getStatus().equals(BookingStatus.CANCELLED));

			boolean deleteFlag = (Interval.from(book.getTimeOfBooking()).to(book.getTimeOfBooking().plusDays(1))
					.contains(businessTime.getTime())
					&& (book.getStatus().equals(BookingStatus.OPEN) || (book.getStatus().equals(BookingStatus.PAID)
							&& book.getPaymentMethod().equals(PaymentMethod.CASH))));

			model.addAttribute("image",
					Base64.getEncoder().encodeToString(booking.get().getAccommodation().getImage()));
			model.addAttribute("payFlag", payFlag);
			model.addAttribute("cancelFlag", cancelFlag);
			model.addAttribute("deleteFlag", deleteFlag);
			return "bookingdetails";
		} else {
			return "redirect:../bookings";
		}
	}

	@PostMapping("bookings/pay")
	public String payBooking(Model model, @RequestParam Long id) {
		var optBooking = bookingManagement.findById(id);
		if (optBooking.isPresent()) {
			bookingManagement.pay(optBooking.get());
			return bookingDetails(model, id);
		}
		return "redirect:../bookings";
	}

	@PostMapping("bookings/cancel")
	public String cancelBooking(Model model, @RequestParam Long id) {
		var optBooking = bookingManagement.findById(id);
		if (optBooking.isPresent()) {
			bookingManagement.cancel(optBooking.get());
			return bookingDetails(model, id);
		}
		return "redirect:../bookings";
	}

	@PostMapping("bookings/delete")
	public String deleteBooking(Model model, @RequestParam Long id) {
		var optBooking = bookingManagement.findById(id);
		if (optBooking.isPresent()) {
			if (bookingManagement.delete(optBooking.get())) {
				return "redirect:../bookings";
			} else {
				return bookingDetails(model, id);
			}
		}
		return "redirect:../bookings";
	}

	@PostMapping(value = "bookings/create")
	public String createBooking(Model model, HttpSession session) {

		String template = null;

		if (session.getAttribute("accommodationId") == null) {
			model.addAttribute("cityList", cityManagement.findAll());
			template = "bookingchooseaccommodation";
		} else {
			long acmId = (long) session.getAttribute("accommodationId");

			if (session.getAttribute("customerId") == null) {
				template = "bookingchoosecustomer";
			} else {
				long customerId = (long) session.getAttribute("customerId");

				if (session.getAttribute("travelguideId") == null) {
					City city = accommodationManagement.findById(acmId).get().getCity();
					model.addAttribute("travelguideList", travelguideManagement.findByCity(city));
					template = "bookingchoosetravelguide";
				} else {
					long travelguideId = (long) session.getAttribute("travelguideId");

					Accommodation acm = accommodationManagement.findById(acmId).get();
					model.addAttribute("accommodation", acm);
					model.addAttribute("image", Base64.getEncoder().encodeToString(acm.getImage()));
					model.addAttribute("bookingDateRange", session.getAttribute("bookingDateRange"));
					model.addAttribute("customer", customerManagement.findById(customerId).get());
					model.addAttribute("travelguide",
							travelguideId != -1 ? travelguideManagement.findById(travelguideId).get() : null);

					var dates = ((String) session.getAttribute("bookingDateRange")).split(" ");
					var startDate = LocalDate.parse(dates[0]);
					var endDate = LocalDate.parse(dates[2]);
					var price = accommodationManagement.findById(acmId).get().getPrice()
							* ChronoUnit.DAYS.between(startDate, endDate);
					model.addAttribute("totalPrice", price);

					template = "bookingvalidation";
				}
			}
		}

		return template;
	}

	@PostMapping(value = "bookings/create", params = "city")
	public String updateAccommodationPage(Model model, @Valid AccommodationSearchForm accommodationForm) {

		model.addAttribute("cityList", cityManagement.findAll());
		model.addAttribute("accommodationForm", accommodationForm);

		var dates = accommodationForm.getDateRange().split(" ");
		var city = cityManagement.findByName(accommodationForm.getCity()).get();

		var type = accommodationForm.getType();
		var location = accommodationForm.getLocation();

		var accommodationList = accommodationManagement.findSuitableAccommodations(city, Arrays.asList(type),
				Arrays.asList(location), dates[0], dates[2], 0);

		// calculate alternatives
		if (accommodationList.isEmpty()) {
			var altTypes = Stream.of(type.prev(), Optional.of(type), type.next()).filter(Optional::isPresent)
					.map(Optional::get).collect(Collectors.toList());
			var altLocations = Stream.of(location.prev(), Optional.of(location), location.next())
					.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

			for (int offset = -3; offset <= 3; offset++) {
				accommodationList.addAll(accommodationManagement.findSuitableAccommodations(city, altTypes,
						altLocations, dates[0], dates[2], offset));
			}

			if (!accommodationList.isEmpty()) {
				model.addAttribute("usingAlternativesFlag", true);
			}
		}

		model.addAttribute("accommodationList", accommodationList);
		return "bookingchooseaccommodation";
	}

	@PostMapping(value = "bookings/create", params = "accommodationId")
	public String validateAccommodation(Model model, HttpSession session, @RequestParam Long accommodationId,
			@RequestParam String bookingDateRange) {

		if (accommodationId != -1) {
			session.setAttribute("accommodationId", accommodationId);
			session.setAttribute("bookingDateRange", bookingDateRange);
		}

		return createBooking(model, session);
	}

	@PostMapping(value = "bookings/create", params = "name")
	public String updateCustomerPage(Model model, @Valid CustomerSearchForm customerForm) {

		model.addAttribute("customerForm", customerForm);
		model.addAttribute("customerList", customerManagement.findByName(customerForm.getName()));
		return "bookingchoosecustomer";
	}

	@PostMapping(value = "bookings/create", params = "customerId")
	public String validateCustomer(Model model, HttpSession session, @RequestParam Long customerId) {

		if (customerId != -1) {
			session.setAttribute("customerId", customerId);
		}

		return createBooking(model, session);
	}

	@PostMapping(value = "bookings/create", params = "travelguideId")
	public String validateTravelguide(Model model, HttpSession session, @RequestParam Long travelguideId) {

		session.setAttribute("travelguideId", travelguideId);
		return createBooking(model, session);
	}

	@PostMapping(value = "bookings/create/book")
	public String bookBooking(Model model, HttpSession session, @RequestParam PaymentMethod paymentMethod) {

		if (session.getAttribute("accommodationId") == null || session.getAttribute("customerId") == null
				|| session.getAttribute("travelguideId") == null) {
			return createBooking(model, session);
		}

		var dates = ((String) session.getAttribute("bookingDateRange")).split(" ");
		var startDate = LocalDate.parse(dates[0]);
		var endDate = LocalDate.parse(dates[2]);
		long acmId = (long) session.getAttribute("accommodationId");
		var accommodation = accommodationManagement.findById(acmId).get();
		long customerId = (long) session.getAttribute("customerId");
		var customer = customerManagement.findById(customerId).get();

		var travelguideId = (long) session.getAttribute("travelguideId");
		Optional<Travelguide> travelguide = Optional.empty();
		if (travelguideId != -1) {
			travelguide = travelguideManagement.findById(travelguideId);
		}

		bookingManagement.createBooking(startDate, endDate, accommodation, customer, paymentMethod, travelguide);
		clearSessionAttributes(session);

		return "redirect:../../bookings";
	}

	@PostMapping(value = "bookings/create/abort")
	public String abortBooking(Model model, HttpSession session) {

		clearSessionAttributes(session);
		return "redirect:../../bookings";
	}

	@GetMapping(value = "bookings/pdfreport")
	public ResponseEntity<?> bookingReport(@RequestParam(required = false) Long id) {

		var headers = new HttpHeaders();

		if (id == null) {
			headers.add("Location", "/bookings");
			return new ResponseEntity<String>(headers, HttpStatus.FOUND);
		}

		var booking = bookingManagement.findById(id);
		if (booking.isPresent()) {

			byte[] pdfInvoice = new PDFGenerator().getPdfInvoiceAsBytes(booking.get());

			headers.setContentType(MediaType.parseMediaType("application/pdf"));
			headers.add("content-disposition", "inline;filename=report.pdf");
			headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

			return new ResponseEntity<byte[]>(pdfInvoice, headers, HttpStatus.OK);
		} else {
			headers.add("Location", "/bookings");
			return new ResponseEntity<String>(headers, HttpStatus.FOUND);
		}

	}

	private void clearSessionAttributes(HttpSession session) {
		session.removeAttribute("accommodationId");
		session.removeAttribute("bookingDateRange");
		session.removeAttribute("customerId");
		session.removeAttribute("travelguideId");
	}

}
