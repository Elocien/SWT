package tourable.accounting;

import java.time.LocalDate;

/**
 * Object to hold the {@code totalIncome}, {@code totalExpense} and {@code
 * totalProfits} of a certain interval
 * 
 * @author Pascal Juppe
 *
 */
public class InvoiceEntry {

	private String name;
	private double totalIncome;
	private double totalExpense;
	private double totalProfit;
	private LocalDate from;
	private LocalDate to;

	/**
	 * Constructor of {code InvoiceEntry}
	 * 
	 * @param name         name of {code InvoiceEntry}
	 * @param totalIncome  sum of incomes of {code InvoiceEntry}
	 * @param totalExpense sum of expenses of {code InvoiceEntry}
	 * @param from         start date of the invoices that were summarized
	 * @param to           end date of the invoices were summarized
	 */
	public InvoiceEntry(String name, double totalIncome, double totalExpense, LocalDate from, LocalDate to) {
		this.name = name;
		this.totalIncome = totalIncome;
		this.totalExpense = totalExpense;
		this.totalProfit = totalIncome + totalExpense;
		this.from = from;
		this.to = to;
	}

	public String getName() {
		return this.name;
	}

	public double getTotalIncome() {
		return this.totalIncome;
	}

	public double getTotalExpense() {
		return this.totalExpense;
	}

	public double getTotalProfit() {
		return this.totalProfit;
	}

	public LocalDate getFrom() {
		return this.from;
	}

	public LocalDate getTo() {
		return this.to;
	}

}
