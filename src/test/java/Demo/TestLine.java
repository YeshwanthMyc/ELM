package Demo;

import static org.testng.Assert.expectThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import CommonUtilities.ReusableUtilities;
import LocatorsOfWindows.InvoiceLocators;
import TestComponents.BaseClass;

public class TestLine extends BaseClass {
	public static String DB_URL = "jdbc:postgresql://localhost:5932/mainaccrual";
	public static String DB_USER = "tad";
	public static String DB_PASSWORD = "tad";
	public static Connection con;
	public static Statement s;

	@BeforeClass
	public void getConnection() throws SQLException {
		con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		s = con.createStatement(); //
	}

	@Test
	public void paymentOut() throws InterruptedException, SQLException {

		driver.findElement(By.xpath("//input[@id='user']")).sendKeys("404011");
		driver.findElement(By.xpath("//input[@id='password']")).sendKeys("12");
		driver.findElement(By.xpath("//button[@id='buttonOK']")).click();

		// Set Default role
		String updateDefaultRoleForPaymentLogin = "UPDATE ad_user SET Default_Ad_Role_ID = \r\n"
				+ "(SELECT ad_role_id FROM ad_role WHERE name = 'موظف إدارة المدفوعات - Payments Employee')\r\n"
				+ "WHERE username = '404011'";
		s.executeUpdate(updateDefaultRoleForPaymentLogin);

		// Open Invoice window
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]")))
				.click();

		WebElement quickLaunchTextBox = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
		quickLaunchTextBox.sendKeys("Purchase Invoice");
		Thread.sleep(500);
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@class='listTable']//td/div/nobr[text()='Purchase Invoice']")))
				.click();

		// Clear Filter
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("(//td[@class='OBGridFilterFunnelIcon']/div/div/img)[1]"))).click();

		// Document No Filter
		WebElement documentNoFilter = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='documentNo']")));
		documentNoFilter.sendKeys("250000155");

		WebElement filteredRow = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//div[@role='presentation']/nobr[text()='" + 250000155 + "']")));
		action.moveToElement(filteredRow).click().build().perform();

		// create/view payment
		WebElement create_ViewPayment = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
				"//td[@class='OBToolbarTextButton' and contains(., 'Create') and contains(., 'View Payments')]")));
		create_ViewPayment.click();

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
			select.selectByValue("C"); // Select شيك وزاري
			Thread.sleep(1000);

			// Parent Sequence
			WebElement parentSequence = row.findElement(By.xpath(".//select[contains(@id,'inpParentSeq')]"));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", parentSequence);
			select = new Select(parentSequence);
			select.selectByVisibleText("1000 - 10000");
		}

		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@id='Save_BTNname']"))).click();	
		driver.switchTo().defaultContent();
		Thread.sleep(3000);
		
		logout();
		approvalresult = payment_Approval();
		String Original_Message = (String) approvalresult.get("originalMessage");
		if (Original_Message.equalsIgnoreCase("Success")) {
			submitMessageSuccess = true;
		} else {
			submitMessageSuccess = false;
			System.out.println("Expected 'Success' but got: " + actualMessageForSubmittext);
			Assert.fail("Expected 'Success' but got: " + actualMessageForSubmittext);
		}

	}

	public Map<String, Object> payment_Approval() throws SQLException, InterruptedException {
		String pending_Role_In_Db;
		String paymentPendingRole;
		String paymentpendingUserResult;
		String paymentpendingUser;

		Map<String, Object> approvalMessageresult = new HashMap<>();
		while (true) {
			String paymentApprovalQuery = "select Pendingapproval from efin_po_approval where FIN_Payment_ID in \r\n"
					+ "(select FIN_Payment_ID from FIN_Payment where EM_Efin_Invoice_ID in\r\n"
					+ "(select c_invoice_id from c_invoice where documentno='250000155') and EM_Efin_Status='EFIN_WFA')\r\n"
					+ "ORDER BY created DESC LIMIT 1";
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
								+ "(select c_invoice_id from c_invoice where documentno='250000155') ) ORDER BY created DESC LIMIT 1";
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

				driver.findElement(By.xpath("//input[@id='user']")).sendKeys(paymentpendingUser);
				driver.findElement(By.xpath("//input[@id='password']")).sendKeys("12");
				driver.findElement(By.xpath("//button[@id='buttonOK']")).click();

				// Approval
				wait.until(ExpectedConditions
						.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]"))).click();
				WebElement quickLaunchTextBox2 = wait.until(ExpectedConditions
						.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
				quickLaunchTextBox2.sendKeys("Payment Out");
				Thread.sleep(500);
				wait.until(ExpectedConditions.elementToBeClickable(
						By.xpath("//table[@class='listTable']//td/div/nobr[text()='Payment Out']"))).click();

				// Invoice filter
				wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinInvoice']")))
						.sendKeys("250000155");
				Thread.sleep(1000);
				action.sendKeys(Keys.ENTER).build().perform();

				Thread.sleep(3000);
				List<WebElement> paymentsToBeApproved = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
						.xpath("//table[@class='listTable']/tbody/tr/td/div/nobr[contains(text(),'250000155 - 20-06-2025')]")));
				for (int i = 0; i < paymentsToBeApproved.size() - 1; i++) {
					paymentsToBeApproved = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(
							"//table[@class='listTable']/tbody/tr/td/div/nobr[contains(text(),'250000155 - 20-06-2025')]")));
					WebElement invoiceRow = paymentsToBeApproved.get(i);
					invoiceRow.click();

					// Approve
					Thread.sleep(2000);
					WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
							"//td[@class='OBToolbarTextButton' and (contains(text(),'Submit') or contains(text(),'Approve'))]")));
					action.moveToElement(submitButton).click().build().perform();
					Thread.sleep(2000);
					wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
					wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
					wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainframe")));
					WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonOK")));
					okButton.click();
					driver.switchTo().defaultContent();
					Thread.sleep(1500);

					ReusableUtilities rs = new ReusableUtilities(driver, wait, action);
					Map<String, Object> SubmitMessageresult = rs.submitMessageValidation(poDocNumber, "Payment Out",
							"Approval", "250000155");
					submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
					originalMessage = (String) SubmitMessageresult.get("originalMessage");
					if (submitMessageSuccessResult) {
						submitMessageSuccess = true;
					} else {
						submitMessageSuccess = false;
						break;
					}

				}

			}else break;

		}
		logout();
		approvalMessageresult.put("submitMessageSuccessResult", submitMessageSuccessResult);
		approvalMessageresult.put("originalMessage", originalMessage);
		return approvalMessageresult;

	}
}
