package RDVCreation;

import java.sql.SQLException;

import org.testng.Assert;
import org.testng.annotations.Test;

import LocatorsOfWindows.ReceiptDeliveryVerificationLocators;
import ReceiptCreationFromPO.POReceipt;
import TestComponents.BaseClass;

public class RDVCreation extends BaseClass {

	@Test(dependsOnMethods = { "ReceiptCreationFromPO.POReceipt.POReceiptCreation1",
			"ReceiptCreationFromPO.POReceipt.purchaseOrderCreation" })
	public void createRDVWithNoDeduction() throws SQLException, InterruptedException {
		ReceiptDeliveryVerificationLocators RDV = new ReceiptDeliveryVerificationLocators(driver, wait, action);
		RDV.login("Openbravo", "12");
		RDV.openWindow("Receipt Delivery Verification");
		RDV.createNewHeader();
		System.out.println(POReceipt.poDocNumber);
		RDV.enterPONumber(POReceipt.poDocNumber);
		RDV.saveHeader();
		RDV.undoIcon();
		RDV.RDVPOFilter(POReceipt.poDocNumber);
		RDV.navigateToTransactionVersion();
		RDV.createNewLine();
		RDV.approvalType();
		RDV.certificateNumber();
		RDV.saveLine();
		RDV.matchAll();
		RDV.popUpOkButton();
		RDV.submitOrApprove();
		String actualMessageForSubmit = RDV.submitMessage(POReceipt.poDocNumber, "Receipt Delivery Verification","Transaction Version");
		Assert.assertTrue(actualMessageForSubmit.equalsIgnoreCase("Success"),
				"Expected message 'Success' but got: " + actualMessageForSubmit);
		RDV.generateAmarsaraf();
		String actualMessageForInvoice = RDV.submitMessage(POReceipt.poDocNumber, "Receipt Delivery Verification","Generate Amarsaraf");
		Assert.assertTrue(actualMessageForInvoice.equalsIgnoreCase("Success"),
				"Expected message 'Success' but got: " + actualMessageForInvoice);
		RDV.popUpOkButton();
		RDV.logout();
	}

	@Test(dependsOnMethods = { "ReceiptCreationFromPO.POReceipt.POReceiptCreation2" })
	public void createRDVWithDeductionHold() throws SQLException, InterruptedException {
		ReceiptDeliveryVerificationLocators RDV = new ReceiptDeliveryVerificationLocators(driver, wait, action);
		RDV.login("Openbravo", "12");
		RDV.openWindow("Receipt Delivery Verification");
		RDV.RDVPOFilter(POReceipt.poDocNumber);
		RDV.navigateToTransactionVersion();
		RDV.createNewLine();
		RDV.approvalType();
		RDV.certificateNumber();
		RDV.saveLine();
		RDV.matchAll();
		RDV.popUpOkButton();
		// Hold
		RDV.submitOrApprove();
		String actualMessageForSubmit = RDV.submitMessage(POReceipt.poDocNumber, "Receipt Delivery Verification","Transaction Version");
		Assert.assertTrue(actualMessageForSubmit.equalsIgnoreCase("Success"),
				"Expected message 'Success' but got: " + actualMessageForSubmit);
		RDV.generateAmarsaraf();
		String actualMessageForInvoice = RDV.submitMessage(POReceipt.poDocNumber, "Receipt Delivery Verification","Generate Amarsaraf");
		Assert.assertTrue(actualMessageForInvoice.equalsIgnoreCase("Success"),
				"Expected message 'Success' but got: " + actualMessageForInvoice);
		RDV.popUpOkButton();
		RDV.logout();

	}

}
