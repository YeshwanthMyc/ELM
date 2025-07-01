package FinancialAccount;

import java.sql.SQLException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import LocatorsOfWindows.financialAccountLocators;
import TestComponents.BaseClass;

public class financialAccountForInvoiceWithAllDeduction extends BaseClass {

	@BeforeClass
	public void setupData() {
		commonData();
	}
	@Test(dependsOnGroups = "paymentAllDeduction")
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
