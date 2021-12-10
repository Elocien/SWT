package tourable.accounting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;

import tourable.AbstractIntegrationTest;
import tourable.city.City;
import tourable.travelguide.Travelguide;

public class AccountingManagementTest extends AbstractIntegrationTest {

	@Autowired
	InvoiceRepository invoiceRepository;

	@Autowired
	BusinessTime businessTime;

	@Autowired
	AccountingManagement accountingManagement;

	@Test
	public void constructorParamNullTest() {
		assertThrows(NullPointerException.class, () -> {
			new AccountingManagement(null, mock(BusinessTime.class));
		});
		assertThrows(NullPointerException.class, () -> {
			new AccountingManagement(mock(InvoiceRepository.class), null);
		});
	}

	@Test
	public void createInvoiceTest() {
		var val = 200d;
		var desc = "desc";
		var cat = TransactionCategory.TRAVELGUIDE_SALE;
		Invoice inv = accountingManagement.createInvoice(val, desc, cat);

		assertTrue(invoiceRepository.findById(inv.getId()).isPresent());
		invoiceRepository.delete(inv);
	}

	@Test
	public void addTest() {
		assertThrows(NullPointerException.class, () -> {
			accountingManagement.add(null);
		});

		var val = 200d;
		var desc = "desc";
		var cat = TransactionCategory.TRAVELGUIDE_SALE;
		Invoice inv = accountingManagement.add(new Invoice(val, desc, cat, businessTime));

		assertTrue(invoiceRepository.findById(inv.getId()).isPresent());
		invoiceRepository.delete(inv);
	}

	@Test
	public void sellTravelguideTest() {
		var name = "Travelguide";
		var price = 2.3d;
		Travelguide tg = new Travelguide(name, mock(City.class), price);
		Invoice inv = accountingManagement.sellTravelguide(tg);

		assertEquals(name, inv.getDescription());
		assertEquals(price, inv.getValue());
		assertEquals(TransactionCategory.TRAVELGUIDE_SALE, inv.getTransactionCategory());
		assertTrue(invoiceRepository.findById(inv.getId()).isPresent());
		invoiceRepository.delete(inv);
	}

	@Test
	public void getInvoiceListTest() {
		var from = "2019-11-11";
		var to = "2019-11-11";
		businessTime.reset();
		businessTime.forward(Duration.between(LocalDateTime.now(), LocalDateTime.of(2019, 11, 11, 11, 11)));

		Invoice inv1 = invoiceRepository
				.save(new Invoice(-10000d, "Test Salary", TransactionCategory.SALARY, businessTime));
		Invoice inv2 = invoiceRepository
				.save(new Invoice(39.99d, "Travelguide Test Sale", TransactionCategory.TRAVELGUIDE_SALE, businessTime));

		assertThat(accountingManagement.getInvoiceList(from, to)).hasSize(2);

		invoiceRepository.delete(inv1);
		invoiceRepository.delete(inv2);
		businessTime.reset();
	}

	@Test
	public void getAnnualAccountingEntriesTest() {
		businessTime.reset();
		businessTime.forward(Duration.between(LocalDateTime.now(), LocalDateTime.of(2018, 11, 11, 11, 11)));

		Invoice inv1 = invoiceRepository
				.save(new Invoice(-10000d, "Test Salary", TransactionCategory.SALARY, businessTime));
		Invoice inv2 = invoiceRepository
				.save(new Invoice(39.99d, "Travelguide Test Sale", TransactionCategory.TRAVELGUIDE_SALE, businessTime));
		Invoice inv3 = invoiceRepository
				.save(new Invoice(-800d, "Test Booking", TransactionCategory.BOOKING, businessTime));
		Invoice inv4 = invoiceRepository
				.save(new Invoice(1000d, "Test Booking", TransactionCategory.BOOKING, businessTime));

		businessTime.forward(Duration.ofDays(365));
		List<InvoiceEntry> list = accountingManagement.getAnnualAccountingEntries();

		assertThat(list).hasSize(7);
		assertEquals(39.99d + 1000d, list.get(1).getTotalIncome());
		assertEquals(-10000d - 800d, list.get(1).getTotalExpense());

		invoiceRepository.delete(inv1);
		invoiceRepository.delete(inv2);
		invoiceRepository.delete(inv3);
		invoiceRepository.delete(inv4);
		businessTime.reset();
	}

	@Test
	public void getMonthlyAccountingEntriesTest() {
		businessTime.reset();
		businessTime.forward(Duration.between(LocalDateTime.now(), LocalDateTime.of(2018, 11, 11, 11, 11)));

		Invoice inv1 = invoiceRepository
				.save(new Invoice(-10000d, "Test Salary", TransactionCategory.SALARY, businessTime));
		Invoice inv2 = invoiceRepository
				.save(new Invoice(39.99d, "Travelguide Test Sale", TransactionCategory.TRAVELGUIDE_SALE, businessTime));
		Invoice inv3 = invoiceRepository
				.save(new Invoice(-800d, "Test Booking", TransactionCategory.BOOKING, businessTime));
		Invoice inv4 = invoiceRepository
				.save(new Invoice(1000d, "Test Booking", TransactionCategory.BOOKING, businessTime));

		businessTime.forward(Duration.ofDays(20));
		List<InvoiceEntry> list = accountingManagement.getMonthlyAccountingEntries();

		assertThat(list).hasSize(12);
		assertEquals(39.99d + 1000d, list.get(1).getTotalIncome());
		assertEquals(-10000d - 800d, list.get(1).getTotalExpense());

		invoiceRepository.delete(inv1);
		invoiceRepository.delete(inv2);
		invoiceRepository.delete(inv3);
		invoiceRepository.delete(inv4);
		businessTime.reset();
	}

	@Test
	public void getDailyAccountingEntriesTest() {
		businessTime.reset();
		businessTime.forward(Duration.between(LocalDateTime.now(), LocalDateTime.of(2018, 11, 11, 11, 11)));

		Invoice inv1 = invoiceRepository
				.save(new Invoice(-10000d, "Test Salary", TransactionCategory.SALARY, businessTime));
		Invoice inv2 = invoiceRepository
				.save(new Invoice(39.99d, "Travelguide Test Sale", TransactionCategory.TRAVELGUIDE_SALE, businessTime));
		Invoice inv3 = invoiceRepository
				.save(new Invoice(-800d, "Test Booking", TransactionCategory.BOOKING, businessTime));
		Invoice inv4 = invoiceRepository
				.save(new Invoice(1000d, "Test Booking", TransactionCategory.BOOKING, businessTime));

		businessTime.forward(Duration.ofDays(1));
		List<InvoiceEntry> list = accountingManagement.getDailyAccountingEntries();

		assertThat(list).hasSize(30);
		assertEquals(39.99d + 1000d, list.get(1).getTotalIncome());
		assertEquals(-10000d - 800d, list.get(1).getTotalExpense());

		invoiceRepository.delete(inv1);
		invoiceRepository.delete(inv2);
		invoiceRepository.delete(inv3);
		invoiceRepository.delete(inv4);
		businessTime.reset();
	}

	@Test
	public void getStatisticsTest() {
		businessTime.reset();
		businessTime.forward(Duration.between(LocalDateTime.now(), LocalDateTime.of(2018, 11, 11, 11, 11)));

		Invoice inv1 = invoiceRepository
				.save(new Invoice(-10000d, "Test Salary", TransactionCategory.SALARY, businessTime));
		Invoice inv2 = invoiceRepository
				.save(new Invoice(39.99d, "Travelguide Test Sale", TransactionCategory.TRAVELGUIDE_SALE, businessTime));
		Invoice inv3 = invoiceRepository
				.save(new Invoice(-800d, "Test Booking", TransactionCategory.BOOKING, businessTime));
		Invoice inv4 = invoiceRepository
				.save(new Invoice(1000d, "Test Booking", TransactionCategory.BOOKING, businessTime));
		String from = businessTime.getTime().toLocalDate().toString();
		String to = businessTime.getTime().toLocalDate().plusDays(1).toString();

		businessTime.forward(Duration.ofDays(365));

		List<InvoiceEntry> list = accountingManagement.getStatistics(from, to);

		assertThat(list).hasSize(4);
		assertEquals(0d, list.get(0).getTotalIncome());
		assertEquals(-10000d, list.get(0).getTotalExpense());
		assertEquals(1000d, list.get(1).getTotalIncome());
		assertEquals(-800d, list.get(1).getTotalExpense());
		assertEquals(39.99d, list.get(2).getTotalIncome());
		assertEquals(0d, list.get(2).getTotalExpense());
		assertEquals(1000d + 39.99d, list.get(3).getTotalIncome());
		assertEquals(-800d - 10000d, list.get(3).getTotalExpense());

		invoiceRepository.delete(inv1);
		invoiceRepository.delete(inv2);
		invoiceRepository.delete(inv3);
		invoiceRepository.delete(inv4);
		businessTime.reset();
	}

	@Test
	public void findByTransactionCategoryAndDateAfterTest() {
		businessTime.reset();
		businessTime.forward(Duration.ofDays(10));

		Invoice invoice = accountingManagement.createInvoice(20d, "test invoice", TransactionCategory.TRAVELGUIDE_SALE);

		businessTime.forward(Duration.ofDays(2));

		List<Invoice> listInvoice = accountingManagement.findByTransactionCategoryAndDateAfter(
				TransactionCategory.TRAVELGUIDE_SALE, businessTime.getTime().minusDays(3));

		assertThat(listInvoice).hasSize(1);
		assertEquals(invoice, listInvoice.get(0));

		invoiceRepository.delete(invoice);
		businessTime.reset();
	}
}
