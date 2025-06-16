package PurchaseOrderCreation;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import LocatorsOfWindows.PurchaseOrderLocators;
import TestComponents.BaseClass;
import TestComponents.RetryAnalyzer;

public class PurchaseOrder extends BaseClass {
	
	@BeforeClass
	public void setupPOData() {
		commonData();
		poData();
	}
	String actualMessageForSubmittext=null;
	Map<String, Object> SubmitMessageresult;
	boolean submitMessageSuccessResult=false;
	
	@Test(dataProvider = "poData", retryAnalyzer = RetryAnalyzer.class)
	public void purchaseOrderCreation(HashMap<String, String> data) throws InterruptedException, SQLException {
		launchApplication();
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
				data.get("poWindowName"), "purchaseOrderCreation");
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

	}

}
