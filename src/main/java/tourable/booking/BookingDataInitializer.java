package tourable.booking;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import org.salespointframework.core.DataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import tourable.accommodation.AccommodationManagement;
import tourable.customer.CustomerManagement;

@Component
@Order(3)
public class BookingDataInitializer implements DataInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(BookingDataInitializer.class);
	private final BookingManagement bookingManagement;
	private final AccommodationManagement accommodationManagement;
	private final CustomerManagement customerManagement;

	public BookingDataInitializer(BookingManagement bookingManagement, AccommodationManagement accommodationManagement,
			CustomerManagement customerManagement) {
		this.bookingManagement = Objects.requireNonNull(bookingManagement);
		this.accommodationManagement = Objects.requireNonNull(accommodationManagement);
		this.customerManagement = Objects.requireNonNull(customerManagement);
	}

	@Override
	public void initialize() {
		LOG.info("creating default bookings");

		var accommodation = accommodationManagement.findByName("Casa de Till").get();
		var customer = customerManagement.findByEmail("kevinkeller@web.de").get();

		bookingManagement.createBooking(LocalDate.of(2020, 3, 12), LocalDate.of(2020, 3, 15), accommodation, customer,
				PaymentMethod.CASH, Optional.empty());
		bookingManagement.createBooking(LocalDate.of(2020, 4, 16), LocalDate.of(2020, 4, 20), accommodation, customer,
				PaymentMethod.TRANSFER, Optional.empty());
		bookingManagement.createBooking(LocalDate.of(2020, 1, 10), LocalDate.of(2020, 1, 15), accommodation, customer,
				PaymentMethod.TRANSFER, Optional.empty());
		bookingManagement.openBookingHandler();
	}

}
