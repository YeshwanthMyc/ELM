package RDVCreation;

import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.exec.LogOutputStream;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import LocatorsOfWindows.ReceiptDeliveryVerificationLocators;
import ReceiptCreationFromPO.POReceipt;
import TestComponents.BaseClass;

public class RDVCreation extends BaseClass{
	POReceipt POR = new POReceipt();
	
	@Test(dependsOnMethods = {"ReceiptCreationFromPO.POReceipt.POReceiptCreation1","ReceiptCreationFromPO.POReceipt.purchaseOrderCreation"})
	public void createRDVWithNoDeduction() throws SQLException, InterruptedException {
		//launchApplication();
		ReceiptDeliveryVerificationLocators RDV = new ReceiptDeliveryVerificationLocators(driver, wait, action);
		RDV.login("Openbravo","12");
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
		RDV.generateAmarsaraf();
		RDV.popUpOkButton();
		RDV.logout();
	}
	
	@Test(dependsOnMethods = {"ReceiptCreationFromPO.POReceipt.POReceiptCreation2"})
	public void createRDVWithDeductionHold() throws SQLException, InterruptedException {
		//launchApplication();
		ReceiptDeliveryVerificationLocators RDV = new ReceiptDeliveryVerificationLocators(driver, wait, action);
		RDV.login("Openbravo","12");
		RDV.openWindow("Receipt Delivery Verification");
		RDV.RDVPOFilter(POReceipt.poDocNumber);
		RDV.navigateToTransactionVersion();
		RDV.createNewLine();
		RDV.approvalType();
		RDV.certificateNumber();
		RDV.saveLine();
		RDV.matchAll();
		RDV.popUpOkButton();
		RDV.submitOrApprove();
		
		//I will create hold logic here 
		RDV.generateAmarsaraf();
		RDV.popUpOkButton();
		RDV.logout();
	
	}
	
	
}
