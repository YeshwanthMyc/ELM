package RDVCreation;

import java.sql.SQLException;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import LocatorsOfWindows.POReceiptLocators;
import LocatorsOfWindows.PurchaseOrderLocators;
import LocatorsOfWindows.ReceiptDeliveryVerificationLocators;
import TestComponents.BaseClass;
import TestComponents.RetryAnalyzer;

public class RDVCreation extends BaseClass {

	@BeforeClass(alwaysRun = true)
	public void setupRdvData() {
		commonData();
		poData();
		receiptData();
		rdvData();
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
		PO.awardDate(BaseClass.currentDate);
		PO.letterDate(currentDate);
		if(isTaxPO=true) {
			PO.enterTaxDetails(data.get("taxMethod"),data.get("poWindowName"));
		}
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

		SubmitMessageresult = PO.submitMessageValidation(poDocNumber, data.get("poWindowName"), "purchaseOrderCreation",
				null);
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
		receiptAmount = lineNetAmount * 0.15;
		String lineNetQty = data.get("quantity");
		receiptQty = Math.round(Double.parseDouble(lineNetQty) * 0.3);

	}

	@Test(dataProvider = "poReceiptData", retryAnalyzer = RetryAnalyzer.class, dependsOnMethods = {
			"purchaseOrderCreation" })
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
		SubmitMessageresult = receipt.submitMessageValidation(poDocNumber, data.get("WindowName"),
				"POReceiptCreationForRDVNoDeduction", null);
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

	@Test(dataProvider = "RDVData", dependsOnMethods = {
			"POReceiptCreationForRDVNoDeduction" }, retryAnalyzer = RetryAnalyzer.class)
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

			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber, "Receipt Delivery Verification",
					"createRDVWithNoDeduction", null);
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
			if (RDVApprovalType.equalsIgnoreCase("Multi")) {

				approvalresult = RDV.RDVApproval(noDeductionTxrnId, poDocNumber, "Receipt Delivery Verification");
				submitMessageSuccessResult = (boolean) approvalresult.get("submitMessageSuccessResult");
				originalMessage = (String) approvalresult.get("originalMessage");
				actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");

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

			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber, "Receipt Delivery Verification",
					"Generate Amasaraf", null);
			submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

			// Data Needed for Invoice
			noDeductionTxrnId = RDV.getTxrnId(poDocNumber);

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

		SubmitMessageresult = receipt.submitMessageValidation(poDocNumber, data.get("WindowName"),
				"POReceiptCreationForRDVDeductionHold", null);
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

			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber, "Receipt Delivery Verification",
					"createRDVWithDeductionHold", null);
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
			if (RDVApprovalType.equalsIgnoreCase("Multi")) {

				approvalresult = RDV.RDVApproval(holdDeductionTxrnId, poDocNumber, "Receipt Delivery Verification");
				submitMessageSuccessResult = (boolean) approvalresult.get("submitMessageSuccessResult");
				originalMessage = (String) approvalresult.get("originalMessage");
				actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");

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
			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber, "Receipt Delivery Verification",
					"Generate Amasaraf", null);
			submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

			// Data For Invoice
			holdDeductionTxrnId = RDV.getTxrnId(poDocNumber);

		} catch (AssertionError | Exception e) {

			if (!matchAllSuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && !holdSuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && holdSuccess && !submitMessageSuccess) {
				if (originalMessage.equalsIgnoreCase("Warning")) {
					System.out.println("Check Warning Message");
					Assert.fail("Check Warning Message");
				}
				if (originalMessage.equalsIgnoreCase("Error")) {
					System.out.println("Record Not Submitted");
					Assert.fail("Record Not Submitted");
				}
			}
			if (matchAllSuccess && holdSuccess && submitMessageSuccess && !generateAmarsarafMessageSuccess) {
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

		SubmitMessageresult = receipt.submitMessageValidation(poDocNumber, data.get("WindowName"),
				"POReceiptCreationForRDVDeductionPenalty", null);
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
			RDV.enterPenaltyDetails(data.get("10% Penalty Name"), data.get("Revenue Account"));
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
			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber, "Receipt Delivery Verification",
					"createRDVWithDeductionPenalty", null);
			submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

			// Data Needed for Invoice
			penaltyDeductionTxrnId = RDV.getTxrnId(poDocNumber);

			logout();
			if (RDVApprovalType.equalsIgnoreCase("Multi")) {

				approvalresult = RDV.RDVApproval(penaltyDeductionTxrnId, poDocNumber, "Receipt Delivery Verification");
				submitMessageSuccessResult = (boolean) approvalresult.get("submitMessageSuccessResult");
				originalMessage = (String) approvalresult.get("originalMessage");
				actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");

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
			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber, "Receipt Delivery Verification",
					"Generate Amasaraf", null);
			submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

		} catch (AssertionError | Exception e) {

			if (!matchAllSuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && !penaltySuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && penaltySuccess && !submitMessageSuccess) {
				if (originalMessage.equalsIgnoreCase("Warning")) {
					System.out.println("Check Warning Message");
					Assert.fail("Check Warning Message");
				}
				if (originalMessage.equalsIgnoreCase("Error")) {
					System.out.println("Record Not Submitted");
					Assert.fail("Record Not Submitted");
				}
			}
			if (matchAllSuccess && penaltySuccess && submitMessageSuccess && !generateAmarsarafMessageSuccess) {
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
		SubmitMessageresult = receipt.submitMessageValidation(poDocNumber, data.get("WindowName"),
				"POReceiptCreationForRDVDeductionExternalPenalty", null);
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
			RDV.enterExternalPenaltyDetails(data.get("External Penalty Name"), penaltyAmountToBeEntered,
					data.get("External Penalty Supplier Name"));
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
			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber, "Receipt Delivery Verification",
					"createRDVWithDeductionExternalPenalty", null);
			submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

			// Data Needed for Invoice
			extPenaltyDeductionTxrnId = RDV.getTxrnId(poDocNumber);

			logout();
			if (RDVApprovalType.equalsIgnoreCase("Multi")) {

				approvalresult = RDV.RDVApproval(extPenaltyDeductionTxrnId, poDocNumber,
						"Receipt Delivery Verification");
				submitMessageSuccessResult = (boolean) approvalresult.get("submitMessageSuccessResult");
				originalMessage = (String) approvalresult.get("originalMessage");
				actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");

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
			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber, "Receipt Delivery Verification",
					"Generate Amasaraf", null);
			submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

		} catch (AssertionError | Exception e) {

			if (!matchAllSuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && !externalpenaltySuccess) {
				// RDV.removeBulkPenalty();
			}
			if (matchAllSuccess && externalpenaltySuccess && !submitMessageSuccess) {
				if (originalMessage.equalsIgnoreCase("Warning")) {
					System.out.println("Check Warning Message");
					Assert.fail("Check Warning Message");
				}
				if (originalMessage.equalsIgnoreCase("Error")) {
					System.out.println("Record Not Submitted");
					Assert.fail("Record Not Submitted");
				}
			}
			if (matchAllSuccess && externalpenaltySuccess && submitMessageSuccess && !generateAmarsarafMessageSuccess) {
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
		SubmitMessageresult = receipt.submitMessageValidation(poDocNumber, data.get("WindowName"),
				"POReceiptCreationForRDVWithAllDeductions", null);
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
			RDV.enterPenaltyDetails(data.get("10% Penalty Name"), data.get("Revenue Account"));
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
			RDV.enterExternalPenaltyDetails(data.get("External Penalty Name"), penaltyAmountToBeEntered,
					data.get("External Penalty Supplier Name"));
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
			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber, "Receipt Delivery Verification",
					"createRDVWithAllDeductions", null);
			submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

			// Data Needed for Invoice
			allDeductionTxrnId = RDV.getTxrnId(poDocNumber);

			logout();
			if (RDVApprovalType.equalsIgnoreCase("Multi")) {

				approvalresult = RDV.RDVApproval(allDeductionTxrnId, poDocNumber, "Receipt Delivery Verification");
				submitMessageSuccessResult = (boolean) approvalresult.get("submitMessageSuccessResult");
				originalMessage = (String) approvalresult.get("originalMessage");
				actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");

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
			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber, "Receipt Delivery Verification",
					"Generate Amasaraf", null);
			submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

		} catch (AssertionError | Exception e) {

			if (!matchAllSuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && !holdSuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && holdSuccess && !penaltySuccess) {
				RDV.deleteIcon();
			}
			if (matchAllSuccess && holdSuccess && penaltySuccess && !externalpenaltySuccess) {
				// RDV.removeBulkPenalty();
			}
			if (matchAllSuccess && holdSuccess && penaltySuccess && externalpenaltySuccess && !submitMessageSuccess) {
				if (originalMessage.equalsIgnoreCase("Warning")) {
					System.out.println("Check Warning Message");
					Assert.fail("Check Warning Message");
				}
				if (originalMessage.equalsIgnoreCase("Error")) {
					System.out.println("Record Not Submitted");
					Assert.fail("Record Not Submitted");
				}
			}
			if (matchAllSuccess && holdSuccess && penaltySuccess && externalpenaltySuccess && submitMessageSuccess
					&& !generateAmarsarafMessageSuccess) {
				System.out.println("Record Not Submitted");
				Assert.fail("Invoice Not Generated");
			}

			throw e;
		}

	}

}
