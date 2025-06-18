package LocatorsOfWindows;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import CommonUtilities.ReusableUtilities;

public class RDVInvoiceWithNoDeductionLocators extends ReusableUtilities {
	WebDriver driver;

	public RDVInvoiceWithNoDeductionLocators(WebDriver driver, WebDriverWait wait, Actions action) throws SQLException {
		super(driver, wait, action);
		this.driver = driver;
		getConnection();
	}

	public String getTempDocNumber(String noDeductionTxrnId) throws SQLException {
		String tempDocNumber = null;
		String documentNoQuery = "select DocumentNo from C_Invoice where C_Invoice_id in \r\n"
				+ "(select C_Invoice_ID from Efin_RDVTxn where Efin_RDVTxn_id ='" + noDeductionTxrnId + "')";
		ResultSet tempDocNumQueryResult = s.executeQuery(documentNoQuery);
		while (tempDocNumQueryResult.next()) {
			tempDocNumber = tempDocNumQueryResult.getString("DocumentNo");
			System.out.println(tempDocNumber);
			break;
		}
		return tempDocNumber;
	}

	public void openInvoiceWindow() throws InterruptedException {
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]")))
				.click();
		WebElement quickLaunchTextBox = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
		quickLaunchTextBox.sendKeys("Purchase Invoice");
		Thread.sleep(500);
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@class='listTable']//td/div/nobr[text()='Purchase Invoice']")))
				.click();
	}

	public void documentNoFilter(String tempDocNumber) {
		WebElement documentNoFilter = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='documentNo']")));
		documentNoFilter.sendKeys(tempDocNumber);

		WebElement filteredRow = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//div[@role='presentation']/nobr[text()='" + tempDocNumber + "']")));
		action.moveToElement(filteredRow).click().build().perform();
	}

	public void mofRequestNumber(String mofNumber, String tempDocNumber) {
		WebElement filteredRow = oneMinuteWait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//div[@role='presentation']/nobr[text()='" + tempDocNumber + "']")));
		action.moveToElement(filteredRow).doubleClick().build().perform();

		WebElement mofReqNum = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinMofreqno']")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", mofReqNum);
		mofReqNum.sendKeys(mofNumber);
	}

	public void description(String descp) throws InterruptedException {
		Thread.sleep(1000);
		for (int i = 0; i < 3; i++) {
			try {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//textarea[@name='description']")))
						.sendKeys(descp);
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}
	}

	public void enterTaxDetails(String taxName) throws InterruptedException {
		// Tax Method
		for (int i = 0; i < 3; i++) {
			try {
				WebElement isTaxLine = wait.until(ExpectedConditions.elementToBeClickable(
						By.xpath("//div[@class='OBFormFieldLabel' and text()='Is Tax Line']/span")));
				isTaxLine.click();
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}

		for (int i = 0; i < 3; i++) {
			try {
				WebElement taxMethod = wait
						.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinTaxMethod']")));
				taxMethod.sendKeys(taxName);
				Thread.sleep(1500);
				action.sendKeys(Keys.ENTER).build().perform();
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}
	}

	public void enterNoClaimDetails(String currentDate) throws InterruptedException {
		// No Claim Details
		for (int i = 0; i < 3; i++) {
			try {
				WebElement noClaim = wait.until(ExpectedConditions
						.elementToBeClickable(By.xpath("//div[@class='OBFormFieldLabel' and text()='No Claim']/span")));
				noClaim.click();
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}

		for (int i = 0; i < 3; i++) {
			try {
				WebElement inwardNo = wait.until(
						ExpectedConditions.presenceOfElementLocated(By.xpath("(//input[@name='efinInwardno'])[2]")));
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", inwardNo);
				inwardNo.sendKeys("12345");
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}

		for (int i = 0; i < 3; i++) {
			try {
				Thread.sleep(1000);
				WebElement inwardDate = wait.until(ExpectedConditions
						.presenceOfElementLocated(By.xpath("//input[@name='efinInwarddateGreg_dateTextField']")));
				inwardDate.click();
				inwardDate.sendKeys(currentDate);
				Thread.sleep(500);
				action.sendKeys(Keys.TAB).build().perform();
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}
	}

	public void enterSupplierInvNumberAndDate(String currentDate) throws InterruptedException {
		for (int i = 0; i < 3; i++) {
			try {
				wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinSupinvno']")))
						.sendKeys("1223");
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}

		Thread.sleep(1000);
		WebElement inwardDate = wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//input[@name='efinSupinvdateG_dateTextField']")));
		inwardDate.click();
		inwardDate.sendKeys(currentDate);
		Thread.sleep(500);
		action.sendKeys(Keys.TAB).build().perform();
	}

	public void addTaxLines() throws InterruptedException {
		for (int i = 0; i < 3; i++) {
			try {
				WebElement addTaxLines = wait.until(ExpectedConditions.elementToBeClickable(
						By.xpath("//td[@class='OBToolbarTextButton' and text()='Add Tax Lines']")));
				addTaxLines.click();

				wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
				wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
				wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainframe")));
				WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonOK")));
				okButton.click();
				driver.switchTo().defaultContent();

				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}
	}

	public boolean amountValidations(String noDeductionTxrnId, String tempDocNumber)
			throws NumberFormatException, SQLException {
		double netMatchAmount = 0;
		double mainLine = 0;
		double taxLine = 0;
		boolean amountValidations = false;
		String netMatchAmtQuery = "select Netmatch_Amt from Efin_RDVTxn where Efin_RDVTxn_id ='" + noDeductionTxrnId
				+ "'";
		ResultSet netMatchAmtQueryResult = s.executeQuery(netMatchAmtQuery);
		while (netMatchAmtQueryResult.next()) {
			String Netmatch_Amt = netMatchAmtQueryResult.getString("Netmatch_Amt");
			netMatchAmount = Double.parseDouble(Netmatch_Amt);
			System.out.println(netMatchAmount);
			break;
		}

		String noDeductionLineNetAmountWithoutTaxQuery = "select LineNetAmt from C_InvoiceLine where C_Invoice_id in \r\n"
				+ "(select C_Invoice_id from C_Invoice where DocumentNo='" + tempDocNumber + "') and EM_Efin_Istax='N'";
		ResultSet noDeductionLineNetAmountWithoutTaxQueryResult = s
				.executeQuery(noDeductionLineNetAmountWithoutTaxQuery);
		while (noDeductionLineNetAmountWithoutTaxQueryResult.next()) {
			String Line_Net_Amt = noDeductionLineNetAmountWithoutTaxQueryResult.getString("LineNetAmt");
			mainLine = Double.parseDouble(Line_Net_Amt);
			System.out.println(mainLine);
			break;
		}

		String noDeductionLineNetAmountWithTaxQuery = "select LineNetAmt from C_InvoiceLine where C_Invoice_id in \r\n"
				+ "(select C_Invoice_id from C_Invoice where DocumentNo='" + tempDocNumber + "') and EM_Efin_Istax='Y'";
		ResultSet noDeductionLineNetAmountWithTaxQueryResult = s.executeQuery(noDeductionLineNetAmountWithTaxQuery);
		while (noDeductionLineNetAmountWithTaxQueryResult.next()) {
			String Line_Net_Amt = noDeductionLineNetAmountWithTaxQueryResult.getString("LineNetAmt");
			taxLine = Double.parseDouble(Line_Net_Amt);
			System.out.println(taxLine);
			break;
		}

		if (netMatchAmount == mainLine && taxLine == mainLine * 0.15) {
			amountValidations = true;
		}
		return amountValidations;
	}

	public Map<String, Object> invoiceApproval(String poDocNumber, String docNumber)
			throws SQLException, InterruptedException {

		boolean submitMessageSuccess = false;
		boolean submitMessageSuccessResult = false;
		String originalMessage = null;
		Map<String, Object> approvalMessageresult = new HashMap<>();

		String pending_Role_In_Db = null;
		String invoicePendingRole = null;
		String invpendingUser = null;
		String invpendingUserResult = null;
		while (true) {
			String invApprovalQuery = "select Pendingapproval from efin_purchasein_app_hist where c_invoice_id in(\r\n"
					+ "select c_invoice_id from c_invoice where documentno='" + docNumber
					+ "')ORDER BY created DESC LIMIT 1";
			ResultSet invApprovalQueryResult = s.executeQuery(invApprovalQuery);
			if (invApprovalQueryResult.next()) {
				pending_Role_In_Db = invApprovalQueryResult.getString("Pendingapproval");
				if (pending_Role_In_Db == null || pending_Role_In_Db.isEmpty()) {
					pending_Role_In_Db = null;
					break;
				}

				if (pending_Role_In_Db.contains("/")) {
					String[] parts = pending_Role_In_Db.split("/");
					invoicePendingRole = (parts.length > 1) ? parts[0] : parts[1];
					invoicePendingRole = invoicePendingRole.trim();

					invpendingUserResult = (parts.length > 1) ? parts[1] : parts[0];
					if (invpendingUserResult.contains("-")) {
						String[] userParts = invpendingUserResult.split("-");
						invpendingUser = (userParts.length > 1) ? userParts[1] : userParts[0];
						invpendingUser = invpendingUser.trim();
					} else {
						String pendingUserQuery = "select Pendingapproval from efin_purchasein_app_hist where c_invoice_id in(\\r\\n\"\r\n"
								+ "+ \"select c_invoice_id from c_invoice where documentno='\"+docNumber+\"')ORDER BY created DESC LIMIT 1";
						ResultSet pendingUserResult = s.executeQuery(pendingUserQuery);
						if (pendingUserResult.next()) {
							invpendingUser = pendingUserResult.getString("Pendingapproval");
						} else {
							throw new RuntimeException("No active user found for role: " + invoicePendingRole);
						}
						
					}
				} else {
					invoicePendingRole = pending_Role_In_Db;
					invoicePendingRole = invoicePendingRole.trim();
					String pendingUserQuery = "SELECT username FROM ad_user "
							+ "JOIN ad_user_roles ON ad_user.ad_user_id = ad_user_roles.ad_user_id "
							+ "JOIN ad_role ON ad_user_roles.ad_role_id = ad_role.ad_role_id "
							+ "WHERE ad_role.name = '" + invoicePendingRole + "' " + "AND ad_user_roles.isactive = 'Y' "
							+ "AND username <> 'Openbravo' LIMIT 1";
					ResultSet pendingUserResult = s.executeQuery(pendingUserQuery);

					if (pendingUserResult.next()) {
						invpendingUser = pendingUserResult.getString("username");
					} else {
						throw new RuntimeException("No active user found for role: " + invoicePendingRole);
					}
				}

				// Update default role
				String updateDefaultRoleQuery = "UPDATE ad_user SET Default_Ad_Role_ID = "
						+ "(SELECT ad_role_id FROM ad_role WHERE name = '" + invoicePendingRole + "') "
						+ "WHERE username = '" + invpendingUser + "'";
				s.executeUpdate(updateDefaultRoleQuery);

				login(invpendingUser, "12");
				openInvoiceWindow();
				documentNoFilter(docNumber);
				submitOrApprove();

				Map<String, Object> SubmitMessageresult = submitMessageValidation(poDocNumber, "Purchase Invoice",
						"Approval", docNumber);
				submitMessageSuccessResult = (boolean) SubmitMessageresult.get("submitMessageSuccess");
				originalMessage = (String) SubmitMessageresult.get("originalMessage");

				if (submitMessageSuccessResult) {
					submitMessageSuccess = true;
				} else {
					submitMessageSuccess = false;
					break;
				}

				if (pending_Role_In_Db != null || !pending_Role_In_Db.isEmpty()) {
					logout();
				}

			} else
				break;
		}
		approvalMessageresult.put("submitMessageSuccessResult", submitMessageSuccessResult);
		approvalMessageresult.put("originalMessage", originalMessage);
		return approvalMessageresult;

	}

}
