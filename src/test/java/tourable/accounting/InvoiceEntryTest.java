package tourable.accounting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class InvoiceEntryTest {

	@Test
	public void constructorTest() {
		var name = "name";
		LocalDate from = LocalDate.of(2019, 1, 1);
		LocalDate to = LocalDate.of(2019, 1, 31);
		var totalExpense = -1200d;
		var totalIncome = 1500d;
		var totalProfit = totalExpense + totalIncome;

		InvoiceEntry iE = new InvoiceEntry(name, totalIncome, totalExpense, from, to);

		assertEquals(name, iE.getName());
		assertEquals(from, iE.getFrom());
		assertEquals(to, iE.getTo());
		assertEquals(totalExpense, iE.getTotalExpense());
		assertEquals(totalIncome, iE.getTotalIncome());
		assertEquals(totalProfit, iE.getTotalProfit());
	}
}
