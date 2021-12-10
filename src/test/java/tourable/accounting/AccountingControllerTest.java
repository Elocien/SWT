package tourable.accounting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import tourable.AbstractIntegrationTest;

public class AccountingControllerTest extends AbstractIntegrationTest {

	@Autowired
	AccountingController accountingController;

	@Autowired
	AccountingManagement accountingManagement;

	@Test
	public void constructorParamNoTNullTest() {
		assertThrows(NullPointerException.class, () -> {
			new AccountingController(null);
		});
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void accountingTest() {
		ExtendedModelMap model = new ExtendedModelMap();
		var site = accountingController.accounting(model);

		assertEquals("accounting", site);
		assertThat(model.getAttribute("yearEntries")).asList().hasSize(7);
		assertThat(model.getAttribute("monthEntries")).asList().hasSize(12);
		assertThat(model.getAttribute("dayEntries")).asList().hasSize(30);
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void parseDynamicDateRangeTest() {
		String dateRange = "2019-10-29 - 2019-12-03";
		ExtendedModelMap model = new ExtendedModelMap();
		RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

		var site = accountingController.parseDynamicDateRange(model, redirectAttributes, dateRange);

		assertEquals("redirect:../dynamic", site);
		assertEquals("2019-10-29", redirectAttributes.getAttribute("from"));
		assertEquals("2019-12-03", redirectAttributes.getAttribute("to"));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	public void dynamicAccountingTest() {
		var from = "2019-10-30";
		var to = "2020-04-20";
		ExtendedModelMap model = new ExtendedModelMap();

		List<Invoice> invoices = accountingManagement.getInvoiceList(from, to);
		var site = accountingController.dynamicAccounting(model, from, to);

		assertThat(model.get("overviewTable")).asList().hasSize(4);
		assertEquals(invoices, model.getAttribute("invoiceList"));
		assertEquals("accountingdynamic", site);
	}
}
