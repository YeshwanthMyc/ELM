package Demo;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import CommonUtilities.ReusableUtilities;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import LocatorsOfWindows.InvoiceLocators;
import LocatorsOfWindows.PaymentOutLocators;
import TestComponents.BaseClass;
import TestComponents.RetryAnalyzer;

public class Testing extends BaseClass {
	@BeforeClass
	public void setupData() {
		commonData();
		poData();
		receiptData();
		rdvData();
		invData();
	}

	@Test(dataProvider = "RDVData")
	public void InvoiceWithNoDeduction(HashMap<String, String> data) throws SQLException, InterruptedException {
		InvoiceLocators RDVInv = new InvoiceLocators(driver, wait, action);
		invDocNumber = RDVInv.getDocNumber(extPenaltyDeductionTxrnId);
		RDVInv.login("4350143", "12");
		RDVInv.openWindow("Purchase Invoice");
		RDVInv.documentNoFilter("T-1001964");
		/*
		 * RDVInv.mofRequestNumber("0", "T-1001964");
		 * RDVInv.description("Automation Testing"); if(isTaxPO=false) {
		 * RDVInv.enterTaxDetails(data.get("taxMethod"),"Purchase Invoice"); }
		 * RDVInv.enterNoClaimDetails(currentDate);
		 * RDVInv.enterSupplierInvNumberAndDate(currentDate); RDVInv.saveHeader();
		 * RDVInv.undoIcon(); if(isTaxPO=false) { RDVInv.addTaxLines(); }
		 */
		Thread.sleep(3000);
		// Data Needed for Invoice
		penaltyName = data.get("10% Penalty Name");
		revenueAccount = data.get("Revenue Account");
		externalPenaltyName = data.get("External Penalty Name");
		externalPenaltySupplierName = data.get("External Penalty Supplier Name");
		boolean amountValidations = RDVInv.amountValidations("EA964554483548649A3D4E43976B9825", "T-1001964", Deduction,
				penaltyName, revenueAccount, externalPenaltyName, externalPenaltySupplierName, isTaxPO);
		Assert.assertTrue(amountValidations, "Amount validation failed");
		RDVInv.submitOrApprove();

		SubmitMessageresult = RDVInv.submitMessageValidation(poDocNumber, "Purchase Invoice",
				"InvoiceWithExternalPenaltyDeduction", invDocNumber);
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

		invDocNumber = RDVInv.getDocNumber(extPenaltyDeductionTxrnId);

		approvalresult = RDVInv.invoiceApproval(poDocNumber, invDocNumber);
		submitMessageSuccessResult = (boolean) approvalresult.get("submitMessageSuccessResult");
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
