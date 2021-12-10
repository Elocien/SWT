package tourable.accounting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tourable.travelguide.Travelguide;

/**
 * {@code AccountingManagement} class is the accounting service.
 * {@code AccountingManagement} aggregates {@link Invoice}s and has methods to
 * return financial statistics and {@link Invoice}s of a certain
 * {@code interval} or or groups thereof in {@link InvoiceEntry}s.
 * 
 * @author Pascal Juppe
 * @author Florian Richter
 *
 */
@Service
@Transactional
public class AccountingManagement {

	private final InvoiceRepository invoices;
	private final BusinessTime businessTime;

	public AccountingManagement(InvoiceRepository invoices, BusinessTime businessTime) {
		this.invoices = Objects.requireNonNull(invoices);
		this.businessTime = Objects.requireNonNull(businessTime);
	}

	/**
	 * Creates {@link Invoice} with input parameters and saves it persistent.
	 * 
	 * @param value    the value of the invoice (negative for Expense, positive for
	 *                 Income)
	 * @param desc     a description of {@link Invoice}.
	 * @param category the {@link TransactionCategory} of the {@link Invoice}
	 * @return Returns the created {@link Invoice}.
	 */
	public Invoice createInvoice(double value, String desc, TransactionCategory category) {
		return invoices.save(new Invoice(value, desc, category, businessTime));
	}

	/**
	 * Saves the {@link Invoice}.
	 * 
	 * @param invoice the invoice to add
	 * @return Returns the added {@link Invoice}.
	 */
	public Invoice add(Invoice invoice) {
		Objects.requireNonNull(invoice);
		return invoices.save(invoice);
	}

	/**
	 * Creates {@link Invoice} of sold {@link Travelguide}.
	 * 
	 * @param travelguide the travel guide so sell
	 * @return Returns {@link Invoice} from sold {@link Travelguide}
	 */
	public Invoice sellTravelguide(Travelguide travelguide) {
		var value = travelguide.getPrice();
		var desc = travelguide.getName();
		return createInvoice(value, desc, TransactionCategory.TRAVELGUIDE_SALE);
	}

	/**
	 * Returns a statistic of the input interval as a {@code List} of
	 * {@link InvoiceEntry}s.
	 * 
	 * @param from from date
	 * @param to   to date
	 * @return a statistic of the input interval as a {@code List} of
	 *         {@link InvoiceEntry}s
	 */
	public List<InvoiceEntry> getStatistics(String from, String to) {
		LocalDate fromLD = LocalDate.parse(from);
		LocalDate toLD = LocalDate.parse(to);
		Interval interval = Interval.from(fromLD.atTime(0, 0, 0)).to(toLD.atTime(23, 59, 59));

		List<InvoiceEntry> statistics = new ArrayList<>();
		for (TransactionCategory cat : TransactionCategory.values()) {
			String name = cat.toString();
			double revenue = getInvoicesInInterval(interval).filter(inv -> inv.getTransactionCategory() == cat)
					.filter(Invoice::isRevenue).mapToDouble(Invoice::getValue).sum();
			double expense = getInvoicesInInterval(interval).filter(inv -> inv.getTransactionCategory() == cat)
					.filter(Invoice::isExpense).mapToDouble(Invoice::getValue).sum();
			statistics.add(new InvoiceEntry(name, revenue, expense, fromLD, toLD));
		}
		double totalRevenue = getTotalIncomeInInterval(interval);
		double totalExpense = getTotalExpenseInInterval(interval);
		statistics.add(new InvoiceEntry("Total", totalRevenue, totalExpense, fromLD, toLD));

		return statistics;
	}

	/**
	 * Returns all {@link Invoice}s of the input interval.
	 * 
	 * @param from all returned {@link Invoice}s will have a time stamp after
	 *             {@code from}
	 * @param to   all returned {@link Invoice}s will have a time stamp before
	 *             {@code from}
	 * @return a {@code List} of {@link Invoice}s within input interval.
	 */
	public List<Invoice> getInvoiceList(String from, String to) {

		LocalDate fromDate = LocalDate.parse(from);
		LocalDate toDate = LocalDate.parse(to);

		Interval interval = Interval.from(fromDate.atTime(0, 0, 0)).to(toDate.atTime(23, 59, 59));
		return getInvoicesInInterval(interval).collect(Collectors.toList());
	}

	/**
	 * @return a {@code List} of objects of type {@link InvoiceEntry}, each with the
	 *         financial statistics of a year, for the last 7 years
	 */
	public List<InvoiceEntry> getAnnualAccountingEntries() {

		List<InvoiceEntry> entries = new ArrayList<>();
		LocalDate end = businessTime.getTime().toLocalDate();
		LocalDate start = end.minusYears(7);
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy");

		for (LocalDate date = end; date.isAfter(start); date = date.minusYears(1)) {
			String name = date.format(format);
			LocalDate startOfYear = date.withDayOfYear(1);
			LocalDate endOfYear = date.withDayOfYear(date.lengthOfYear());
			double totalIncome = getTotalIncomeInInterval(
					Interval.from(startOfYear.atTime(0, 0, 0)).to(endOfYear.atTime(23, 59, 59)));
			double totalExpense = getTotalExpenseInInterval(
					Interval.from(startOfYear.atTime(0, 0, 0)).to(endOfYear.atTime(23, 59, 59)));
			entries.add(new InvoiceEntry(name, totalIncome, totalExpense, startOfYear, endOfYear));
		}

		return entries;
	}

	/**
	 * @return a {@code List} of objects of type {@link InvoiceEntry}, each with the
	 *         financial statistics of a month, for the last 12 months
	 */
	public List<InvoiceEntry> getMonthlyAccountingEntries() {

		List<InvoiceEntry> entries = new ArrayList<>();
		LocalDate end = businessTime.getTime().toLocalDate();
		LocalDate start = end.minusMonths(12);
		DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM yyyy");

		for (LocalDate date = end; date.isAfter(start); date = date.minusMonths(1)) {
			String name = date.format(format);
			LocalDate startOfMonth = date.withDayOfMonth(1);
			LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());
			double totalIncome = getTotalIncomeInInterval(
					Interval.from(startOfMonth.atTime(0, 0, 0)).to(endOfMonth.atTime(23, 59, 59)));
			double totalExpense = getTotalExpenseInInterval(
					Interval.from(startOfMonth.atTime(0, 0, 0)).to(endOfMonth.atTime(23, 59, 59)));
			entries.add(new InvoiceEntry(name, totalIncome, totalExpense, startOfMonth, endOfMonth));
		}

		return entries;
	}

	/**
	 * @return a {@code List} of objects of type {@link InvoiceEntry}, each with the
	 *         financial statistics of a day, for the last 30 days
	 */
	public List<InvoiceEntry> getDailyAccountingEntries() {

		List<InvoiceEntry> entries = new ArrayList<>();
		LocalDate end = businessTime.getTime().toLocalDate();
		LocalDate start = end.minusDays(30);
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd. MMM yyyy");

		for (LocalDate date = end; date.isAfter(start); date = date.minusDays(1)) {
			String name = date.format(format);
			double totalIncome = getTotalIncomeInInterval(
					Interval.from(date.atTime(0, 0, 0)).to(date.atTime(23, 59, 59)));
			double totalExpense = getTotalExpenseInInterval(
					Interval.from(date.atTime(0, 0, 0)).to(date.atTime(23, 59, 59)));
			entries.add(new InvoiceEntry(name, totalIncome, totalExpense, date, date));
		}

		return entries;
	}

	private Stream<Invoice> getInvoicesInInterval(Interval interval) {
		return invoices.stream().filter(invoice -> interval.contains(invoice.getDate()));
	}

	private double getTotalIncomeInInterval(Interval interval) {
		return getInvoicesInInterval(interval).filter(Invoice::isRevenue).mapToDouble(Invoice::getValue).sum();
	}

	private double getTotalExpenseInInterval(Interval interval) {
		return getInvoicesInInterval(interval).filter(Invoice::isExpense).mapToDouble(Invoice::getValue).sum();
	}

	/**
	 * Returns the first created {@link Invoice} of a certain {@code category} after
	 * a certain {@code date}.
	 * 
	 * @param category {@link TransactionCategory} to search
	 * @param date     {@code LocalDateTime} after which the returned invoice was
	 *                 created
	 * @return the first created {@link Invoice} of a certain {@code category} after
	 *         a certain {@code date}.
	 */
	public List<Invoice> findByTransactionCategoryAndDateAfter(TransactionCategory category, LocalDateTime date) {
		return invoices.findByTransactionCategoryAndDateAfter(category, date);
	}

}
