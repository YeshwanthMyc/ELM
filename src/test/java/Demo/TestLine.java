package Demo;

import org.testng.annotations.Test;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import LocatorsOfWindows.ReceiptDeliveryVerificationLocators;
import TestComponents.BaseClass;
import TestComponents.RetryAnalyzer;

public class TestLine extends BaseClass {

	public static double matchedAmt = 0;
	public static double holdAmt = 0;

	// No Deduction
	public static double noDeductionNetMatchedAmt = 0;
	public static String noDeductionTxrnId = null;
	public static String noDeductionInvoioceId = null;

	boolean matchAllSuccess = false;
	boolean holdSuccess = false;
	boolean submitMessageSuccess = false;
	boolean generateAmarsarafMessageSuccess = false;

	String RDVApprovalType = "Multi";
	String poDocNumber ="1018139";

	@Test(dataProvider = "RDVData")
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
			holdDeductionTxrnId = RDV.getTxrnId(poDocNumber);
			
			logout();
			if (RDVApprovalType.equalsIgnoreCase("Multi")) {
				submitMessageSuccess = RDV.RDVApproval(holdDeductionTxrnId, poDocNumber, "Receipt Delivery Verification");
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
