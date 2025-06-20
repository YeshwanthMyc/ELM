package LocatorsOfWindows;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

public class ReceiptDeliveryVerificationLocators extends ReusableUtilities {
	WebDriver driver;
	private static int certificateNo = 1;
	public static String certficateNumber = null;

	public ReceiptDeliveryVerificationLocators(WebDriver driver, WebDriverWait wait, Actions action)
			throws SQLException {
		super(driver, wait, action);
		this.driver = driver;
		getConnection();
	}

	public void enterPONumber(String poNumber) throws InterruptedException {
		Thread.sleep(1000);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='salesOrder'])[2]")));
		driver.findElement(By.xpath("(//input[@name='salesOrder'])[2]")).sendKeys(poNumber);
		Thread.sleep(1000);
		action.sendKeys(Keys.ENTER).build().perform();
	}

	public void RDVPOFilter(String poNumber) throws InterruptedException {
		// PO filter
		Thread.sleep(2000);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='salesOrder']")));
		driver.findElement(By.xpath("//input[@name='salesOrder']")).sendKeys(poNumber);
		Thread.sleep(1000);
		action.sendKeys(Keys.ENTER).build().perform();

		// Select filtered record
		Thread.sleep(1000);
		WebElement filteredPORow = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("(//div/nobr[contains(text(),'" + poNumber + "')])[1]")));
		action.moveToElement(filteredPORow).click().perform();
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", filteredPORow);
	}

	public void navigateToTransactionVersion() throws InterruptedException {
		WebElement linesTab = wait.until(
				ExpectedConditions.elementToBeClickable(By.xpath("//td[contains(text(),'Transaction versions')]")));
		action.moveToElement(linesTab).doubleClick().build().perform();
		Thread.sleep(500);
	}

	public void approvalType() throws InterruptedException {
		By approvalTypeLocator = By.xpath("(//input[@name='approvalType'])[2]");
		WebElement approvalTypeInput = wait.until(ExpectedConditions.elementToBeClickable(approvalTypeLocator));

		approvalTypeInput.clear();
		approvalTypeInput.sendKeys("Running RDV");
		approvalTypeInput.sendKeys(Keys.ENTER);
	}

	public void certificateNumber() throws InterruptedException {
		certficateNumber = "Automation" + String.valueOf(certificateNo);
		Thread.sleep(1000);
		By certificateNoLocator = By.xpath("(//input[@name='certificateNo'])[2]");
		wait.until(ExpectedConditions.presenceOfElementLocated(certificateNoLocator));
		WebElement certificateNoInput = driver.findElement(certificateNoLocator);
		certificateNoInput.sendKeys(certficateNumber);
		certificateNo++;
	}

	public void openTransactionVersion() throws InterruptedException {
		Thread.sleep(1000);
		WebElement filteredTxrnVersion = wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//tr/td/div[@role='presentation']/nobr[contains(text(),'" + certficateNumber + "')]")));
		action.moveToElement(filteredTxrnVersion).click().perform();
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", filteredTxrnVersion);
	}

	public void matchAll() {
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Match All')]")));
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Match All')]")))
				.click();
	}

	public void enterHoldDetails(String holdName, String holdAmounttoBeEntered) throws InterruptedException {
		// Hold action
		Thread.sleep(1000);
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Hold Action Txn')]")));
		wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Hold Action Txn')]"))).click();

		// Move to Frame
		Thread.sleep(2000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));

		// Click on Add
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Add')]")));
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Add')]"))).click();

		// DropDown
		WebElement holdPopUpDropDown = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//select[@name='holdtype']")));
		Select holdNameDropdown = new Select(holdPopUpDropDown);
		holdNameDropdown.selectByContainsVisibleText(holdName);

		// hold amount
		WebElement holdAmount = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//input[@name='holdamount'])[2]")));
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", holdAmount);
		holdAmount.sendKeys(holdAmounttoBeEntered);
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Save')]"))).click();
		Thread.sleep(1000);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='Dialog_OK']"))).click();
		Thread.sleep(1000);
		driver.switchTo().defaultContent();

		// Close Hold Pop-up
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("(//div[contains(@class,'OBPopupIconClose') and contains(@onscroll,'closeButton')])[1]")))
				.click();
	}

	public void enterPenaltyDetails(String penaltyName,String revenueAccount) throws InterruptedException {
		// Penalty Action(10% penalty)
		Thread.sleep(1000);
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Bulk Penalty')]")));
		wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Bulk Penalty')]"))).click();

		Thread.sleep(2000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("RdvApplyPenalty")));

		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Add')]")));
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Add')]"))).click();

		WebElement penaltyPopUpDropDown = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//select[@name='penaltytype']")));
		Select penaltyNameDropdown = new Select(penaltyPopUpDropDown);
		penaltyNameDropdown.selectByContainsVisibleText(penaltyName);

		// Select revenue account
		WebElement accountDropDown = wait
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//select[@name='accounttype']")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", accountDropDown);
		Select accountName = new Select(accountDropDown);
		accountName.selectByContainsVisibleText("Adjustment");
		Thread.sleep(1000);
		WebElement uniqueCodeDropDown = wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("(//span[@class='select2-selection select2-selection--single'])[2]")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", uniqueCodeDropDown);
		Thread.sleep(500);
		action.moveToElement(
				driver.findElement(By.xpath("(//span[@class='select2-selection select2-selection--single'])[2]")))
				.click().build().perform();

		Thread.sleep(1000);
		WebElement accountNumber = wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("(//span[@class='select2-search select2-search--dropdown'])/input")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", accountNumber);
		accountNumber.sendKeys(revenueAccount);

		Thread.sleep(1500);
		action.sendKeys(Keys.ENTER).build().perform();

		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Save')]"))).click();
		Thread.sleep(1000);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='Dialog_OK']"))).click();
		Thread.sleep(1000);
		driver.switchTo().defaultContent();
	}

	public void enterExternalPenaltyDetails(String External_Penalty_Name, String penaltyAmountToBeEntered,String externalPenaltySupplierName)
			throws InterruptedException {
		// Penalty Action(External Penalty)
		Thread.sleep(1000);
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Bulk Penalty')]")));
		wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Bulk Penalty')]"))).click();

		Thread.sleep(2000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("RdvApplyPenalty")));

		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Add')]")));
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Add')]"))).click();

		WebElement penaltyPopUpDropDown1 = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//select[@name='penaltytype']")));
		Select penaltyName1 = new Select(penaltyPopUpDropDown1);
		penaltyName1.selectByContainsVisibleText(External_Penalty_Name);

		WebElement penaltyAmount = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//input[@name='penaltyamount'])[2]")));
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", penaltyAmount);
		penaltyAmount.sendKeys(penaltyAmountToBeEntered);

		Thread.sleep(1500);
		WebElement penaltyBusinessPartnerDropDown = wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("(//span[@class='select2-selection__rendered'])[1]")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
				penaltyBusinessPartnerDropDown);
		Thread.sleep(500);
		action.moveToElement(driver.findElement(By.xpath("(//span[@class='select2-selection__rendered'])[1]"))).click()
				.build().perform();

		Thread.sleep(500);
		WebElement externalContractorName = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("(//span[@class='select2-search select2-search--dropdown'])/input")));
		externalContractorName.sendKeys(externalPenaltySupplierName);

		Thread.sleep(500);
		action.sendKeys(Keys.ENTER).build().perform();

		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Save')]"))).click();
		Thread.sleep(1000);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='Dialog_OK']"))).click();
		Thread.sleep(1000);
		driver.switchTo().defaultContent();
	}

	public void generateAmarsaraf(String windowName, String poNumber) throws InterruptedException {
		openWindow(windowName);
		RDVPOFilter(poNumber);
		navigateToTransactionVersion();
		openTransactionVersion();
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Generate Amarsaraf')]")));
		wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Generate Amarsaraf')]"))).click();
	}

	public double getMatchedAmount(String PoNumber) throws SQLException {
		String matchedAmount = "";
		String matchedAmountQuery = "select match_amt from Efin_RDVTxn where Efin_RDV_id in\r\n"
				+ "(select Efin_RDV_id from Efin_RDV where c_order_id in\r\n"
				+ "(select c_order_id from c_order where documentNo='" + PoNumber + "'))\r\n"
				+ " ORDER BY created DESC limit 1";

		ResultSet matchedAmountResult = s.executeQuery(matchedAmountQuery);
		if (matchedAmountResult.next()) {
			matchedAmount = matchedAmountResult.getString("match_amt");
		}
		double macthedAmt = Double.parseDouble(matchedAmount);
		return macthedAmt;
	}

	public double getNetMatchedAmount(String PoNumber) throws SQLException {
		String NetmatchedAmount = "";
		String NetmatchedAmountQuery = "select Netmatch_Amt from Efin_RDVTxn where Efin_RDV_id in\r\n"
				+ "(select Efin_RDV_id from Efin_RDV where c_order_id in\r\n"
				+ "(select c_order_id from c_order where documentNo='" + PoNumber + "'))\r\n"
				+ " ORDER BY created DESC limit 1";

		ResultSet NetmatchedAmountResult = s.executeQuery(NetmatchedAmountQuery);
		if (NetmatchedAmountResult.next()) {
			NetmatchedAmount = NetmatchedAmountResult.getString("Netmatch_Amt");
		}
		double NetmacthedAmt = Double.parseDouble(NetmatchedAmount);
		return NetmacthedAmt;
	}

	public String getTxrnId(String PoNumber) throws SQLException {
		String TxrnId = null;
		String TxrnIdQuery = "select Efin_RDVTxn_id from Efin_RDVTxn where Efin_RDV_id in\r\n"
				+ "(select Efin_RDV_id from Efin_RDV where c_order_id in\r\n"
				+ "(select c_order_id from c_order where documentNo='" + PoNumber + "'))\r\n"
				+ "ORDER BY created DESC limit 1";

		ResultSet TxrnIdResult = s.executeQuery(TxrnIdQuery);
		if (TxrnIdResult.next()) {
			TxrnId = TxrnIdResult.getString("Efin_RDVTxn_id");
		}
		return TxrnId;
	}

	public String getInvoiceId(String PoNumber) throws SQLException {
		String invoiceId = null;
		String invoiceIdQuery = "select c_invoice_id from Efin_RDVTxn where Efin_RDV_id in\r\n"
				+ "(select Efin_RDV_id from Efin_RDV where c_order_id in\r\n"
				+ "(select c_order_id from c_order where documentNo='" + PoNumber + "'))\r\n"
				+ "ORDER BY created DESC limit 1";

		ResultSet invoiceIdQueryResult = s.executeQuery(invoiceIdQuery);
		if (invoiceIdQueryResult.next()) {
			invoiceId = invoiceIdQueryResult.getString("c_invoice_id");
		}
		return invoiceId;
	}

	public double getHoldAmount(String PoNumber) throws SQLException {
		String holdAmount = "";
		String holdAmountQuery = "select holdamount from Efin_RDVTxn where Efin_RDV_id in\r\n"
				+ "(select Efin_RDV_id from Efin_RDV where c_order_id in\r\n"
				+ "(select c_order_id from c_order where documentNo='" + PoNumber + "'))\r\n"
				+ "ORDER BY created DESC limit 1";

		ResultSet holdAmountResult = s.executeQuery(holdAmountQuery);
		if (holdAmountResult.next()) {
			holdAmount = holdAmountResult.getString("holdamount");
		}
		double holdAmt = Double.parseDouble(holdAmount);
		return holdAmt;
	}

	public double getPenaltyAmount(String PoNumber) throws SQLException {
		String penaltyAmount = "";
		String holdAmountQuery = "select penalty_amt from Efin_RDVTxn where Efin_RDV_id in\r\n"
				+ "(select Efin_RDV_id from Efin_RDV where c_order_id in\r\n"
				+ "(select c_order_id from c_order where documentNo='" + PoNumber + "'))\r\n"
				+ "ORDER BY created DESC limit 1";

		ResultSet penaltyAmountResult = s.executeQuery(holdAmountQuery);
		if (penaltyAmountResult.next()) {
			penaltyAmount = penaltyAmountResult.getString("penalty_amt");
		}
		double penaltyAmt = Double.parseDouble(penaltyAmount);
		return penaltyAmt;
	}

	

	public Map<String, Object> RDVApproval(String TxrnId,String poDocNumber,String windowName) throws SQLException, InterruptedException {
		String rdvPendingRoleInDB = null;
		String rdvPendingRole = null;
		String rdvpendingUser = null;
		boolean submitMessageSuccess = false;
		boolean submitMessageSuccessResult=false;
		String originalMessage=null;
		Map<String, Object> approvalMessageresult = new HashMap<>();
		while (true) {
			// Fetch pending role from DB
			String rdvPendingRoleQuery = "SELECT pendingapproval FROM efin_rdvacthist WHERE Efin_Rdvtxn_ID = '"
					+ TxrnId + "'  ORDER BY created DESC LIMIT 1";
			ResultSet rdvPendingRoleQueryResult = s.executeQuery(rdvPendingRoleQuery);

			if (rdvPendingRoleQueryResult.next()) {
				rdvPendingRoleInDB = rdvPendingRoleQueryResult.getString("pendingapproval");

				if (rdvPendingRoleInDB == null || rdvPendingRoleInDB.isEmpty()) {
					rdvPendingRole = null;
					break;
				}

				if (rdvPendingRoleInDB.contains("/")) {
					String[] parts = rdvPendingRoleInDB.split("/");
					rdvPendingRole = (parts.length > 1) ? parts[0] : parts[1];
					rdvPendingRole = rdvPendingRole.trim();
				} else {
					rdvPendingRole = rdvPendingRoleInDB;
					rdvPendingRole = rdvPendingRole.trim();
				}

				// Fetch user with that role
				String pendingUserQuery = "SELECT username FROM ad_user "
						+ "JOIN ad_user_roles ON ad_user.ad_user_id = ad_user_roles.ad_user_id "
						+ "JOIN ad_role ON ad_user_roles.ad_role_id = ad_role.ad_role_id " + "WHERE ad_role.name = '"
						+ rdvPendingRole + "' " + "AND ad_user_roles.isactive = 'Y' "
						+ "AND username <> 'Openbravo' LIMIT 1";
				ResultSet pendingUserResult = s.executeQuery(pendingUserQuery);

				if (pendingUserResult.next()) {
					rdvpendingUser = pendingUserResult.getString("username");
				} else {
					throw new RuntimeException("No active user found for role: " + rdvPendingRole);
				}

				// Update default role
				String updateDefaultRoleQuery = "UPDATE ad_user SET Default_Ad_Role_ID = "
						+ "(SELECT ad_role_id FROM ad_role WHERE name = '" + rdvPendingRole + "') "
						+ "WHERE username = '" + rdvpendingUser + "'";
				s.executeUpdate(updateDefaultRoleQuery);

				// Login and approve
				login(rdvpendingUser, "12");
				System.out.println("Approval In-Process");
				openWindow("Receipt Delivery Verification");
				RDVPOFilter(poDocNumber);
				navigateToTransactionVersion();
				openTransactionVersion();

				submitOrApprove();
				Map<String, Object> SubmitMessageresult = submitMessageValidation(poDocNumber,windowName,"Approval",null);
				submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
				originalMessage = (String) SubmitMessageresult.get("originalMessage");
				

				if (submitMessageSuccessResult) {
					submitMessageSuccess = true;
				} else {
					submitMessageSuccess = false;
					break; 
				}

				logout();
			} else
				break;

		}
		approvalMessageresult.put("submitMessageSuccessResult", submitMessageSuccessResult);
		approvalMessageresult.put("originalMessage", originalMessage);	
		return approvalMessageresult;
	}

}
