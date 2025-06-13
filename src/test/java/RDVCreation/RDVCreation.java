package RDVCreation;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import LocatorsOfWindows.POReceiptLocators;
import LocatorsOfWindows.PurchaseOrderLocators;
import LocatorsOfWindows.ReceiptDeliveryVerificationLocators;
import TestComponents.BaseClass;
import TestComponents.RetryAnalyzer;

public class RDVCreation extends BaseClass {

	@BeforeClass
	public void setupRdvData() {
		commonData();
		poData();
		receiptData();
		rdvData();
	}

	@Test(dataProvider = "poData", retryAnalyzer = RetryAnalyzer.class, groups = { "NoDeduction" })
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
		PO.awardDate(BaseClass.currentDate);
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
		Map<String, Object> SubmitMessageresult = PO.submitMessageValidation(poDocNumber,
				data.get("poWindowName"), "purchaseOrderCreation");
		boolean submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
		String actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
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
		receiptAmount = lineNetAmount * 0.15;
		String lineNetQty = data.get("quantity");
		receiptQty = Math.round(Double.parseDouble(lineNetQty) * 0.3);

	}

	@Test(dataProvider = "poReceiptData", retryAnalyzer = RetryAnalyzer.class, dependsOnMethods = {
			"purchaseOrderCreation" }, groups = { "NoDeduction" })
	public void POReceiptCreationForRDVNoDeduction(HashMap<String, String> data)
			throws SQLException, InterruptedException {

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
		Map<String, Object> SubmitMessageresult = receipt.submitMessageValidation(poDocNumber,
				data.get("WindowName"), "POReceiptCreationForRDVNoDeduction");
		boolean submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
		String actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
		if (submitMessageSuccessResult) {
			submitMessageSuccess = true;
		} else {
			submitMessageSuccess = false;
			System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
			Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
		}

	}

	@Test(dataProvider = "RDVData", dependsOnMethods = {
			"POReceiptCreationForRDVNoDeduction" }, retryAnalyzer = RetryAnalyzer.class, groups = { "NoDeduction" })
	public void createRDVWithNoDeduction(HashMap<String, String> data) throws SQLException, InterruptedException {
		ReceiptDeliveryVerificationLocators RDV = new ReceiptDeliveryVerificationLocators(driver, wait, action);
		try {

			if (RDVApprovalType.equalsIgnoreCase("Single")) {
				RDV.login("Openbravo", "12");
			} else
				RDV.login("4339589", "12");

			RDV.openWindow("Receipt Delivery Verification");
			RDV.createNewHeader();
			RDV.enterPONumber(poDocNumber);
			RDV.saveHeader();
			RDV.undoIcon();
			RDV.RDVPOFilter(poDocNumber);
			RDV.navigateToTransactionVersion();
			RDV.createNewLine();
			RDV.approvalType();
			RDV.certificateNumber();
			RDV.saveLine();
			RDV.matchAll();
			RDV.popUpOkButton();

			// Match all
			matchedAmt = RDV.getMatchedAmount(poDocNumber);
			if (matchedAmt > 0) {
				matchAllSuccess = true;
			} else {
				matchAllSuccess = false;
				System.out.println("Matched amount is 0, cannot proceed.");
				Assert.fail("Match All failed. Matched amount is 0.");
			}

			// Submit Transaction version
			RDV.submitOrApprove();
			Map<String, Object> SubmitMessageresult = RDV.submitMessageValidation(poDocNumber,
					"Receipt Delivery Verification", "createRDVWithNoDeduction");
			boolean submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			String actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");

			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

			// Data Needed for Invoice
			noDeductionNetMatchedAmt = RDV.getNetMatchedAmount(poDocNumber);
			noDeductionTxrnId = RDV.getTxrnId(poDocNumber);

			logout();
			if (RDVApprovalType.equalsIgnoreCase("Multi")) {
				submitMessageSuccess = RDV.RDVApproval(noDeductionTxrnId, poDocNumber, "Receipt Delivery Verification");
				if (submitMessageSuccessResult) {
					submitMessageSuccess = true;
				} else {
					submitMessageSuccess = false;
					System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
					Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
				}
			}

			// Generate Amarsaraf
			RDV.login(data.get("HQ RDV user"), "12");
			RDV.generateAmarsaraf("Receipt Delivery Verification", poDocNumber);
			RDV.popUpOkButton();
			String actualMessageForInvoice = RDV.submitMessage(poDocNumber, "Receipt Delivery Verification",
					"Generate Amarsaraf");
			String actualMessageForInvoicetext[] = actualMessageForInvoice.split(" Result:");
			originalMessage = actualMessageForInvoicetext[0];
			if (originalMessage.equalsIgnoreCase("Success")) {
				generateAmarsarafMessageSuccess = true;
				noDeductionInvoioceId = RDV.getInvoiceId(poDocNumber);
			} else {
				generateAmarsarafMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
				Assert.fail("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
			}

		} catch (AssertionError | Exception e) {

			if (!matchAllSuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && !submitMessageSuccess) {
				if (originalMessage.equalsIgnoreCase("Warning")) {
					System.out.println("Check Warning Message");
					Assert.fail("Check Warning Message");
				}
				if (originalMessage.equalsIgnoreCase("Error")) {
					System.out.println("Record Not Submitted");
					Assert.fail("Record Not Submitted");
				}
			}
			if (matchAllSuccess && submitMessageSuccess && !generateAmarsarafMessageSuccess) {
				System.out.println("Record Not Submitted");
				Assert.fail("Invoice Not Generated");
			}
			throw e;
		}

	}

	@Test(dataProvider = "poReceiptData", retryAnalyzer = RetryAnalyzer.class, dependsOnMethods = {
			"createRDVWithNoDeduction" })
	public void POReceiptCreationForRDVDeductionHold(HashMap<String, String> data)
			throws SQLException, InterruptedException {
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
		Map<String, Object> SubmitMessageresult = receipt.submitMessageValidation(poDocNumber,
				data.get("WindowName"), "POReceiptCreationForRDVDeductionHold");
		boolean submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
		String actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
		if (submitMessageSuccessResult) {
			submitMessageSuccess = true;
		} else {
			submitMessageSuccess = false;
			System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
			Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
		}

	}

	@Test(dataProvider = "RDVData", dependsOnMethods = {
			"POReceiptCreationForRDVDeductionHold" }, retryAnalyzer = RetryAnalyzer.class)
	public void createRDVWithDeductionHold(HashMap<String, String> data) throws SQLException, InterruptedException {
		ReceiptDeliveryVerificationLocators RDV = new ReceiptDeliveryVerificationLocators(driver, wait, action);
		try {

			if (RDVApprovalType.equalsIgnoreCase("Single")) {
				RDV.login("Openbravo", "12");
			} else
				RDV.login("4339589", "12");

			RDV.openWindow("Receipt Delivery Verification");
			RDV.createNewHeader();
			RDV.enterPONumber(poDocNumber);
			RDV.saveHeader();
			RDV.undoIcon();
			RDV.RDVPOFilter(poDocNumber);
			RDV.navigateToTransactionVersion();
			RDV.createNewLine();
			RDV.approvalType();
			RDV.certificateNumber();
			RDV.saveLine();
			RDV.matchAll();
			RDV.popUpOkButton();

			// Match all
			matchedAmt = RDV.getMatchedAmount(poDocNumber);
			if (matchedAmt > 0) {
				matchAllSuccess = true;
			} else {
				matchAllSuccess = false;
				System.out.println("Matched amount is 0, cannot proceed.");
				Assert.fail("Match All failed. Matched amount is 0.");
			}

			// Hold
			double holdAmount = matchedAmt * 0.3;
			String holdAmounttoBeEntered = String.valueOf(holdAmount);
			RDV.enterHoldDetails(data.get("holdName"), holdAmounttoBeEntered);
			holdAmt = RDV.getHoldAmount(poDocNumber);
			if (holdAmt > 0) {
				holdSuccess = true;
			} else {
				holdSuccess = false;
				System.out.println("Unable to add hold");
				Assert.fail("Unable to add hold");
			}

			// Submit Transaction version
			RDV.submitOrApprove();
			Map<String, Object> SubmitMessageresult = RDV.submitMessageValidation(poDocNumber,
					"Receipt Delivery Verification", "createRDVWithDeductionHold");
			boolean submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			String actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");

			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

			// Data For Invoice
			logout();
			if (RDVApprovalType.equalsIgnoreCase("Multi")) {
				submitMessageSuccess = RDV.RDVApproval(noDeductionTxrnId, poDocNumber, "Receipt Delivery Verification");
				if (submitMessageSuccessResult) {
					submitMessageSuccess = true;
				} else {
					submitMessageSuccess = false;
					System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
					Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
				}
			}

			// Generate Amarsaraf
			RDV.login(data.get("HQ RDV user"), "12");
			RDV.generateAmarsaraf("Receipt Delivery Verification", poDocNumber);
			RDV.popUpOkButton();
			String actualMessageForInvoice = RDV.submitMessage(poDocNumber, "Receipt Delivery Verification",
					"Generate Amarsaraf");
			String actualMessageForInvoicetext[] = actualMessageForInvoice.split(" Result:");
			originalMessage = actualMessageForInvoicetext[0];
			if (originalMessage.equalsIgnoreCase("Success")) {
				generateAmarsarafMessageSuccess = true;
				noDeductionInvoioceId = RDV.getInvoiceId(poDocNumber);
			} else {
				generateAmarsarafMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
				Assert.fail("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
			}

		} catch (AssertionError | Exception e) {

			if (!matchAllSuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && !submitMessageSuccess) {
				if (originalMessage.equalsIgnoreCase("Warning")) {
					System.out.println("Check Warning Message");
					Assert.fail("Check Warning Message");
				}
				if (originalMessage.equalsIgnoreCase("Error")) {
					System.out.println("Record Not Submitted");
					Assert.fail("Record Not Submitted");
				}
			}
			if (matchAllSuccess && submitMessageSuccess && !generateAmarsarafMessageSuccess) {
				System.out.println("Record Not Submitted");
				Assert.fail("Invoice Not Generated");
			}
			throw e;
		}

	}

	@Test(dataProvider = "poReceiptData", retryAnalyzer = RetryAnalyzer.class, dependsOnMethods = {
			"createRDVWithDeductionHold" })
	public void POReceiptCreationForRDVDeductionPenalty(HashMap<String, String> data)
			throws SQLException, InterruptedException {
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
		Map<String, Object> SubmitMessageresult = receipt.submitMessageValidation(poDocNumber,
				data.get("WindowName"), "POReceiptCreationForRDVDeductionPenalty");
		boolean submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
		String actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
		if (submitMessageSuccessResult) {
			submitMessageSuccess = true;
		} else {
			submitMessageSuccess = false;
			System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
			Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
		}

	}

	@Test(dataProvider = "RDVData", dependsOnMethods = {
			"POReceiptCreationForRDVDeductionPenalty" }, retryAnalyzer = RetryAnalyzer.class)
	public void createRDVWithDeductionPenalty(HashMap<String, String> data) throws SQLException, InterruptedException {
		ReceiptDeliveryVerificationLocators RDV = new ReceiptDeliveryVerificationLocators(driver, wait, action);
		try {

			if (RDVApprovalType.equalsIgnoreCase("Single")) {
				RDV.login("Openbravo", "12");
			} else
				RDV.login("4339589", "12");

			RDV.openWindow("Receipt Delivery Verification");
			RDV.createNewHeader();
			RDV.enterPONumber(poDocNumber);
			RDV.saveHeader();
			RDV.undoIcon();
			RDV.RDVPOFilter(poDocNumber);
			RDV.navigateToTransactionVersion();
			RDV.createNewLine();
			RDV.approvalType();
			RDV.certificateNumber();
			RDV.saveLine();
			RDV.matchAll();
			RDV.popUpOkButton();

			// Match all
			matchedAmt = RDV.getMatchedAmount(poDocNumber);
			if (matchedAmt > 0) {
				matchAllSuccess = true;
			} else {
				matchAllSuccess = false;
				System.out.println("Matched amount is 0, cannot proceed.");
				Assert.fail("Match All failed. Matched amount is 0.");
			}

			// Penalty
			RDV.enterPenaltyDetails(data.get("10% Penalty Name"));
			penaltyAmt = RDV.getPenaltyAmount(poDocNumber);
			if (penaltyAmt > 0) {
				penaltySuccess = true;
			} else {
				penaltySuccess = false;
				System.out.println("Unable to add Penalty");
				Assert.fail("Unable to add Penalty");
			}

			// Submit Transaction version
			RDV.submitOrApprove();
			Map<String, Object> SubmitMessageresult = RDV.submitMessageValidation(poDocNumber,
					"Receipt Delivery Verification", "createRDVWithDeductionPenalty");
			boolean submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			String actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");

			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

			// Data Needed for Invoice

			logout();
			if (RDVApprovalType.equalsIgnoreCase("Multi")) {
				submitMessageSuccess = RDV.RDVApproval(noDeductionTxrnId, poDocNumber, "Receipt Delivery Verification");
				if (submitMessageSuccessResult) {
					submitMessageSuccess = true;
				} else {
					submitMessageSuccess = false;
					System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
					Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
				}
			}

			// Generate Amarsaraf
			RDV.login(data.get("HQ RDV user"), "12");
			RDV.generateAmarsaraf("Receipt Delivery Verification", poDocNumber);
			RDV.popUpOkButton();
			String actualMessageForInvoice = RDV.submitMessage(poDocNumber, "Receipt Delivery Verification",
					"Generate Amarsaraf");
			String actualMessageForInvoicetext[] = actualMessageForInvoice.split(" Result:");
			originalMessage = actualMessageForInvoicetext[0];
			if (originalMessage.equalsIgnoreCase("Success")) {
				generateAmarsarafMessageSuccess = true;
				noDeductionInvoioceId = RDV.getInvoiceId(poDocNumber);
			} else {
				generateAmarsarafMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
				Assert.fail("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
			}

		} catch (AssertionError | Exception e) {

			if (!matchAllSuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && !submitMessageSuccess) {
				if (originalMessage.equalsIgnoreCase("Warning")) {
					System.out.println("Check Warning Message");
					Assert.fail("Check Warning Message");
				}
				if (originalMessage.equalsIgnoreCase("Error")) {
					System.out.println("Record Not Submitted");
					Assert.fail("Record Not Submitted");
				}
			}
			if (matchAllSuccess && submitMessageSuccess && !generateAmarsarafMessageSuccess) {
				System.out.println("Record Not Submitted");
				Assert.fail("Invoice Not Generated");
			}
			throw e;
		}

	}

	@Test(dataProvider = "poReceiptData", retryAnalyzer = RetryAnalyzer.class, dependsOnMethods = {
			"createRDVWithDeductionPenalty" })
	public void POReceiptCreationForRDVDeductionExternalPenalty(HashMap<String, String> data)
			throws SQLException, InterruptedException {
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
		Map<String, Object> SubmitMessageresult = receipt.submitMessageValidation(poDocNumber,
				data.get("WindowName"), "POReceiptCreationForRDVDeductionExternalPenalty");
		boolean submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
		String actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
		if (submitMessageSuccessResult) {
			submitMessageSuccess = true;
		} else {
			submitMessageSuccess = false;
			System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
			Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
		}
	}

	@Test(dataProvider = "RDVData", dependsOnMethods = {
			"POReceiptCreationForRDVDeductionExternalPenalty" }, retryAnalyzer = RetryAnalyzer.class)
	public void createRDVWithDeductionExternalPenalty(HashMap<String, String> data)
			throws SQLException, InterruptedException {
		ReceiptDeliveryVerificationLocators RDV = new ReceiptDeliveryVerificationLocators(driver, wait, action);
		try {

			if (RDVApprovalType.equalsIgnoreCase("Single")) {
				RDV.login("Openbravo", "12");
			} else
				RDV.login("4339589", "12");

			RDV.openWindow("Receipt Delivery Verification");
			RDV.createNewHeader();
			RDV.enterPONumber(poDocNumber);
			RDV.saveHeader();
			RDV.undoIcon();
			RDV.RDVPOFilter(poDocNumber);
			RDV.navigateToTransactionVersion();
			RDV.createNewLine();
			RDV.approvalType();
			RDV.certificateNumber();
			RDV.saveLine();
			RDV.matchAll();
			RDV.popUpOkButton();

			// Match all
			matchedAmt = RDV.getMatchedAmount(poDocNumber);
			if (matchedAmt > 0) {
				matchAllSuccess = true;
			} else {
				matchAllSuccess = false;
				System.out.println("Matched amount is 0, cannot proceed.");
				Assert.fail("Match All failed. Matched amount is 0.");
			}

			// External Penalty
			double penaltyAmount = matchedAmt * 0.3;
			String penaltyAmountToBeEntered = String.valueOf(penaltyAmount);
			RDV.enterExternalPenaltyDetails(data.get("External Penalty Name"), penaltyAmountToBeEntered);
			externalpenaltyAmt = RDV.getPenaltyAmount(poDocNumber);
			if (externalpenaltyAmt > 0) {
				externalpenaltySuccess = true;
			} else {
				externalpenaltySuccess = false;
				System.out.println("Unable to add External Penalty");
				Assert.fail("Unable to add External Penalty");
			}

			// Submit Transaction version
			RDV.submitOrApprove();
			Map<String, Object> SubmitMessageresult = RDV.submitMessageValidation(poDocNumber,
					"Receipt Delivery Verification", "createRDVWithDeductionExternalPenalty");
			boolean submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			String actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");

			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

			// Data Needed for Invoice

			logout();
			if (RDVApprovalType.equalsIgnoreCase("Multi")) {
				submitMessageSuccess = RDV.RDVApproval(noDeductionTxrnId, poDocNumber, "Receipt Delivery Verification");
				if (submitMessageSuccessResult) {
					submitMessageSuccess = true;
				} else {
					submitMessageSuccess = false;
					System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
					Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
				}
			}

			// Generate Amarsaraf
			RDV.login(data.get("HQ RDV user"), "12");
			RDV.generateAmarsaraf("Receipt Delivery Verification", poDocNumber);
			RDV.popUpOkButton();
			String actualMessageForInvoice = RDV.submitMessage(poDocNumber, "Receipt Delivery Verification",
					"Generate Amarsaraf");
			String actualMessageForInvoicetext[] = actualMessageForInvoice.split(" Result:");
			originalMessage = actualMessageForInvoicetext[0];
			if (originalMessage.equalsIgnoreCase("Success")) {
				generateAmarsarafMessageSuccess = true;
				noDeductionInvoioceId = RDV.getInvoiceId(poDocNumber);
			} else {
				generateAmarsarafMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
				Assert.fail("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
			}

		} catch (AssertionError | Exception e) {

			if (!matchAllSuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && !submitMessageSuccess) {
				if (originalMessage.equalsIgnoreCase("Warning")) {
					System.out.println("Check Warning Message");
					Assert.fail("Check Warning Message");
				}
				if (originalMessage.equalsIgnoreCase("Error")) {
					System.out.println("Record Not Submitted");
					Assert.fail("Record Not Submitted");
				}
			}
			if (matchAllSuccess && submitMessageSuccess && !generateAmarsarafMessageSuccess) {
				System.out.println("Record Not Submitted");
				Assert.fail("Invoice Not Generated");
			}
			throw e;
		}

	}

	@Test(dataProvider = "poReceiptData", retryAnalyzer = RetryAnalyzer.class, dependsOnMethods = {
			"createRDVWithDeductionExternalPenalty" })
	public void POReceiptCreationForRDVWithAllDeductions(HashMap<String, String> data)
			throws SQLException, InterruptedException {
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
		Map<String, Object> SubmitMessageresult = receipt.submitMessageValidation(poDocNumber,
				data.get("WindowName"), "POReceiptCreationForRDVWithAllDeductions");
		boolean submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
		String actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
		if (submitMessageSuccessResult) {
			submitMessageSuccess = true;
		} else {
			submitMessageSuccess = false;
			System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
			Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
		}

	}

	@Test(dataProvider = "RDVData", dependsOnMethods = {
			"POReceiptCreationForRDVWithAllDeductions" }, retryAnalyzer = RetryAnalyzer.class)
	public void createRDVWithAllDeductions(HashMap<String, String> data) throws SQLException, InterruptedException {
		ReceiptDeliveryVerificationLocators RDV = new ReceiptDeliveryVerificationLocators(driver, wait, action);
		try {

			if (RDVApprovalType.equalsIgnoreCase("Single")) {
				RDV.login("Openbravo", "12");
			} else
				RDV.login("4339589", "12");

			RDV.openWindow("Receipt Delivery Verification");
			RDV.createNewHeader();
			RDV.enterPONumber(poDocNumber);
			RDV.saveHeader();
			RDV.undoIcon();
			RDV.RDVPOFilter(poDocNumber);
			RDV.navigateToTransactionVersion();
			RDV.createNewLine();
			RDV.approvalType();
			RDV.certificateNumber();
			RDV.saveLine();
			RDV.matchAll();
			RDV.popUpOkButton();

			// Match all
			matchedAmt = RDV.getMatchedAmount(poDocNumber);
			if (matchedAmt > 0) {
				matchAllSuccess = true;
			} else {
				matchAllSuccess = false;
				System.out.println("Matched amount is 0, cannot proceed.");
				Assert.fail("Match All failed. Matched amount is 0.");
			}

			// Hold
			double holdAmount = matchedAmt * 0.3;
			String holdAmounttoBeEntered = String.valueOf(holdAmount);
			RDV.enterHoldDetails(data.get("holdName"), holdAmounttoBeEntered);
			holdAmt = RDV.getHoldAmount(poDocNumber);
			if (holdAmt > 0) {
				holdSuccess = true;
			} else {
				holdSuccess = false;
				System.out.println("Unable to add hold");
				Assert.fail("Unable to add hold");
			}

			// Penalty
			RDV.enterPenaltyDetails(data.get("10% Penalty Name"));
			penaltyAmt = RDV.getPenaltyAmount(poDocNumber);
			if (penaltyAmt > 0) {
				penaltySuccess = true;
			} else {
				penaltySuccess = false;
				System.out.println("Unable to add Penalty");
				Assert.fail("Unable to add Penalty");
			}

			// External Penalty
			double penaltyAmount = matchedAmt * 0.3;
			String penaltyAmountToBeEntered = String.valueOf(penaltyAmount);
			RDV.enterExternalPenaltyDetails(data.get("External Penalty Name"), penaltyAmountToBeEntered);
			externalpenaltyAmt = RDV.getPenaltyAmount(poDocNumber);
			if (externalpenaltyAmt == matchedAmt * 0.4) {
				externalpenaltySuccess = true;
			} else {
				externalpenaltySuccess = false;
				System.out.println("Unable to add External Penalty");
				Assert.fail("Unable to add External Penalty");
			}

			// Submit Transaction version
			RDV.submitOrApprove();
			Map<String, Object> SubmitMessageresult = RDV.submitMessageValidation(poDocNumber,
					"Receipt Delivery Verification", "createRDVWithAllDeductions");
			boolean submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			String actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");

			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

			// Data Needed for Invoice

			logout();
			if (RDVApprovalType.equalsIgnoreCase("Multi")) {
				submitMessageSuccess = RDV.RDVApproval(noDeductionTxrnId, poDocNumber, "Receipt Delivery Verification");
				if (submitMessageSuccessResult) {
					submitMessageSuccess = true;
				} else {
					submitMessageSuccess = false;
					System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
					Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
				}
			}

			// Generate Amarsaraf
			RDV.login(data.get("HQ RDV user"), "12");
			RDV.generateAmarsaraf("Receipt Delivery Verification", poDocNumber);
			RDV.popUpOkButton();
			String actualMessageForInvoice = RDV.submitMessage(poDocNumber, "Receipt Delivery Verification",
					"Generate Amarsaraf");
			String actualMessageForInvoicetext[] = actualMessageForInvoice.split(" Result:");
			originalMessage = actualMessageForInvoicetext[0];
			if (originalMessage.equalsIgnoreCase("Success")) {
				generateAmarsarafMessageSuccess = true;
				noDeductionInvoioceId = RDV.getInvoiceId(poDocNumber);
			} else {
				generateAmarsarafMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
				Assert.fail("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
			}

		} catch (AssertionError | Exception e) {

			if (!matchAllSuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && !submitMessageSuccess) {
				if (originalMessage.equalsIgnoreCase("Warning")) {
					System.out.println("Check Warning Message");
					Assert.fail("Check Warning Message");
				}
				if (originalMessage.equalsIgnoreCase("Error")) {
					System.out.println("Record Not Submitted");
					Assert.fail("Record Not Submitted");
				}
			}
			if (matchAllSuccess && submitMessageSuccess && !generateAmarsarafMessageSuccess) {
				System.out.println("Record Not Submitted");
				Assert.fail("Invoice Not Generated");
			}
			throw e;
		}

	}

}
