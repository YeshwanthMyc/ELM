package ReceiptCreationFromPO;

import java.sql.SQLException;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import LocatorsOfWindows.POReceiptLocators;
import LocatorsOfWindows.PurchaseOrderLocators;
import TestComponents.BaseClass;
import TestComponents.RetryAnalyzer;

public class POReceipt extends BaseClass {
	
	@BeforeClass
	public void setupReceiptData() {
		commonData();
		poData();
		receiptData();
	}

	
	@Test(dataProvider = "poData", retryAnalyzer = RetryAnalyzer.class)
	public void purchaseOrderCreation(HashMap<String, String> data) throws InterruptedException, SQLException {

		PurchaseOrderLocators PO = new PurchaseOrderLocators(driver, wait, action);
		if (poApprovalType.equalsIgnoreCase("Multi")) {
			PO.login(data.get("MultiApprovalRequester"), data.get("password"));
		}

		if (poApprovalType.equalsIgnoreCase("Single")) {
			PO.login(data.get("SingleApprovalRequester"), data.get("password"));
		}
		PO.openWindow(data.get("poWindowName"));
		PO.createNewHeader();
		PO.maximizeHeader();
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
		if (poApprovalType.equalsIgnoreCase("Multi")) {
			PO.selectUniqueCode(data.get("MultiApprovalRequesterRoleId"), data.get("accountNumber"));
		}
		if (poApprovalType.equalsIgnoreCase("Single")) {
			PO.selectUniqueCode(data.get("SingleApprovalRequesterRoleId"), data.get("accountNumber"));
		}
		PO.saveHeader();
		PO.addAttachment(data.get("attachmentFilePath"));
		PO.navigateToPOLinesTab();
		PO.createNewLine();
		PO.selectProduct(data.get("productCode"));
		productCode = data.get("productCode");
		PO.enterQuantity(data.get("quantity"));
		PO.enterUnitPrice(data.get("unitPrice"));
		PO.saveLine();
		double lineNetAmount = PO.getLineNetAmount();
		if (lineNetAmount >= 10000) {
			PO.contractAttributes(hijricurrentDate, hijrifutureDate);
		}
		PO.navigateToPOHeader();
		PO.submitOrApprove();
		SubmitMessageresult = PO.submitMessageValidation(poDocNumber,
				data.get("poWindowName"), "purchaseOrderCreation",null);
		submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
		actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
		if (submitMessageSuccessResult) {
			submitMessageSuccess = true;
		} else {
			submitMessageSuccess = false;
			System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
			Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
		}
		logout();
		PO.POApproval(data.get("poWindowName"), data.get("accountNumber"));
		PO.login(data.get("AccrualUser"), data.get("password"));
		PO.addCostCenter(data.get("poWindowName"));
		PO.addLedgerAccount(data.get("productCode"), data.get("accountNumber"));
		System.out.println(PO.getPoNumber());
		poDocNumber = PO.getPoNumber();
		receiptAmount = lineNetAmount * 0.3;
		String lineNetQty = data.get("quantity");
		receiptQty = Math.round(Double.parseDouble(lineNetQty) * 0.3);
		
	}

	@Test(dataProvider = "poReceiptData", retryAnalyzer = RetryAnalyzer.class, dependsOnMethods = {
			"purchaseOrderCreation" })
	public void POReceiptCreation(HashMap<String, String> data) throws SQLException, InterruptedException {

		POReceiptLocators receipt = new POReceiptLocators(driver, wait, action);
		receipt.login(data.get("SingleApprovalRequester"), data.get("password"));
		receipt.openWindow(data.get("WindowName"));
		receipt.createNewHeader();
		receipt.transactionType(txrnType, hijricurrentDate, data.get("Department"));
		receipt.passPO(poDocNumber);
		receipt.saveHeader();
		receipt.addLines(contractType);
		receipt.popUpAction(contractType, productCode, String.valueOf(receiptAmount), String.valueOf(receiptQty));
		receipt.submitOrApprove();
		SubmitMessageresult = receipt.submitMessageValidation(poDocNumber,
				data.get("WindowName"), "POReceiptCreationForRDVNoDeduction",null);
		submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
		actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
		if (submitMessageSuccessResult) {
			submitMessageSuccess = true;
		} else {
			submitMessageSuccess = false;
			System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
			Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
		}

	}


}
