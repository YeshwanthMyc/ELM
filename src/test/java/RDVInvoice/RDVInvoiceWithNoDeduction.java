package RDVInvoice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import LocatorsOfWindows.RDVInvoiceWithNoDeductionLocators;
import TestComponents.BaseClass;
public class RDVInvoiceWithNoDeduction extends BaseClass{
	
	
	
	@BeforeClass
	public void setupPOData() {
		commonData();
		invData();
	}
	
	
	@Test(dataProvider ="RDVInvoiceData",dependsOnGroups =  "NoDeduction",groups = {"NoDeduction"})
	public void InvoiceWithNoDeduction(HashMap<String, String> data) throws SQLException, InterruptedException {
		RDVInvoiceWithNoDeductionLocators RDVInv = new RDVInvoiceWithNoDeductionLocators(driver,wait,action);
		invDocNumber = RDVInv.getTempDocNumber(noDeductionTxrnId);
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
		boolean amountValidations = RDVInv.amountValidations(noDeductionTxrnId, invDocNumber);
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
		
		invDocNumber = RDVInv.getTempDocNumber(noDeductionTxrnId);
		
		approvalresult = RDVInv.invoiceApproval(poDocNumber,invDocNumber);
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
}
