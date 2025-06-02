package ReceiptCreationFromPO;

import java.sql.SQLException;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import LocatorsOfWindows.POReceiptLocators;
import LocatorsOfWindows.PurchaseOrderLocators;
import TestComponents.BaseClass;
import TestComponents.RetryAnalyzer;

public class POReceipt extends BaseClass {
	String contractType = "Qty";
	String txrnType = "Project Receiving";
	String poDocNumber = "";
	String productCode="";
	double receiptAmount=0;
	double receiptQty=0;
	@Test(dataProvider = "poData",retryAnalyzer = RetryAnalyzer.class)
	public void purchaseOrderCreation(HashMap<String, String> data) throws InterruptedException, SQLException {

		launchApplication();
		PurchaseOrderLocators PO = new PurchaseOrderLocators(driver, wait, action);

		// Open Window
		if (poApprovalType.equalsIgnoreCase("Multi")) {
			PO.login(data.get("MultiApprovalRequester"), data.get("password"));
		}

		if (poApprovalType.equalsIgnoreCase("Single")) {
			PO.login(data.get("SingleApprovalRequester"), data.get("password"));
		}

		PO.openWindow(data.get("poWindowName"));
		PO.createNewHeader();
		PO.maximizeHeader();

		// PO Header Fields
		PO.processType(data.get("processType"));
		PO.referenceNumber(data.get("referenceNum"));
		if (contractType.equalsIgnoreCase("Amt")) {
			PO.contractCategory(data.get("AmtcontractCategoryName"));
		} else
			PO.contractCategory(data.get("QtycontractCategoryName"));

		PO.projectName(data.get("projectDescription"));
		PO.awardNumber(data.get("awardNumber"));
		PO.awardDate(currentDate);
		PO.letterDate(currentDate);
		PO.selectSupplier(data.get("supplierName"));
		PO.city(data.get("cityName"));
		PO.MOFDates(currentDate);

		// Select Unique Code
		if (poApprovalType.equalsIgnoreCase("Multi")) {
			PO.selectUniqueCode(data.get("MultiApprovalRequesterRoleId"), data.get("accountNumber"));
		}
		if (poApprovalType.equalsIgnoreCase("Single")) {
			PO.selectUniqueCode(data.get("SingleApprovalRequesterRoleId"), data.get("accountNumber"));
		}

		PO.saveHeader();

		// Attachment
		PO.addAttachment(data.get("attachmentFilePath"));

		// Lines Tab
		PO.navigateToPOLinesTab();
		PO.createNewLine();
		PO.selectProduct(data.get("productCode"));
		productCode = data.get("productCode");
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
		String actualMessage = PO.submitMessage(PO.getPoNumber());
		Assert.assertTrue(actualMessage.equalsIgnoreCase("Success"), "Expected message 'Success' but got: " + actualMessage);
		// Logout
		PO.logout();

		// Approval
		PO.POApproval(data.get("poWindowName"), data.get("accountNumber"));

		// Ledger account
		PO.login(data.get("AccrualUser"), data.get("password"));
		PO.addCostCenter(data.get("poWindowName"));
		PO.addLedgerAccount(data.get("productCode"), data.get("accountNumber"));

		// Get PO Number
		System.out.println(PO.getPoNumber());
		poDocNumber=PO.getPoNumber();
		
		//Get Receipt Amount and Qty
		receiptAmount = lineNetAmount * 0.3;
		String lineNetQty = data.get("quantity");		
		receiptQty = Math.round(Double.parseDouble(lineNetQty) * 0.3);
		
		// Logout and Close driver
		PO.logout();
		
		

	}
	
	@Test(dataProvider = "poReceiptData",dependsOnMethods = "purchaseOrderCreation",retryAnalyzer = RetryAnalyzer.class)
	public void POReceiptCreation(HashMap<String, String> data) throws SQLException, InterruptedException {
		launchApplication();
		POReceiptLocators receipt = new POReceiptLocators(driver, wait, action);
		receipt.login(data.get("SingleApprovalRequester"), data.get("password"));
		receipt.openWindow(data.get("WindowName"));
		receipt.createNewHeader();
		receipt.transactionType(txrnType, currentDate, data.get("department"));
		receipt.passPO(poDocNumber);
		receipt.saveHeader();
		receipt.addLines(contractType);
		receipt.popUpAction(contractType, productCode, String.valueOf(receiptAmount), String.valueOf(receiptQty));
		receipt.submitOrApprove();
		String actualMessage = receipt.submitMessage(poDocNumber);
		Assert.assertTrue(actualMessage.equalsIgnoreCase("Success"), "Expected message 'Success' but got: " + actualMessage);
	}

}
