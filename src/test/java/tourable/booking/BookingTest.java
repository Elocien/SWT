package tourable.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;

import tourable.AbstractIntegrationTest;
import tourable.accommodation.Accommodation;
import tourable.customer.Customer;

public class BookingTest extends AbstractIntegrationTest {

	@Autowired
	BusinessTime businessTime;

	@Test
	public void bookingConstructorTest() {

		var teststarttime = businessTime.getTime();
		var start = LocalDate.of(2000, 01, 01).atTime(0, 0, 0);
		var end = LocalDate.of(2000, 12, 31).atTime(23, 59, 59);
		var price = 1200f;
		var paymentMethod = PaymentMethod.CASH;
		var status = BookingStatus.OPEN;
		var customer = new Customer("X Y", "street", "m@i.l", "123", businessTime);
		var accommodation = mock(Accommodation.class);
		Booking booking = new Booking(start, end, price, paymentMethod, status, customer, accommodation, businessTime);
		var testendtime = businessTime.getTime();

		assertEquals(start, booking.getStart());
		assertEquals(start, booking.getInterval().getStart());
		assertEquals(end, booking.getEnd());
		assertEquals(end, booking.getInterval().getEnd());
		assertEquals(price, booking.getPrice());
		assertEquals(paymentMethod, booking.getPaymentMethod());
		assertEquals(status, booking.getStatus());
		assertEquals(customer, booking.getCustomer());
		assertEquals(accommodation, booking.getAccommodation());
		assertEquals(true, Interval.from(teststarttime).to(testendtime).contains(booking.getTimeOfBooking()));
	}

	@Test
	public void bookingSetterTest() {

		var start = LocalDate.of(2000, 01, 01).atTime(0, 0, 0);
		var end = LocalDate.of(2000, 12, 31).atTime(23, 59, 59);
		var price = 1200f;
		var paymentMethod = PaymentMethod.CASH;
		var status = BookingStatus.OPEN;
		var customer = new Customer("X Y", "street", "m@i.l", "123", businessTime);
		var accommodation = mock(Accommodation.class);
		Booking booking = new Booking(start, end, price, paymentMethod, status, customer, accommodation, businessTime);
		var newStatus = BookingStatus.PAID;
		booking.setStatus(newStatus);

		assertEquals(newStatus, booking.getStatus());
	}

}
