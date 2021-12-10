package tourable.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;

import tourable.AbstractIntegrationTest;
import tourable.accommodation.Accommodation;
import tourable.accommodation.AccommodationManagement;
import tourable.accounting.AccountingManagement;
import tourable.accounting.Invoice;
import tourable.accounting.InvoiceRepository;
import tourable.customer.Customer;
import tourable.customer.CustomerManagement;

public class BookingManagementTest extends AbstractIntegrationTest {

	@Autowired
	BookingManagement bookingManagement;

	@Autowired
	BookingRepository bookingRepository;

	@Autowired
	AccommodationManagement accommodationManagement;

	@Autowired
	CustomerManagement customerManagement;

	@Autowired
	InvoiceRepository invoiceRepository;

	@Autowired
	BusinessTime businessTime;

	ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", LocaleContextHolder.getLocale());
	String bookingId = resourceBundle.getString("bookings.id.short");
	String paidFromCustomer = resourceBundle.getString("accounting.description.paidFromCustomer");
	String paidToAccommodation = resourceBundle.getString("accounting.description.paidToAccommodation");
	String paidToCustomer = resourceBundle.getString("accounting.description.paidToCustomer");
	String paidFromAccommodation = resourceBundle.getString("accounting.description.paidFromAccommodation");
	String open = resourceBundle.getString("bookings.status.OPEN");
	String paid = resourceBundle.getString("bookings.status.PAID");
	String unpaid = resourceBundle.getString("bookings.status.UNPAID");
	String cancelled = resourceBundle.getString("bookings.status.CANCELLED");
	String deleted = resourceBundle.getString("bookings.deleted");

	@Test
	public void constructorParamNontNullBookingManagementTest() {
		assertThrows(NullPointerException.class, () -> {
			new BookingManagement(null, mock(AccountingManagement.class), businessTime);
		});
		assertThrows(NullPointerException.class, () -> {
			new BookingManagement(mock(BookingRepository.class), null, businessTime);
		});
	}

	@Test
	public void payTest() {

		var startDate1 = LocalDate.of(2020, 6, 11);
		var endDate1 = LocalDate.of(2020, 6, 12);
		var paymentMethod1 = PaymentMethod.TRANSFER;
		var paymentMethod2 = PaymentMethod.CASH;

		List<Accommodation> accommodationList = (List<Accommodation>) accommodationManagement.findAll();
		var accommodation = accommodationList.get(0);
		List<Customer> customerList = (List<Customer>) customerManagement.findAllCustomers();
		var customer = customerList.get(0);

		var booking1 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod1,
				Optional.empty());

		bookingManagement.pay(booking1);
		assertEquals(BookingStatus.PAID, bookingManagement.findById(booking1.getId()).get().getStatus());

		// test if invoices were created
		String descriptionOfInvoice1 = bookingId + ": " + booking1.getId() + ", " + open + " → " + paid + ", "
				+ paidFromCustomer;
		String descriptionOfInvoice2 = bookingId + ": " + booking1.getId() + ", " + open + " → " + paid + ", "
				+ paidToAccommodation;
		double expectedValueOfInvoice1 = booking1.getPrice();
		double expectedValueOfInvoice2 = booking1.getPrice() * (1 - booking1.getAccommodation().getProvision()) * -1d;
		Invoice invoice1 = invoiceRepository.findByDescription(descriptionOfInvoice1).get(0);
		Invoice invoice2 = invoiceRepository.findByDescription(descriptionOfInvoice2).get(0);
		assertEquals(expectedValueOfInvoice1, invoice1.getValue());
		assertEquals(expectedValueOfInvoice2, invoice2.getValue());
		assertThat(booking1.getInvoicesOfBooking()).asList().contains(invoice1);

		// test if invoices were not created, because it was already paid
		var booking2 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod2,
				Optional.empty());

		bookingManagement.pay(booking2);

		String descriptionOfInvoice3 = bookingId + ": " + booking2.getId() + ", " + open + " → " + paid + ", "
				+ paidFromCustomer;
		String descriptionOfInvoice4 = bookingId + ": " + booking2.getId() + ", " + open + " → " + paid + ", "
				+ paidToAccommodation;
		assertThat(invoiceRepository.findByDescription(descriptionOfInvoice3)).isEmpty();
		assertThat(invoiceRepository.findByDescription(descriptionOfInvoice4)).isEmpty();

		var booking3 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod1,
				Optional.empty());
		booking3.setStatus(BookingStatus.UNPAID);
		bookingRepository.save(booking3);

		assertEquals(BookingStatus.UNPAID, booking3.getStatus());

		bookingManagement.pay(booking3);

		assertEquals(BookingStatus.PAID, booking3.getStatus());
	}

	@Test
	public void cancelBookingOpen30Test() {

		var startDate1 = LocalDate.of(2020, 6, 15);
		var endDate1 = LocalDate.of(2020, 6, 16);
		var paymentMethod1 = PaymentMethod.TRANSFER;

		List<Accommodation> accommodationList = (List<Accommodation>) accommodationManagement.findAll();
		var accommodation = accommodationList.get(0);
		List<Customer> customerList = (List<Customer>) customerManagement.findAllCustomers();
		var customer = customerList.get(0);

		var booking1 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod1,
				Optional.empty());

		bookingManagement.cancel(booking1);

		assertEquals(BookingStatus.CANCELLED, bookingManagement.findById(booking1.getId()).get().getStatus());

		// test if invoices were created
		String descriptionOfInvoice1 = bookingId + ": " + booking1.getId() + ", " + open + " → " + cancelled + " 30%, "
				+ paidFromCustomer;
		String descriptionOfInvoice2 = bookingId + ": " + booking1.getId() + ", " + open + " → " + cancelled + " 30%, "
				+ paidToAccommodation;
		double expectedValueOfInvoice1 = booking1.getPrice() * 0.3d;
		double expectedValueOfInvoice2 = booking1.getPrice() * (1 - booking1.getAccommodation().getProvision()) * -0.3d;
		Invoice invoice1 = invoiceRepository.findByDescription(descriptionOfInvoice1).get(0);
		Invoice invoice2 = invoiceRepository.findByDescription(descriptionOfInvoice2).get(0);
		assertEquals(expectedValueOfInvoice1, invoice1.getValue());
		assertEquals(expectedValueOfInvoice2, invoice2.getValue());
	}

	@Test
	public void cancelBookingPaid30Test() {

		var startDate2 = LocalDate.of(2020, 6, 13);
		var endDate2 = LocalDate.of(2020, 6, 14);
		var paymentMethod2 = PaymentMethod.CASH;

		List<Accommodation> accommodationList = (List<Accommodation>) accommodationManagement.findAll();
		var accommodation = accommodationList.get(0);
		List<Customer> customerList = (List<Customer>) customerManagement.findAllCustomers();
		var customer = customerList.get(0);

		var booking2 = bookingManagement.createBooking(startDate2, endDate2, accommodation, customer, paymentMethod2,
				Optional.empty());

		bookingManagement.cancel(booking2);

		assertEquals(BookingStatus.CANCELLED, bookingManagement.findById(booking2.getId()).get().getStatus());

		// test if invoices were created
		String descriptionOfInvoice1 = bookingId + ": " + booking2.getId() + ", " + paid + " → " + cancelled + " 30%, "
				+ paidToCustomer;
		String descriptionOfInvoice2 = bookingId + ": " + booking2.getId() + ", " + paid + " → " + cancelled + " 30%, "
				+ paidFromAccommodation;
		double expectedValueOfInvoice1 = booking2.getPrice() * -0.7d;
		double expectedValueOfInvoice2 = booking2.getPrice() * (1 - booking2.getAccommodation().getProvision()) * 0.7d;
		Invoice invoice1 = invoiceRepository.findByDescription(descriptionOfInvoice1).get(0);
		Invoice invoice2 = invoiceRepository.findByDescription(descriptionOfInvoice2).get(0);
		assertEquals(expectedValueOfInvoice1, invoice1.getValue());
		assertEquals(expectedValueOfInvoice2, invoice2.getValue());
	}

	@Test
	public void cancelBookingOpen100Test() {

		var startDate3 = businessTime.getTime().toLocalDate().plusDays(1);
		var endDate3 = businessTime.getTime().toLocalDate().plusDays(2);
		var paymentMethod1 = PaymentMethod.TRANSFER;

		List<Accommodation> accommodationList = (List<Accommodation>) accommodationManagement.findAll();
		var accommodation = accommodationList.get(0);
		List<Customer> customerList = (List<Customer>) customerManagement.findAllCustomers();
		var customer = customerList.get(0);

		var booking3 = bookingManagement.createBooking(startDate3, endDate3, accommodation, customer, paymentMethod1,
				Optional.empty());

		bookingManagement.cancel(booking3);

		assertEquals(BookingStatus.CANCELLED, bookingManagement.findById(booking3.getId()).get().getStatus());

		// test if invoices were created
		String descriptionOfInvoice1 = bookingId + ": " + booking3.getId() + ", " + open + " → " + cancelled + " 100%, "
				+ paidFromCustomer;
		String descriptionOfInvoice2 = bookingId + ": " + booking3.getId() + ", " + open + " → " + cancelled + " 100%, "
				+ paidToAccommodation;
		double expectedValueOfInvoice1 = booking3.getPrice();
		double expectedValueOfInvoice2 = booking3.getPrice() * (1 - booking3.getAccommodation().getProvision()) * -1d;
		Invoice invoice1 = invoiceRepository.findByDescription(descriptionOfInvoice1).get(0);
		Invoice invoice2 = invoiceRepository.findByDescription(descriptionOfInvoice2).get(0);
		assertEquals(expectedValueOfInvoice1, invoice1.getValue());
		assertEquals(expectedValueOfInvoice2, invoice2.getValue());
	}

	@Test
	public void cancelBookingPaid100Test() {

		var startDate4 = businessTime.getTime().toLocalDate().plusDays(3);
		var endDate4 = businessTime.getTime().toLocalDate().plusDays(4);
		var paymentMethod2 = PaymentMethod.CASH;

		List<Accommodation> accommodationList = (List<Accommodation>) accommodationManagement.findAll();
		var accommodation = accommodationList.get(0);
		List<Customer> customerList = (List<Customer>) customerManagement.findAllCustomers();
		var customer = customerList.get(0);

		var booking4 = bookingManagement.createBooking(startDate4, endDate4, accommodation, customer, paymentMethod2,
				Optional.empty());

		bookingManagement.cancel(booking4);
		assertEquals(BookingStatus.CANCELLED, bookingManagement.findById(booking4.getId()).get().getStatus());

		// test if invoices were not created
		String descriptionOfInvoice1 = bookingId + ": " + booking4.getId() + ", " + paid + " → " + cancelled + "100%, "
				+ paidToCustomer;
		String descriptionOfInvoice2 = bookingId + ": " + booking4.getId() + ", " + paid + " → " + cancelled + "100%, "
				+ paidFromAccommodation;
		assertThat(invoiceRepository.findByDescription(descriptionOfInvoice1)).isEmpty();
		assertThat(invoiceRepository.findByDescription(descriptionOfInvoice2)).isEmpty();
	}

	@Test
	public void deleteTest() {

		var startDate1 = LocalDate.of(2020, 6, 11);
		var endDate1 = LocalDate.of(2020, 6, 12);
		List<Accommodation> accommodationList = (List<Accommodation>) accommodationManagement.findAll();
		var accommodation = accommodationList.get(0);
		List<Customer> customerList = (List<Customer>) customerManagement.findAllCustomers();
		var customer = customerList.get(0);
		var paymentMethod1 = PaymentMethod.CASH;
		var booking1 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod1,
				Optional.empty());
		var booking1Id = booking1.getId();

		String descriptionOfInvoice1 = bookingId + ": " + booking1Id + ", " + paid + " → " + deleted + ", "
				+ paidToCustomer;
		String descriptionOfInvoice2 = bookingId + ": " + booking1Id + ", " + paid + " → " + deleted + ", "
				+ paidFromAccommodation;
		double expectedValueOfInvoice1 = -booking1.getPrice();

		// test if booking is deleted
		assertTrue(bookingManagement.delete(booking1));
		assertTrue(bookingManagement.findById(booking1Id).isEmpty());

		// test if invoices were created
		Invoice invoice1 = invoiceRepository.findByDescription(descriptionOfInvoice1).get(0);
		Invoice invoice2 = invoiceRepository.findByDescription(descriptionOfInvoice2).get(0);
		assertEquals(expectedValueOfInvoice1, invoice1.getValue());
		assertTrue(invoice2.isRevenue());

		// changed bookings can't be deleted
		var paymentMethod2 = PaymentMethod.TRANSFER;
		var booking2 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer, paymentMethod2,
				Optional.empty());
		var booking2Id = booking2.getId();

		bookingManagement.pay(booking2);
		assertFalse(bookingManagement.delete(booking2));
		assertTrue(bookingManagement.findById(booking2Id).isPresent());
	}

	@Test
	public void openBookingHandlerTest() {

		var paymentMethod1 = PaymentMethod.TRANSFER;
		List<Accommodation> accommodationList = (List<Accommodation>) accommodationManagement.findAll();
		var accommodation = accommodationList.get(0);
		List<Customer> customerList = (List<Customer>) customerManagement.findAllCustomers();

		// booking to send a reminder
		var customer1 = customerList.get(0);
		var startDate1 = businessTime.getTime().toLocalDate().plusDays(3);
		var endDate1 = businessTime.getTime().toLocalDate().plusDays(4);
		var booking1 = bookingManagement.createBooking(startDate1, endDate1, accommodation, customer1, paymentMethod1,
				Optional.empty());

		// booking to be set to UNPAID
		var customer2 = customerList.get(1);
		var startDate2 = businessTime.getTime().toLocalDate().plusDays(-1);
		var endDate2 = businessTime.getTime().toLocalDate().plusDays(0);
		var booking2 = bookingManagement.createBooking(startDate2, endDate2, accommodation, customer2, paymentMethod1,
				Optional.empty());

		bookingManagement.openBookingHandler();

		assertThat(bookingManagement.mailSender().getSentMessages()).isNotEmpty();
		Map<String, SimpleMailMessage> sentMailsMap = new HashMap<>();
		bookingManagement.mailSender().getSentMessages().forEach(m -> sentMailsMap.put(m.getTo()[0], m));
		assertThat(sentMailsMap).isNotEmpty();

		// check if reminder Mail was sent
		assertThat(sentMailsMap.get(customer1.getEmail()).getText()).contains(" " + booking1.getId(),
				"EUR " + String.format("%.2f", booking1.getPrice()));

		// check status set to unpaid and if Mail was sent
		assertEquals(BookingStatus.UNPAID, bookingManagement.findById(booking2.getId()).get().getStatus());
		assertThat(sentMailsMap.get(customer2.getEmail()).getText()).contains(" " + booking2.getId(),
				"EUR " + String.format("%.2f", booking2.getPrice()));
	}
}
