package LocatorsOfWindows;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import CommonUtilities.ReusableUtilities;

public class PaymentOutLocators extends ReusableUtilities {

	WebDriver driver;

	public PaymentOutLocators(WebDriver driver, WebDriverWait wait, Actions action) throws SQLException {
		super(driver, wait, action);
		this.driver = driver;
		getConnection();
	}

	public void setDefaultRoleForLogin(String Login_User_Name, String Login_Role) throws SQLException {
		String updateDefaultRoleForPaymentLogin = "UPDATE ad_user SET Default_Ad_Role_ID = \r\n"
				+ "(SELECT ad_role_id FROM ad_role WHERE name = '" + Login_Role + "')\r\n" + "WHERE username = '"
				+ Login_User_Name + "'";
		s.executeUpdate(updateDefaultRoleForPaymentLogin);
	}

	public void create_View_Payment() throws InterruptedException {
		WebElement create_ViewPayment = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
				"//td[@class='OBToolbarTextButton' and contains(., 'Create') and contains(., 'View Payments')]")));
		create_ViewPayment.click();

	}

	public void select_PaymentInstr_ParentSeq(String Payment_Sequence) throws InterruptedException {
		Thread.sleep(2000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
		List<WebElement> paymentsList = wait.until(ExpectedConditions
				.presenceOfAllElementsLocatedBy(By.xpath("//table[@class='ui-jqgrid-btable']/tbody/tr[@role='row']")));
		Select select;

		for (int i = 1; i < paymentsList.size(); i++) {
			paymentsList = driver.findElements(By.xpath("//table[@class='ui-jqgrid-btable']/tbody/tr[@role='row']"));
			WebElement row = paymentsList.get(i);
			row.click();
			Thread.sleep(500);

			// Scroll and select value
			WebElement paymentInstruction = row.findElement(By.xpath(".//select[contains(@id,'inpPaymentInst')]"));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", paymentInstruction);
			select = new Select(paymentInstruction);
			select.selectByValue("C");
			Thread.sleep(1000);

			// Parent Sequence
			WebElement parentSequence = row.findElement(By.xpath(".//select[contains(@id,'inpParentSeq')]"));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", parentSequence);
			select = new Select(parentSequence);
			select.selectByVisibleText(Payment_Sequence);
		}

		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@id='Save_BTNname']"))).click();
		driver.switchTo().defaultContent();
		Thread.sleep(3000);
	}

	public Map<String, Object> payment_Approval(String poDocNumber, String invDocNumber, String currentDate)
			throws SQLException, InterruptedException {

		String pending_Role_In_Db = null;
		String paymentPendingRole;
		String paymentpendingUserResult;
		String paymentpendingUser;

		boolean submitMessageSuccess = false;
		boolean submitMessageSuccessResult = false;
		String originalMessage = null;
		Map<String, Object> approvalMessageresult = new HashMap<>();

		while (true) {
			String paymentApprovalQuery = "select Pendingapproval from efin_po_approval where FIN_Payment_ID in \r\n"
					+ "(select FIN_Payment_ID from FIN_Payment where EM_Efin_Invoice_ID in\r\n"
					+ "(select c_invoice_id from c_invoice where documentno='" + invDocNumber
					+ "') and EM_Efin_Status='EFIN_WFA')\r\n" + "ORDER BY created DESC LIMIT 1";
			ResultSet paymentApprovalQueryResult = s.executeQuery(paymentApprovalQuery);
			if (paymentApprovalQueryResult.next()) {
				pending_Role_In_Db = paymentApprovalQueryResult.getString("Pendingapproval");

				if (pending_Role_In_Db == null || pending_Role_In_Db.isEmpty()) {
					pending_Role_In_Db = null;
					break;
				}

				if (pending_Role_In_Db.contains("/")) {
					String[] parts = pending_Role_In_Db.split("/");
					paymentPendingRole = (parts.length > 1) ? parts[0] : parts[1];
					paymentPendingRole = paymentPendingRole.trim();

					paymentpendingUserResult = (parts.length > 1) ? parts[1] : parts[0];
					if (paymentpendingUserResult.contains("-")) {
						String[] userParts = paymentpendingUserResult.split("-");
						paymentpendingUser = (userParts.length > 1) ? userParts[1] : userParts[0];
						paymentpendingUser = paymentpendingUser.trim();
					} else {
						String pendingUserQuery = "select Pendingapproval from efin_po_approval where FIN_Payment_ID in \r\n"
								+ "(select FIN_Payment_ID from FIN_Payment where EM_Efin_Invoice_ID in\r\n"
								+ "(select c_invoice_id from c_invoice where documentno='" + invDocNumber
								+ "') ) ORDER BY created DESC LIMIT 1";
						ResultSet pendingUserQueryResult = s.executeQuery(pendingUserQuery);
						if (pendingUserQueryResult.next()) {
							paymentpendingUser = pendingUserQueryResult.getString("Pendingapproval");
						} else {
							throw new RuntimeException("No active user found for role: " + paymentPendingRole);
						}

					}
				} else {
					paymentPendingRole = pending_Role_In_Db;
					paymentPendingRole = paymentPendingRole.trim();
					String pendingUserQuery = "SELECT username FROM ad_user "
							+ "JOIN ad_user_roles ON ad_user.ad_user_id = ad_user_roles.ad_user_id "
							+ "JOIN ad_role ON ad_user_roles.ad_role_id = ad_role.ad_role_id "
							+ "WHERE ad_role.name = '" + paymentPendingRole + "' " + "AND ad_user_roles.isactive = 'Y' "
							+ "AND username <> 'Openbravo' LIMIT 1";
					ResultSet pendingUserResult = s.executeQuery(pendingUserQuery);

					if (pendingUserResult.next()) {
						paymentpendingUser = pendingUserResult.getString("username");
					} else {
						throw new RuntimeException("No active user found for role: " + paymentPendingRole);
					}
				}

				// Update default role
				String updateDefaultRoleQuery = "UPDATE ad_user SET Default_Ad_Role_ID = "
						+ "(SELECT ad_role_id FROM ad_role WHERE name = '" + paymentPendingRole + "') "
						+ "WHERE username = '" + paymentpendingUser + "'";
				s.executeUpdate(updateDefaultRoleQuery);

				// Login
				login(paymentpendingUser, "12");

				// Approval
				InvoiceLocators RDVInv = new InvoiceLocators(driver, wait, action);
				RDVInv.openWindow("Payment Out");
				wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinInvoice']")))
						.sendKeys(invDocNumber);
				Thread.sleep(1000);
				action.sendKeys(Keys.ENTER).build().perform();

				Thread.sleep(3000);
				List<WebElement> paymentsToBeApproved = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
						By.xpath("//table[@class='listTable']/tbody/tr/td/div/nobr[contains(text(),'" + invDocNumber
								+ " - " + currentDate + "')]")));

				for (int i = 0; i < paymentsToBeApproved.size() - 1; i++) {
					paymentsToBeApproved = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
							By.xpath("//table[@class='listTable']/tbody/tr/td/div/nobr[contains(text(),'" + invDocNumber
									+ " - " + currentDate + "')]")));
					WebElement invoiceRow = paymentsToBeApproved.get(i);
					invoiceRow.click();

					// Approve
					submitOrApprove();

					Map<String, Object> SubmitMessageresult = submitMessageValidation(poDocNumber, "Payment Out",
							"Approval", invDocNumber);
					submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
					originalMessage = (String) SubmitMessageresult.get("originalMessage");
					if (submitMessageSuccessResult) {
						submitMessageSuccess = true;
					} else {
						submitMessageSuccess = false;
						break;
					}

				}

			} else
				break;

		}
		if (pending_Role_In_Db == null || pending_Role_In_Db.isEmpty()) {
			logout();
		}
		approvalMessageresult.put("submitMessageSuccessResult", submitMessageSuccessResult);
		approvalMessageresult.put("originalMessage", originalMessage);
		return approvalMessageresult;

	}
}
