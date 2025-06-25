package PaymentOut;

import java.sql.SQLException;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import LocatorsOfWindows.InvoiceLocators;
import LocatorsOfWindows.PaymentOutLocators;
import TestComponents.BaseClass;

public class PaymentOutForInvoiceWithAllDeductions extends BaseClass{

	@BeforeClass
	public void setupData() {
		commonData();
		poData();
		receiptData();
		rdvData();
		paymentData();
	}
	@Test(dataProvider = "PaymentOutData",dependsOnGroups = "AllDeductions")
	public void paymentOutForInvWithAllDed(HashMap<String, String> data) throws SQLException, InterruptedException {
		PaymentOutLocators paymentOut = new PaymentOutLocators(driver,wait,action);
		InvoiceLocators RDVInv = new InvoiceLocators(driver,wait,action);
		paymentOut.setDefaultRoleForLogin(data.get("Login_User_Name"), data.get("Login_Role"));
		paymentOut.login(data.get("Login_User_Name"), "12");
		RDVInv.openWindow("Purchase Invoice");
		RDVInv.documentNoFilter(invDocNumber);
		paymentOut.create_View_Payment();
		paymentOut.select_PaymentInstr_ParentSeq(data.get("Payment_Sequence"));
		logout();
		
		approvalresult =paymentOut.payment_Approval(poDocNumber, invDocNumber, currentDate);
		String Original_Message = (String) approvalresult.get("originalMessage");
		if (Original_Message.equalsIgnoreCase("Success")) {
			submitMessageSuccess = true;
		} else {
			submitMessageSuccess = false;
			System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
			Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
		}
		
		//
	}
}
