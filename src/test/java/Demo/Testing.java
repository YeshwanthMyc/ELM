package Demo;

import java.sql.SQLException;
import java.util.HashMap;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;
import LocatorsOfWindows.InvoiceLocators;
import TestComponents.BaseClass;
import TestComponents.RetryAnalyzer;

public class Testing extends BaseClass{
	@BeforeClass
	public void setupData() {
		commonData();
		poData();
		receiptData();
		rdvData();
		invData();
	}
	@Test(dataProvider ="RDVInvoiceData")
	public void InvoiceWithNoDeduction(HashMap<String, String> data) throws SQLException, InterruptedException {
		InvoiceLocators RDVInv = new InvoiceLocators(driver,wait,action);
		invDocNumber = RDVInv.getDocNumber(noDeductionTxrnId);
		RDVInv.login(data.get("userName"), "12");
		RDVInv.openInvoiceWindow();
		RDVInv.documentNoFilter(invDocNumber);
		RDVInv.mofRequestNumber(data.get("mofRequestNo"),invDocNumber);
		RDVInv.description("Automation Testing");
		RDVInv.enterTaxDetails(data.get("taxMethod"));
		RDVInv.enterNoClaimDetails(currentDate);
		RDVInv.enterSupplierInvNumberAndDate(currentDate);
		RDVInv.saveHeader();
		RDVInv.undoIcon();
		RDVInv.addTaxLines();
		Thread.sleep(3000);
		boolean amountValidations = RDVInv.amountValidations(noDeductionTxrnId, 
				invDocNumber, Deduction,penaltyName,revenueAccount,externalPenaltyName,externalPenaltySupplierName);
		Assert.assertTrue(amountValidations,"Amount validation failed");
		RDVInv.submitOrApprove();
		
		SubmitMessageresult = RDVInv.submitMessageValidation(poDocNumber,
				"Purchase Invoice", "InvoiceWithNoDeduction",invDocNumber);
		submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
		actualMessageForSubmittext = (String) SubmitMessageresult.get("actualMessageForSubmittext[1]");
		if (submitMessageSuccessResult) {
			submitMessageSuccess = true;
		} else {
			submitMessageSuccess = false;
			System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
			Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
		}
		RDVInv.logout();
		
		invDocNumber = RDVInv.getDocNumber(noDeductionTxrnId);
		
		approvalresult = RDVInv.invoiceApproval(poDocNumber,invDocNumber);
		submitMessageSuccessResult =(boolean) approvalresult.get("submitMessageSuccessResult");
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
