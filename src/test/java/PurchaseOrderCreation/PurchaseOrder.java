package PurchaseOrderCreation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.testng.annotations.Test;

import Base.BaseClass;
import LocatorsOfWindows.PurchaseOrderLocators;

public class PurchaseOrder extends BaseClass {

	// Dates
	LocalDate today = LocalDate.now();
	LocalDate futuredate = today.plusDays(5);

	// Gregorian Date
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
	String currentDate = today.format(formatter);
	String futureDate = futuredate.format(formatter);

	// Hijri Date
	HijrahDate hijriToday = HijrahChronology.INSTANCE.date(today);
	HijrahDate hijriFutureDate = HijrahChronology.INSTANCE.date(futuredate);
	DateTimeFormatter hijriFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");
	String hijricurrentDate = hijriToday.format(hijriFormatter);
	String hijrifutureDate = hijriFutureDate.format(hijriFormatter);

	String approvalType = System.getProperty("approvalType", "Single");

	@Test(dataProvider = "poData")
	public void purchaseOrderCreation(HashMap<String, String> data) throws InterruptedException, SQLException {

		launchApplication();

		PurchaseOrderLocators PO = new PurchaseOrderLocators(driver, wait, action);

		// Open Window
		if (approvalType.equalsIgnoreCase("Multi")) {
			PO.login(data.get("MultiApprovalRequester"), data.get("password"));
		}

		if (approvalType.equalsIgnoreCase("Single")) {
			PO.login(data.get("SingleApprovalRequester"), data.get("password"));
		}

		PO.openWindow(data.get("poWindowName"));
		PO.createNewHeader();
		PO.maximizeHeader();

		// PO Header Fields
		PO.processType(data.get("processType"));
		PO.referenceNumber(data.get("referenceNum"));
		PO.contractCategory(data.get("contractCategoryName"));
		PO.projectName(data.get("projectDescription"));
		PO.awardNumber(data.get("awardNumber"));
		PO.awardDate(currentDate);
		PO.letterDate(currentDate);
		PO.selectSupplier(data.get("supplierName"));
		PO.city(data.get("cityName"));
		PO.MOFDates(currentDate);

		// Select Unique Code
		if (approvalType.equalsIgnoreCase("Multi")) {
			PO.selectUniqueCode(data.get("MultiApprovalRequesterRoleId"), data.get("accountNumber"));
		}
		if (approvalType.equalsIgnoreCase("Single")) {
			PO.selectUniqueCode(data.get("SingleApprovalRequesterRoleId"), data.get("accountNumber"));
		}

		PO.saveHeader();

		// Attachment
		PO.addAttachment(data.get("attachmentFilePath"));

		// Lines Tab
		PO.navigateToPOLinesTab();
		PO.createNewLine();
		PO.selectProduct(data.get("productCode"));
		PO.enterQuantity(data.get("quantity"));
		PO.enterUnitPrice(data.get("unitPrice"));
		PO.saveLine();

		// Contract Attributes
		double lineNetAmount = PO.getLineNetAmount();
		if (lineNetAmount >= 10000) {
			PO.contractAttributes(hijricurrentDate, hijrifutureDate);
		}

		// Navigate To PO header
		PO.navigateToPOHeader();

		// Submit PO
		PO.submitOrApprove();
		PO.successMessage();

		// Logout
		PO.logout();

		// Approval
		PO.POApproval(data.get("poWindowName"), data.get("accountNumber"));

		// Ledger account
		PO.login(data.get("AccrualUser"), data.get("password"));
		PO.addLedgerAccount(data.get("poWindowName"), data.get("productCode"), data.get("accountNumber"));

		// Get PO Number
		System.out.println(PO.getPoNumber());

		PO.logout();
		driver.close();

	}

}
