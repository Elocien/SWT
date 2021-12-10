package tourable.accounting;

import java.time.LocalDate;
import java.util.Objects;

import javax.validation.constraints.NotEmpty;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The {@code AccountingController} class is a {@code Controller}, to handle the
 * front end inputs.
 * 
 * @author Florian Richter
 * @author Pascal Juppe
 *
 */
@Controller
public class AccountingController {

	private final AccountingManagement accountingManagement;

	AccountingController(AccountingManagement accountingManagement) {
		this.accountingManagement = Objects.requireNonNull(accountingManagement);
	}

	/**
	 * Returns the {@code /accounting} page
	 * 
	 * @param model model interface
	 * @return template {@code accounting.html} to parse
	 */
	@GetMapping("/accounting")
	@PreAuthorize("hasRole('BOSS')")
	public String accounting(Model model) {

		model.addAttribute("yearEntries", accountingManagement.getAnnualAccountingEntries());
		model.addAttribute("monthEntries", accountingManagement.getMonthlyAccountingEntries());
		model.addAttribute("dayEntries", accountingManagement.getDailyAccountingEntries());
		return "accounting";
	}

	/**
	 * handles the {@code /accounting/dynamic page}, to show all {@link Invoice}s in
	 * an interval
	 * 
	 * @param model model interface
	 * @param from  from date
	 * @param to    to date
	 * @return template {@code accountingdynamic.html} to parse
	 */
	@GetMapping("/accounting/dynamic")
	@PreAuthorize("hasRole('BOSS')")
	public String dynamicAccounting(Model model, @NotEmpty @RequestParam String from,
			@NotEmpty @RequestParam String to) {

		model.addAttribute("fromDate", LocalDate.parse(from));
		model.addAttribute("toDate", LocalDate.parse(to));
		model.addAttribute("overviewTable", accountingManagement.getStatistics(from, to));
		model.addAttribute("invoiceList", accountingManagement.getInvoiceList(from, to));

		return "accountingdynamic";
	}

	/**
	 * method parses input {@code dateRange} to start and end of the interval
	 * 
	 * @param model              model interface
	 * @param redirectAttributes redirection attributes to use
	 * @param dateRange          input
	 * @return return to redirect to {@code /accounting/dynamic} page
	 */
	@GetMapping("/accounting/dynamic/parse")
	@PreAuthorize("hasRole('BOSS')")
	public String parseDynamicDateRange(Model model, RedirectAttributes redirectAttributes,
			@NotEmpty @RequestParam String dateRange) {

		var dates = dateRange.split(" ");
		redirectAttributes.addAttribute("from", dates[0]);
		redirectAttributes.addAttribute("to", dates[2]);
		return "redirect:../dynamic";
	}
}
