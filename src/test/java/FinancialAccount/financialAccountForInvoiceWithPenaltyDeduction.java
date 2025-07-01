package FinancialAccount;

import java.sql.SQLException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import LocatorsOfWindows.financialAccountLocators;
import TestComponents.BaseClass;

public class financialAccountForInvoiceWithPenaltyDeduction extends BaseClass {

	@BeforeClass
	public void setupData() {
		commonData();
	}
	@Test(dependsOnGroups = "paymentPenaltyDeduction")
	public void financialAccountPosting() throws SQLException, InterruptedException {
		financialAccountLocators fa = new financialAccountLocators(driver,wait,action);
		fa.login("Openbravo", "12");
		fa.openWindow("Financial Account");
		fa.selectMOFAccount();
		fa.clickAddMultiplePayment();
		fa.selectInvoiceFromPopUp(invDocNumber);
		fa.selectLatestPayment(invDocNumber);
		fa.post();
	}
}
