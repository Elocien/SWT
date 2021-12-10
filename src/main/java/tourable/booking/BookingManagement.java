package tourable.booking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.salespointframework.support.RecordingMailSender;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tourable.accommodation.Accommodation;
import tourable.accounting.AccountingManagement;
import tourable.accounting.TransactionCategory;
import tourable.customer.Customer;
import tourable.travelguide.Travelguide;

/**
 * Service class that handles business logic of bookings
 * 
 * @author Pascal Juppe
 * @author Florian Richter
 */
@Service
@Transactional
public class BookingManagement {

	private final BookingRepository bookings;
	private final AccountingManagement accountingManagement;
	private final RecordingMailSender mailSender;
	private final BusinessTime businessTime;

	private final ResourceBundle resourceBundle;
	private final String bookingId;
	private final String paidToCustomer;
	private final String paidFromCustomer;
	private final String paidToAccommodation;
	private final String paidFromAccommodation;
	private final String paid;
	private final String unpaid;
	private final String open;
	private final String cancelled;
	private final String deleted;

	public BookingManagement(BookingRepository bookings, AccountingManagement accountingManagement,
			BusinessTime businessTime) {
		this.bookings = Objects.requireNonNull(bookings);
		this.accountingManagement = Objects.requireNonNull(accountingManagement);
		this.businessTime = Objects.requireNonNull(businessTime);
		this.mailSender = new RecordingMailSender();

		this.resourceBundle = ResourceBundle.getBundle("messages", LocaleContextHolder.getLocale());
		this.bookingId = this.resourceBundle.getString("bookings.id.short");
		this.paidToCustomer = this.resourceBundle.getString("accounting.description.paidToCustomer");
		this.paidFromCustomer = this.resourceBundle.getString("accounting.description.paidFromCustomer");
		this.paidToAccommodation = this.resourceBundle.getString("accounting.description.paidToAccommodation");
		this.paidFromAccommodation = this.resourceBundle.getString("accounting.description.paidFromAccommodation");
		this.paid = this.resourceBundle.getString("bookings.status.PAID");
		this.open = this.resourceBundle.getString("bookings.status.OPEN");
		this.cancelled = this.resourceBundle.getString("bookings.status.CANCELLED");
		this.unpaid = this.resourceBundle.getString("bookings.status.UNPAID");
		this.deleted = this.resourceBundle.getString("bookings.deleted");

	}

	public RecordingMailSender mailSender() {
		return this.mailSender;
	}

	/**
	 * creates a new booking and saves it to the database
	 * 
	 * @param startDate     the travel start date - may overlap with the end date of
	 *                      another booking
	 * @param endDate       the travel end date - may overlap with the start date of
	 *                      another booking
	 * @param accommodation the booked accommodation
	 * @param customer      the customer that booked the trip
	 * @param paymentMethod the used payment method
	 * @param travelguide   an {@linkplain Optional} travel guide that is sold
	 *                      concurrently}
	 * @return the new booking
	 */
	public Booking createBooking(LocalDate startDate, LocalDate endDate, Accommodation accommodation, Customer customer,
			PaymentMethod paymentMethod, Optional<Travelguide> travelguide) {

		var startTime = Objects.requireNonNull(startDate).atTime(12, 0, 1);
		var endTime = Objects.requireNonNull(endDate).atTime(11, 59, 59);
		var acm = Objects.requireNonNull(accommodation);
		var cst = Objects.requireNonNull(customer);
		var method = Objects.requireNonNull(paymentMethod);

		var price = acm.getPrice() * ChronoUnit.DAYS.between(startDate, endDate);
		var status = (method == PaymentMethod.CASH) ? BookingStatus.PAID : BookingStatus.OPEN;

		if (travelguide.isPresent()) {
			accountingManagement.sellTravelguide(travelguide.get());
		}

		var booking = bookings.save(new Booking(startTime, endTime, price, method, status, cst, acm, businessTime));

		customer.setLastBookingDate(booking.getTimeOfBooking());
		if (method == PaymentMethod.CASH) {
			var provision = (-1) * price * (1 - acm.getProvision());
			booking.addInvoiceToBooking(accountingManagement.createInvoice(price,
					bookingId + ": " + booking.getId() + ", " + paid + ", " + paidFromCustomer,
					TransactionCategory.BOOKING));
			accountingManagement.createInvoice(provision,
					bookingId + ": " + booking.getId() + ", " + paid + ", " + paidToAccommodation,
					TransactionCategory.BOOKING);
			bookings.save(booking);
		}

		return booking;
	}

	/**
	 * turns state of booking from OPEN / UNPAID to PAID and creates appropriate
	 * {@linkplain tourable.accounting.Invoice Invoice}s
	 * 
	 * @param booking which was paid
	 */
	public void pay(Booking booking) {
		var status = booking.getStatus();
		LocalDate start = booking.getStart().toLocalDate();
		var price = booking.getPrice();
		var priceProv = (-1) * price * (1 - booking.getAccommodation().getProvision());
		if (status.equals(BookingStatus.OPEN) && !start.isBefore(businessTime.getTime().toLocalDate())) {
			// Revenue
			booking.addInvoiceToBooking(accountingManagement.createInvoice(price,
					bookingId + ": " + booking.getId() + ", " + open + " → " + paid + ", " + paidFromCustomer,
					TransactionCategory.BOOKING));
			// Expense
			accountingManagement.createInvoice(priceProv,
					bookingId + ": " + booking.getId() + ", " + open + " → " + paid + ", " + paidToAccommodation,
					TransactionCategory.BOOKING);
			booking.setStatus(BookingStatus.PAID);
		} else if (status.equals(BookingStatus.UNPAID)) {
			// Revenue
			booking.addInvoiceToBooking(accountingManagement.createInvoice(price,
					bookingId + ": " + booking.getId() + ", " + unpaid + " → " + paid + ", " + paidFromCustomer,
					TransactionCategory.BOOKING));
			// Expense
			accountingManagement.createInvoice(priceProv,
					bookingId + ": " + booking.getId() + ", " + unpaid + " → " + paid + ", " + paidToAccommodation,
					TransactionCategory.BOOKING);
			booking.setStatus(BookingStatus.PAID);
		}
		bookings.save(booking);
	}

	/**
	 * cancels booking and creates Invoices for cancellation
	 * 
	 * @param booking to cancel
	 */
	public void cancel(Booking booking) {
		var status = booking.getStatus();
		LocalDate start = booking.getStart().toLocalDate();
		if (!businessTime.getTime().toLocalDate().plusDays(7).isAfter(start)) {
			if (status.equals(BookingStatus.OPEN)) {
				var price = booking.getPrice();
				var priceProv = (-1) * price * (1 - booking.getAccommodation().getProvision());

				// Revenue
				booking.addInvoiceToBooking(
						accountingManagement.createInvoice(price * 0.3d, bookingId + ": " + booking.getId() + ", "
								+ open + " → " + cancelled + " 30%, " + paidFromCustomer, TransactionCategory.BOOKING));

				// Expense
				accountingManagement.createInvoice(priceProv * 0.3d, bookingId + ": " + booking.getId() + ", " + open
						+ " → " + cancelled + " 30%, " + paidToAccommodation, TransactionCategory.BOOKING);

				booking.setStatus(BookingStatus.CANCELLED);
				bookings.save(booking);
			} else if (status.equals(BookingStatus.PAID)) {
				var price = booking.getPrice();
				var priceProv = (-1) * price * (1 - booking.getAccommodation().getProvision());

				// Expense
				booking.addInvoiceToBooking(
						accountingManagement.createInvoice(price * -0.7d, bookingId + ": " + booking.getId() + ", "
								+ paid + " → " + cancelled + " 30%, " + paidToCustomer, TransactionCategory.BOOKING));

				// Revenue
				accountingManagement.createInvoice(priceProv * -0.7d, bookingId + ": " + booking.getId() + ", " + paid
						+ " → " + cancelled + " 30%, " + paidFromAccommodation, TransactionCategory.BOOKING);

				booking.setStatus(BookingStatus.CANCELLED);
				bookings.save(booking);
			}
		} else if (businessTime.getTime().toLocalDate().plusDays(7).isAfter(start)
				&& !businessTime.getTime().toLocalDate().isAfter(start)) {
			if (status.equals(BookingStatus.OPEN)) {
				var price = booking.getPrice();
				var priceProv = (-1) * price * (1 - booking.getAccommodation().getProvision());

				// Revenue
				booking.addInvoiceToBooking(accountingManagement.createInvoice(price, bookingId + ": " + booking.getId()
						+ ", " + open + " → " + cancelled + " 100%, " + paidFromCustomer, TransactionCategory.BOOKING));

				// Expense
				accountingManagement.createInvoice(priceProv, bookingId + ": " + booking.getId() + ", " + open + " → "
						+ cancelled + " 100%, " + paidToAccommodation, TransactionCategory.BOOKING);

				booking.setStatus(BookingStatus.CANCELLED);
				bookings.save(booking);
			} else if (status.equals(BookingStatus.PAID)) {
				booking.setStatus(BookingStatus.CANCELLED);
				bookings.save(booking);
			}
		}
	}

	/**
	 * deletes booking, when certain condions are set: - cant delete bookings older
	 * then a day - cant delete processed bookings
	 * 
	 * @param booking to delete
	 * @return true if deleted
	 */
	public boolean delete(Booking booking) {
		// can only be deleted within 1 day, when nothing else beside creation was done
		boolean inTimeFlag = Interval.from(booking.getTimeOfBooking()).to(booking.getTimeOfBooking().plusDays(1))
				.contains(businessTime.getTime());
		boolean openFlag = booking.getStatus().equals(BookingStatus.OPEN);
		boolean paidNotChangedFlag = booking.getStatus().equals(BookingStatus.PAID)
				&& booking.getPaymentMethod().equals(PaymentMethod.CASH);

		if (inTimeFlag && (openFlag || paidNotChangedFlag)) {
			// if booking is already paid, get Money back
			if (booking.getStatus().equals(BookingStatus.PAID)) {
				var price = booking.getPrice();
				var priceProv = (-1) * price * (1 - booking.getAccommodation().getProvision());
				// Expense
				booking.addInvoiceToBooking(accountingManagement.createInvoice(price * -1d,
						bookingId + ": " + booking.getId() + ", " + paid + " → " + deleted + ", " + paidToCustomer,
						TransactionCategory.BOOKING));

				// Revenue
				accountingManagement.createInvoice(priceProv * -1d, bookingId + ": " + booking.getId() + ", " + paid
						+ " → " + deleted + ", " + paidFromAccommodation, TransactionCategory.BOOKING);
				bookings.save(booking);
			}
			bookings.delete(booking);
		}
		return !bookings.findById(booking.getId()).isPresent();
	}

	/**
	 * scheduled method, which sends SimpleMailMessage reminders and updates status
	 * from OPEN to UNPAID
	 */
	@Scheduled(cron = "0 0 8 * * ?")
	public void openBookingHandler() {
		Iterable<Booking> openBookings = bookings.findByStatus(BookingStatus.OPEN);
		for (Booking b : openBookings) {
			if (businessTime.getTime().isAfter(b.getStart())) {
				mailSender.send(unpaid(b));
			} else if (businessTime.getTime().toLocalDate().isEqual((b.getStart().toLocalDate().minusDays(3)))) {
				mailSender.send(reminder(b));
			}
		}
	}

	private SimpleMailMessage reminder(Booking booking) {
		String text1 = resourceBundle.getString("bookings.reminder.text1");
		String text2 = resourceBundle.getString("bookings.reminder.text2");
		String text3 = resourceBundle.getString("bookings.reminder.text3");
		String text4 = resourceBundle.getString("bookings.reminder.text4");
		String text5 = resourceBundle.getString("bookings.reminder.text5");
		String text6 = resourceBundle.getString("bookings.reminder.text6");
		String text7 = resourceBundle.getString("bookings.reminder.text7");

		Customer customer = booking.getCustomer();
		String subject = resourceBundle.getString("bookings.reminder.subject");

		String text = "\n" + text1 + " " + customer.getName() + ", \n" + text2 + " "
				+ booking.getTimeOfBooking().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)) + " " + text3
				+ " " + booking.getId() + " " + text4 + " EUR " + String.format("%.2f", booking.getPrice()) + " "
				+ text5 + " "
				+ booking.getStart().minusHours(4).minusSeconds(1)
						.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT))
				+ " " + text6 + "\n" + text7;

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("info@citytours.com");
		message.setTo(customer.getEmail());
		message.setSubject(subject);
		message.setText(text);

		return message;
	}

	private SimpleMailMessage unpaid(Booking booking) {
		String text1 = resourceBundle.getString("bookings.unpaid.text1");
		String text2 = resourceBundle.getString("bookings.unpaid.text2");
		String text3 = resourceBundle.getString("bookings.unpaid.text3");
		String text4 = resourceBundle.getString("bookings.unpaid.text4");
		String text5 = resourceBundle.getString("bookings.unpaid.text5");
		String text6 = resourceBundle.getString("bookings.unpaid.text6");
		String text7 = resourceBundle.getString("bookings.unpaid.text7");

		Customer customer = booking.getCustomer();
		String subject = resourceBundle.getString("bookings.unpaid.subject") + " " + booking.getId();
		String text = "\n" + text1 + " " + customer.getName() + ", \n" + text2 + " "
				+ booking.getTimeOfBooking().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)) + " " + text3
				+ " " + booking.getId() + " " + text4 + ".\n" + text5 + " EUR "
				+ String.format("%.2f", booking.getPrice()) + " " + text6 + ". \n" + text7;

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("info@citytours.com");
		message.setTo(customer.getEmail());
		message.setSubject(subject);
		message.setText(text);
		booking.setStatus(BookingStatus.UNPAID);
		bookings.save(booking);
		return message;
	}

	/**
	 * @return a {@linkplain Stream} of all bookings
	 */
	public Iterable<Booking> findAll() {
		return bookings.findAll(Sort.by(Sort.Direction.ASC, "status").and(Sort.by(Sort.Direction.DESC, "id")));
	}

	/**
	 * @param id the booking id
	 * @return an {@linkplain Optional} {@linkplain Booking} for the given id
	 */
	public Optional<Booking> findById(Long id) {
		return bookings.findById(id);
	}

	/**
	 * @param status the booking status
	 * @return a {@linkplain Stream} of all bookings
	 */
	public Iterable<Booking> findByStatus(BookingStatus status) {
		return bookings.findByStatus(status, Sort.by(Sort.Direction.DESC, "id"));
	}

	/**
	 * @param customer the given customer
	 * @return the list of {@linkplain Booking}s for a given customer
	 */
	public Iterable<Booking> findByCustomer(Customer customer) {
		return bookings.findByCustomer(customer);
	}

	/**
	 * Returns all intervals of open or paid bookings for a given accommodation
	 * 
	 * @param acm the given accommodation
	 * @return the {@linkplain Stream} of {@linkplain Interval}s
	 */
	public Stream<Interval> getBookingIntervalsForAccommodation(Accommodation acm) {
		return bookings.stream()
				.filter(booking -> booking.getStatus().equals(BookingStatus.PAID)
						|| booking.getStatus().equals(BookingStatus.OPEN))
				.filter(booking -> booking.getAccommodation().equals(acm)).map(booking -> booking.getInterval());
	}

	/**
	 * @param acm the given accommodation
	 * @return whether there are any {@linkplain Booking}s, which need the data of
	 *         the given accommodation
	 */
	public boolean hasOpenBookingsForAccommodation(Accommodation acm) {
		return bookings.stream().filter(booking -> booking.getAccommodation().equals(acm))
				.anyMatch(booking -> booking.getStatus().equals(BookingStatus.OPEN)
						|| (booking.getStatus().equals(BookingStatus.PAID)
								&& businessTime.getTime().isAfter(booking.getEnd())));
	}

	/**
	 * @param customer the given customer
	 * @return whether there are any {@linkplain Booking}s, which need the data of
	 *         the given customer
	 */
	public boolean hasOpenBookingsForCustomer(Customer customer) {
		return bookings.stream().filter(booking -> booking.getCustomer().equals(customer))
				.anyMatch(booking -> booking.getStatus().equals(BookingStatus.OPEN)
						|| (booking.getStatus().equals(BookingStatus.PAID)
								&& businessTime.getTime().isAfter(booking.getStart())));
	}

}
