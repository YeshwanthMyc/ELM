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

public class InvoiceLocators extends ReusableUtilities {
	WebDriver driver;

	public InvoiceLocators(WebDriver driver, WebDriverWait wait, Actions action) throws SQLException {
		super(driver, wait, action);
		this.driver = driver;
		getConnection();
	}

	public String getDocNumber(String TxrnId) throws SQLException {
		String DocNumber = null;
		String documentNoQuery = "select DocumentNo from C_Invoice where C_Invoice_id in \r\n"
				+ "(select C_Invoice_ID from Efin_RDVTxn where Efin_RDVTxn_id ='" + TxrnId + "')";
		ResultSet DocNumQueryResult = s.executeQuery(documentNoQuery);
		while (DocNumQueryResult.next()) {
			DocNumber = DocNumQueryResult.getString("DocumentNo");
			System.out.println(DocNumber);
			break;
		}
		return DocNumber;
	}

	public void openWindow(String windowName) throws InterruptedException {
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]")))
				.click();
		WebElement quickLaunchTextBox = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
		quickLaunchTextBox.sendKeys(windowName);
		Thread.sleep(500);
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@class='listTable']//td/div/nobr[text()='"+windowName+"']")))
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

		WebElement mofReqNum = oneMinuteWait
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
				action.sendKeys(Keys.TAB).build().perform();
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}

		for (int i = 0; i < 3; i++) {
			try {
				Thread.sleep(2000);
				By dateFieldLocator = By.xpath("//input[@name='efinInwarddateGreg_dateTextField']");
				WebElement inwardDate = wait
						.until(ExpectedConditions.refreshed(ExpectedConditions.elementToBeClickable(dateFieldLocator)));
				inwardDate.click();
				inwardDate.sendKeys(currentDate);
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

		Thread.sleep(2000);
		WebElement supplierInvoiceDate = wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//input[@name='efinSupinvdateG_dateTextField']")));
		supplierInvoiceDate.sendKeys(currentDate);

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

	private double getAmountFromQuery(String query) throws SQLException {
		double amount = 0;
		ResultSet rs = s.executeQuery(query);
		if (rs.next()) {
			String value = rs.getString(1);
			amount = Double.parseDouble(value);
			System.out.println(amount);

		}
		return amount;
	}
	
	private static double roundTwoDecimals(double value) {
	    return Math.round(value * 100.0) / 100.0;
	}

	public boolean amountValidations(String TxrnId, String tempDocNumber, String Deduction, String PenaltyName,
			String revenueAccount, String ExternalPenaltyName, String externalBusinessPartnerName,boolean isTaxPO)
			throws NumberFormatException, SQLException {

		boolean amountValidations = false;

		double netMatchAmount = getAmountFromQuery(
				"select Netmatch_Amt from Efin_RDVTxn where Efin_RDVTxn_id ='" + TxrnId + "'");

		double mainLineAmt = getAmountFromQuery("select LineNetAmt from C_InvoiceLine where C_Invoice_id in "
				+ "(select C_Invoice_id from C_Invoice where DocumentNo='" + tempDocNumber + "') and EM_Efin_Istax='N' "
				+ "and EM_Efin_C_Elementvalue_ID in (select C_ElementValue_ID from C_ElementValue where value!='"
				+ revenueAccount + "') "
				+ "and coalesce(C_Bpartner_ID,'') not in (select C_BPartner_id from C_BPartner where Name ='"
				+ externalBusinessPartnerName + "')");

		double taxLine = getAmountFromQuery("select LineNetAmt from C_InvoiceLine where C_Invoice_id in "
				+ "(select C_Invoice_id from C_Invoice where DocumentNo='" + tempDocNumber + "') and EM_Efin_Istax='Y' "
				+ "and coalesce(EM_Efin_Beneficiary2_ID,'') not in (select C_BPartner_id from C_BPartner where Name ='"
				+ externalBusinessPartnerName + "')");

		double penaltyAmt = getAmountFromQuery("select Sum from efin_penalty_detail where Efin_Rdvtxn_ID='" + TxrnId
				+ "'\r\n" + "and Deductiontype in(select EUT_Deflookups_TypeLn.Value from EUT_Deflookups_TypeLn \r\n"
				+ "join EUT_Deflookups_Type on EUT_Deflookups_TypeLn.EUT_Deflookups_Type_ID = EUT_Deflookups_Type.EUT_Deflookups_Type_ID\r\n"
				+ "where EUT_Deflookups_Type.Value='PENALTY_TYPE' and EUT_Deflookups_TypeLn.Arabicname ='" + PenaltyName
				+ "')");

		double penaltyLineAmt = getAmountFromQuery("select LineNetAmt from C_InvoiceLine where C_Invoice_id in "
				+ "(select C_Invoice_id from C_Invoice where DocumentNo='" + tempDocNumber + "') and EM_Efin_Istax='N' "
				+ "and EM_Efin_C_Elementvalue_ID in (select C_ElementValue_ID from C_ElementValue where value='"
				+ revenueAccount + "')");

		double externalPenaltyAmt = getAmountFromQuery("select Sum from efin_penalty_detail where Efin_Rdvtxn_ID='"
				+ TxrnId + "'\r\n"
				+ "and Deductiontype in(select EUT_Deflookups_TypeLn.Value from EUT_Deflookups_TypeLn \r\n"
				+ "join EUT_Deflookups_Type on EUT_Deflookups_TypeLn.EUT_Deflookups_Type_ID = EUT_Deflookups_Type.EUT_Deflookups_Type_ID\r\n"
				+ "where EUT_Deflookups_Type.Value='PENALTY_TYPE' and EUT_Deflookups_TypeLn.Arabicname ='"
				+ ExternalPenaltyName + "')");

		double lineExternalPenaltyAmt = getAmountFromQuery("select LineNetAmt from C_InvoiceLine where C_Invoice_id in "
				+ "(select C_Invoice_id from C_Invoice where DocumentNo='" + tempDocNumber + "') and EM_Efin_Istax='N' "
				+ "and EM_Efin_C_Elementvalue_ID in (select C_ElementValue_ID from C_ElementValue where value!='"
				+ revenueAccount + "') "
				+ "and coalesce(C_Bpartner_ID,'') in (select C_BPartner_id from C_BPartner where Name ='"
				+ externalBusinessPartnerName + "')");

		double extPenaltytaxLine = getAmountFromQuery("select LineNetAmt from C_InvoiceLine where C_Invoice_id in "
				+ "(select C_Invoice_id from C_Invoice where DocumentNo='" + tempDocNumber + "') and EM_Efin_Istax='Y' "
				+ "and coalesce(EM_Efin_Beneficiary2_ID,'') in (select C_BPartner_id from C_BPartner where Name ='"
				+ externalBusinessPartnerName + "')");

		if (Deduction.equalsIgnoreCase("None") || Deduction.equalsIgnoreCase("Hold")) {
			if(isTaxPO=false) {
				if (netMatchAmount > 0 && mainLineAmt > 0 && taxLine > 0) {
					if (netMatchAmount == mainLineAmt && taxLine == mainLineAmt * 0.15) {
						amountValidations = true;
					}
				}
			}
			
			if(isTaxPO=true) {
				if (netMatchAmount > 0 && mainLineAmt > 0 && taxLine > 0) {
					if(mainLineAmt==roundTwoDecimals(netMatchAmount/1.15) && taxLine==roundTwoDecimals(mainLineAmt * 0.15)) {
						amountValidations = true;
					}
				}
			}
			

		} else if (Deduction.equalsIgnoreCase("Penalty")) {
			if(isTaxPO=false) {
				if (mainLineAmt > 0 && netMatchAmount > 0 && penaltyAmt > 0 && taxLine > 0) {
					if (mainLineAmt == netMatchAmount + penaltyAmt && -penaltyLineAmt == penaltyAmt
							&& taxLine == netMatchAmount * 0.15) {
						amountValidations = true;
					}
				}
			}
			if(isTaxPO=true) {
				if (mainLineAmt > 0 && netMatchAmount > 0 && penaltyAmt > 0 && taxLine > 0) {
					if((mainLineAmt==roundTwoDecimals((netMatchAmount/1.15)+penaltyAmt)) && -penaltyLineAmt == penaltyAmt
							&& taxLine==roundTwoDecimals((netMatchAmount/1.15)*0.15)) {
						amountValidations = true;
					}
				}
			}
			

		} else if (Deduction.equalsIgnoreCase("External Penalty")) {
			if(isTaxPO=false) {
				if (mainLineAmt > 0 && netMatchAmount > 0 && externalPenaltyAmt > 0 && lineExternalPenaltyAmt > 0
						&& taxLine > 0 && extPenaltytaxLine > 0) {
					if (mainLineAmt == netMatchAmount && externalPenaltyAmt == lineExternalPenaltyAmt
							&& taxLine == mainLineAmt * 0.15 && extPenaltytaxLine == lineExternalPenaltyAmt * 0.15) {
						amountValidations = true;
					}
				}
			}
			if(isTaxPO=true) {
				if (mainLineAmt > 0 && netMatchAmount > 0 && externalPenaltyAmt > 0 && lineExternalPenaltyAmt > 0
						&& taxLine > 0 && extPenaltytaxLine > 0) {
					System.out.println(roundTwoDecimals(netMatchAmount/1.15));
					System.out.println(externalPenaltyAmt/1.15);
					System.out.println(mainLineAmt*0.15);
					System.out.println(lineExternalPenaltyAmt * 0.15);
					
					if(mainLineAmt== roundTwoDecimals(netMatchAmount/1.15) && lineExternalPenaltyAmt == roundTwoDecimals(externalPenaltyAmt/1.15) &&
							taxLine == roundTwoDecimals(mainLineAmt*0.15) && extPenaltytaxLine == roundTwoDecimals(lineExternalPenaltyAmt * 0.15)) {
						amountValidations = true;
					}
				}
			}
			

		} else if (Deduction.equalsIgnoreCase("All Deductions")) {
			if(isTaxPO=false) {
				if (mainLineAmt > 0 && lineExternalPenaltyAmt > 0 && taxLine > 0 && extPenaltytaxLine > 0
						&& penaltyAmt > 0) {
					if (mainLineAmt == netMatchAmount + penaltyAmt && externalPenaltyAmt == lineExternalPenaltyAmt
							&& taxLine == netMatchAmount * 0.15 && extPenaltytaxLine == lineExternalPenaltyAmt * 0.15
							&& -penaltyLineAmt == penaltyAmt) {
						amountValidations = true;
					}
				}
			}
			if(isTaxPO=true) {
				if(mainLineAmt > 0 && lineExternalPenaltyAmt > 0 && taxLine > 0 && extPenaltytaxLine > 0
						&& penaltyAmt > 0) {
					if(mainLineAmt==roundTwoDecimals((netMatchAmount/1.15)+penaltyAmt) && lineExternalPenaltyAmt==roundTwoDecimals(externalPenaltyAmt/1.15)
							&& taxLine == roundTwoDecimals((netMatchAmount/1.15)*0.15) && extPenaltytaxLine==lineExternalPenaltyAmt*0.15 && -penaltyLineAmt == penaltyAmt) {
						amountValidations = true;
						
					}
					
				}
			}
			
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
					login("Openbravo", "12");
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
				openWindow("Purchase Invoice");
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
