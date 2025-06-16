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
	String poDocNumber ="1018145";
	
	String actualMessageForSubmittext=null;
	Map<String, Object> SubmitMessageresult;
	boolean submitMessageSuccessResult=false;
	Map<String, Object> approvalresult;

	
	@Test(dataProvider = "RDVData")
	public void createRDVWithDeductionHold(HashMap<String, String> data) throws SQLException, InterruptedException {
		ReceiptDeliveryVerificationLocators RDV = new ReceiptDeliveryVerificationLocators(driver, wait, action);
		rdvData();
		receiptData();
		commonData();
		poData();
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
			
			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber,
					"Receipt Delivery Verification", "createRDVWithNoDeduction");//changes
			submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
			actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
			if (submitMessageSuccessResult) {
				submitMessageSuccess = true;
			} else {
				submitMessageSuccess = false;
				System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
				Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
			}

			if (submitMessageSuccessResult) {//changes Duplicated code need to remove
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
				
				approvalresult = RDV.RDVApproval(noDeductionTxrnId, poDocNumber, "Receipt Delivery Verification");//changes
				submitMessageSuccessResult =(boolean) approvalresult.get("submitMessageSuccessResult");
				originalMessage =(String) approvalresult.get("originalMessage");
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
			
			SubmitMessageresult = RDV.submitMessageValidation(poDocNumber,
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
