package Demo;

import java.sql.SQLException;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import LocatorsOfWindows.ReceiptDeliveryVerificationLocators;
import RDVCreation.RDVCreation;
import TestComponents.BaseClass;
import TestComponents.RetryAnalyzer;

public class TestLine extends BaseClass {

	boolean matchAllSuccess = false;
	boolean submitMessageSuccess = false;
	boolean generateAmarsarafMessageSuccess = false;
	boolean holdSuccess = false;
	boolean penaltySuccess = false;
	boolean externalpenaltySuccess = false;
	String originalMessage = null;
	static HashMap<String, String> currentTestData;
	double matchedAmt = 0;
	double holdAmt = 0;
	double penaltyAmt = 0;
	double externalpenaltyAmt = 0;

//	@Test(dataProvider = "RDVData",retryAnalyzer = RetryAnalyzer.class)
	public void createRDVWithAllDeductions(HashMap<String, String> data) throws SQLException, InterruptedException {

		String originalMessage = null;
		ReceiptDeliveryVerificationLocators RDV = new ReceiptDeliveryVerificationLocators(driver, wait, action);
		try {

			RDV.login("Openbravo", "12");
			RDV.openWindow("Receipt Delivery Verification");
			RDV.RDVPOFilter("1018107");
			RDV.navigateToTransactionVersion();
			RDV.createNewLine();
			RDV.approvalType();
			RDV.certificateNumber();
			RDV.saveLine();
			RDV.matchAll();
			RDV.popUpOkButton();
			matchedAmt = RDV.getMatchedAmount("1018107");

			// Match All
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
			holdAmt = RDV.getHoldAmount("1018107");
			if (holdAmt > 0) {
				holdSuccess = true;
			} else {
				holdSuccess = false;
				System.out.println("Unable to add hold");
				Assert.fail("Unable to add hold");
			}

			// Penalty
			RDV.enterPenaltyDetails(data.get("10% Penalty Name"));
			penaltyAmt = RDV.getPenaltyAmount("1018107");
			if (penaltyAmt > 0) {
				penaltySuccess = true;
			} else {
				penaltySuccess = false;
				System.out.println("Unable to add Penalty");
				Assert.fail("Unable to add Penalty");
			}

			// External Penalty
			double penaltyAmount = matchedAmt*0.3;
			String penaltyAmountToBeEntered=String.valueOf(penaltyAmount);
			RDV.enterExternalPenaltyDetails(data.get("External Penalty Name"),penaltyAmountToBeEntered);
			externalpenaltyAmt = RDV.getPenaltyAmount("1018107");
			if (externalpenaltyAmt == matchedAmt * 0.4) {
				externalpenaltySuccess = true;
			} else {
				externalpenaltySuccess = false;
				System.out.println("Unable to add External Penalty");
				Assert.fail("Unable to add External Penalty");
			}

			// Submit Transaction version
			RDV.submitOrApprove();
			String actualMessageForSubmit = RDV.submitMessage("1018107", "Receipt Delivery Verification",
					"Transaction Version");
			String actualMessageForSubmittext[] = actualMessageForSubmit.split(" Result:");
			originalMessage = actualMessageForSubmittext[0];
			if (originalMessage.equalsIgnoreCase("Success")) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext[1]);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext[1]);
			}

			// Generate Amarsaraf
			RDV.generateAmarsaraf();
			RDV.popUpOkButton();
			String actualMessageForInvoice = RDV.submitMessage("1018107", "Receipt Delivery Verification",
					"Generate Amarsaraf");
			String actualMessageForInvoicetext[] = actualMessageForInvoice.split(" Result:");
			originalMessage = actualMessageForInvoicetext[0];
			if (originalMessage.equalsIgnoreCase("Success")) {
				generateAmarsarafMessageSuccess = true;
			} else {
				generateAmarsarafMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
				Assert.fail("Expected 'Success' but got: " + actualMessageForInvoicetext[1]);
			}
		} catch (AssertionError | Exception e) {

			if (!matchAllSuccess) {
				RDV.deleteIcon();
			} else if (matchAllSuccess && !holdSuccess) {
				RDV.deleteIcon();
			} else if (matchAllSuccess && holdSuccess && !penaltySuccess) {
				RDV.deleteIcon();
			} else if (matchAllSuccess && holdSuccess && penaltySuccess && !externalpenaltySuccess) {
				// RDV.removeBulkPenalty();
				RDV.deleteIcon();
			} else if (matchAllSuccess && holdSuccess && penaltySuccess && externalpenaltySuccess
					&& !submitMessageSuccess) {
				if (originalMessage.equalsIgnoreCase("Warning")) {
					System.out.println("Check Warning Message");
					RDV.RDVCancel();
					Assert.fail("Check Warning Message");
				}
				if (originalMessage.equalsIgnoreCase("Error")) {
					System.out.println("Record Not Submitted");
					// RDV.removeBulkPenalty();
					RDV.deleteIcon();
					Assert.fail("Record Not Submitted");
				}
			} else if (matchAllSuccess && holdSuccess && penaltySuccess && externalpenaltySuccess
					&& submitMessageSuccess && !generateAmarsarafMessageSuccess) {
				System.out.println("Invoice generation failed. Stopping execution.");
				Assert.fail("Invoice generation failed. Stopping execution.");
			}

			throw e;
		}

	}
	
	//@Test(dataProvider = "RDVData")
	public void testing(HashMap<String, String> data) throws SQLException, InterruptedException {
		currentTestData = data;
		RDVCreation RDV = new RDVCreation();
		//double extAmt = RDV.createRDVWithDeductionExternalPenalty(currentTestData);
		//System.out.println(extAmt);
	}
}
