package tourable.accounting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;

import tourable.AbstractIntegrationTest;

public class InvoiceTest extends AbstractIntegrationTest {

	@Autowired
	BusinessTime businessTime;

	@Test
	public void invoiceTest() {
		var beforeInit = businessTime.getTime();
		var value = 23d;
		var desc = "desc";
		var cat = TransactionCategory.SALARY;
		Invoice inv1 = new Invoice(value, desc, cat, businessTime);
		Invoice inv2 = new Invoice(-value, desc, cat, businessTime);
		var initInterval = Interval.from(beforeInit).to(businessTime.getTime());

		assertEquals(value, inv1.getValue());
		assertEquals(desc, inv1.getDescription());
		assert (initInterval.contains(inv1.getDate()));
		assert (inv1.isRevenue());
		assert (inv2.isExpense());
	}
}
