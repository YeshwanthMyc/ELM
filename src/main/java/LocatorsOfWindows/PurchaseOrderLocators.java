package LocatorsOfWindows;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.openqa.selenium.support.ui.WebDriverWait;

import CommonUtilities.ReusableUtilities;

public class PurchaseOrderLocators extends ReusableUtilities {

	WebDriver driver;

	// Approval Details
	String poNumber = "";
	String pendingRole = "";

	// Unique code Details
	boolean isBudgetControl = false;
	boolean isUniqueCodeApplied = false;
	String isEncumbered = "N";

	public PurchaseOrderLocators(WebDriver driver, WebDriverWait wait, Actions action) throws SQLException {
		super(driver, wait, action);
		this.driver = driver;
		getConnection();
	}

	public void processType(String processType) {
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='escmPoType']")))
				.sendKeys(processType);
		action.sendKeys(Keys.ENTER).build().perform();
	}

	public void referenceNumber(String refereneceNum) throws InterruptedException {
		Thread.sleep(500);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='escmReferenceNo']")))
				.sendKeys(refereneceNum);
		action.sendKeys(Keys.TAB).build().perform();
	}

	public void contractCategory(String contractCategoryName) throws InterruptedException {
		Thread.sleep(2000);
		WebElement contractCategory = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='escmContactType'])[2]")));
		contractCategory.sendKeys(contractCategoryName);
		Thread.sleep(500);
		action.sendKeys(Keys.ENTER).build().perform();
		Thread.sleep(1000);
	}

	public void projectName(String projectDescription) {
		WebElement projectName = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//textarea[@name='escmProjectname']")));
		projectName.sendKeys(projectDescription);
	}

	public void awardNumber(String awardNumber) {
		WebElement awardNo = wait
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='escmAwardNo']")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", awardNo);
		action.moveToElement(awardNo).perform();
		awardNo.sendKeys(awardNumber);
	}

	public void awardDate(String currentDate) throws InterruptedException {
		WebElement awardDate = wait.until(
				ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='escmAwarddateG_dateTextField']")));
		awardDate.sendKeys(currentDate);
		action.moveToElement(driver.findElement(By.tagName("body"))).click().perform();
		Thread.sleep(1000);
	}

	public void letterDate(String currentDate) {
		WebElement letterDate = null;
		int letterDateAttempt = 0;
		while (letterDateAttempt < 2) {
			try {
				letterDate = wait.until(ExpectedConditions
						.elementToBeClickable(By.xpath("//input[@name='escmLetterdateG_dateTextField']")));
				wait.until(ExpectedConditions.elementToBeClickable(letterDate));
				letterDate.click();
				Thread.sleep(500);
				letterDate.sendKeys(currentDate);
				break;
			} catch (Exception e) {

				letterDateAttempt++;
			}
		}
	}

	public void selectSupplier(String supplierName) {
		int supplierAttempt = 0;
		WebElement supplier = null;
		while (supplierAttempt < 2) {
			try {
				Thread.sleep(500);
				supplier = wait.until(ExpectedConditions
						.visibilityOfElementLocated(By.xpath("(//input[@name='businessPartner'])[2]")));
				supplier.click();
				action.moveToElement(supplier).sendKeys(supplierName);
				Thread.sleep(1000);
				action.sendKeys(Keys.ENTER).build().perform();
				action.sendKeys(Keys.TAB);
				break;
			} catch (Exception e) {

				supplierAttempt++;
			}
		}
	}

	public void city(String cityName) throws InterruptedException {
		Thread.sleep(1000);
		WebElement city = null;
		int cityAttempt = 0;
		while (cityAttempt < 2) {
			try {
				city = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='escmCCity'])[2]")));
				city.sendKeys(cityName);
				city.sendKeys(Keys.ENTER);
				Thread.sleep(2000);
				break;
			} catch (Exception e) {
				cityAttempt++;
			}
		}
	}

	public void MOFDates(String currentDate) throws InterruptedException {
		// Send MOF Date
		Thread.sleep(1000);
		WebElement sendMOFDate = wait.until(
				ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='eSCMMOFDateG_dateTextField']")));

		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sendMOFDate);

		wait.until(ExpectedConditions.elementToBeClickable(sendMOFDate));
		sendMOFDate.sendKeys(currentDate);
		sendMOFDate.sendKeys(Keys.TAB, Keys.TAB);

		// Receive MOF Date
		Thread.sleep(1000);
		WebElement receiveMOFDate = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//input[@name='eSCMReceiveMOFDateG_dateTextField']")));
		receiveMOFDate.sendKeys(currentDate);
		receiveMOFDate.sendKeys(Keys.TAB, Keys.TAB);
	}

	public Map<String, Boolean> selectUniqueCode(String roleId, String accountNumber)
			throws SQLException, InterruptedException {
		Map<String, Boolean> uniqueCodeResult = new HashMap<>();
		String isBudgetControlQuery = "SELECT Property FROM AD_Preference WHERE VisibleAt_Role_ID = '" + roleId
				+ "' and Property='ESCM_BudgetControl' and value ='Y'";
		ResultSet isBudgetControlResult = s.executeQuery(isBudgetControlQuery);
		if (isBudgetControlResult.next()) {
			isBudgetControl = true;

			if (isBudgetControl == true && isUniqueCodeApplied == false) {
				// unique code selector
				isUniqueCodeApplied = true;
				int uniqueCodeSelectorRetry = 0;
				boolean accountFieldVisibile = false;
				while (uniqueCodeSelectorRetry <= 2 && !accountFieldVisibile) {
					try {
						WebElement uniqueCodeSelector = wait.until(ExpectedConditions
								.presenceOfElementLocated(By.xpath("(//img[contains(@src, 'search_picker.png')])[1]")));
						((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
								uniqueCodeSelector);
						action.moveToElement(uniqueCodeSelector).doubleClick().build().perform();

						WebElement accountField = wait.until(ExpectedConditions
								.visibilityOfElementLocated(By.xpath("//input[@type='TEXT' and @name='account']")));
						if (accountField.isDisplayed()) {
							accountFieldVisibile = true;
							break;
						}

					} catch (Exception e) {
						uniqueCodeSelectorRetry++;
					}
				}

				// Send account number in unique code selector
				Thread.sleep(500);
				WebElement accountField = wait.until(
						ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='TEXT' and @name='account']")));
				accountField.sendKeys(accountNumber);
				action.sendKeys(Keys.ENTER).build().perform();
				WebElement listOfAccountsLocator = oneMinuteWait.until(ExpectedConditions.visibilityOfElementLocated(
						By.xpath("//tr/td[5]/div/nobr[contains(text(),'" + accountNumber + "')]")));
				action.moveToElement(listOfAccountsLocator).sendKeys(Keys.ENTER).build().perform();
				Thread.sleep(1500);
				// Get list of unique codes
				List<WebElement> accounts = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
						By.xpath("//tr/td[5]/div/nobr[contains(text(),'" + accountNumber + "')]")));
				// Select 1st unique code
				accounts.get(0).click();

				// close unique code pop-up
				try {
					WebElement popUpOkButton = wait.until(
							ExpectedConditions.elementToBeClickable(By.xpath("(//td[@class='OBFormButton'])[3]")));
					popUpOkButton.click();
				} catch (Exception e) {
					//System.out.println("Popup OK button not found, proceeding to next step.");
				}

				WebElement selectUniqueCode = wait
						.until(ExpectedConditions.elementToBeClickable(By.xpath("(//td[@class='OBFormButton'])[1]")));
				selectUniqueCode.click();
			}
		}
		uniqueCodeResult.put("isBudgetControl", isBudgetControl);
		uniqueCodeResult.put("isUniqueCodeApplied", isUniqueCodeApplied);
		return uniqueCodeResult;

	}

	public void navigateToPOLinesTab() throws InterruptedException {
		Thread.sleep(1000);
		WebElement linesTab = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[contains(text(),'Lines Attributes')]")));
		action.moveToElement(linesTab).doubleClick().build().perform();
		Thread.sleep(500);
	}

	public void selectProduct(String productCode) {
		WebElement product = null;
		int productAttempt = 0;
		while (productAttempt < 2) {
			try {
				product = wait
						.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='product'])[2]")));
				product.sendKeys(productCode);
				Thread.sleep(500);
				product.sendKeys(Keys.ENTER);
				break;
			} catch (Exception e) {

				productAttempt++;
			}
		}
	}

	public void enterQuantity(String quantity) {
		WebElement lineQuantity = null;
		int lineQuantityAttempt = 0;
		while (lineQuantityAttempt < 2) {
			try {
				Thread.sleep(1000);
				lineQuantity = wait.until(ExpectedConditions
						.visibilityOfElementLocated(By.xpath("(//input[@name='orderedQuantity'])[2]")));
				lineQuantity.clear();
				action.moveToElement(lineQuantity).sendKeys(quantity).build().perform();
				action.sendKeys(Keys.TAB).build().perform();
				break;
			} catch (Exception e) {
				lineQuantityAttempt++;
			}
		}
	}

	public void enterUnitPrice(String unitPrice) {
		WebElement negotiatedUnitPrice = null;
		int negotiatedUnitPriceAttempt = 0;
		while (negotiatedUnitPriceAttempt < 2) {
			try {
				Thread.sleep(1000);
				negotiatedUnitPrice = wait
						.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='unitPrice'])[2]")));
				Thread.sleep(1000);
				negotiatedUnitPrice.clear();
				negotiatedUnitPrice.sendKeys(unitPrice);
				break;
			} catch (Exception e) {
				negotiatedUnitPriceAttempt++;

			}
		}
	}

	public double getLineNetAmount() throws SQLException {
		String lineTotalQuery = "select LineNetAmt from c_orderline\r\n" + "join c_order \r\n"
				+ "on c_orderline.c_order_id = c_order.c_order_id\r\n" + "order by c_order.created desc \r\n"
				+ "limit 1 ";
		ResultSet lineTotalResult = s.executeQuery(lineTotalQuery);
		double lineNetAmount = 0;
		if (lineTotalResult.next()) {
			String lineNetAmtResult = lineTotalResult.getString("LineNetAmt");
			//System.out.println("LineNetAmt: " + lineNetAmtResult);
			lineNetAmount = Double.parseDouble(lineNetAmtResult);
		}
		return lineNetAmount;
	}

	public void contractAttributes(String hijricurrentDate, String hijrifutureDate) throws InterruptedException {

		// Contract Attributes
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
				"//td[contains(@class, 'OBTabBarButtonChildTitle') and contains(text(), 'Contract Attributes')]")))
				.click();
		Thread.sleep(1000);

		WebElement contractStartDate = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//input[@name='escmContractstartdate_dateTextField']")));
		contractStartDate.sendKeys(hijricurrentDate);
		contractStartDate.sendKeys(Keys.TAB);
		Thread.sleep(1000);

		WebElement contractEndDate = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//input[@name='escmContractenddate_dateTextField']")));
		contractEndDate.sendKeys(hijrifutureDate);
		contractEndDate.sendKeys(Keys.TAB);
		Thread.sleep(2000);

	}

	public void navigateToPOHeader() {
		WebElement POHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
				"//td/table/tbody/tr/td[@class='OBTabBarButtonMainTitleSelectedInactive' and contains(text(),'Purchase Order')]")));
		action.moveToElement(POHeader).doubleClick().build().perform();
	}

	public String getPoNumber() throws SQLException {
		String poDetailsQuery = "select DocumentNo from c_order order by created desc limit 1";
		String poNumberResult = "";

		ResultSet poResult = s.executeQuery(poDetailsQuery);
		if (poResult.next()) {
			poNumberResult = poResult.getString("DocumentNo");

		} else {
			throw new SQLException("No PO document found in the database.");
		}
		return poNumberResult;
	}

	public Map<String, String> getPendingRole(String poNumber) throws SQLException {
		// get pending Role
		String pendingRole = "";
		String pendingRole_Id = "";
		Map<String, String> PendingRoleResult = new HashMap<>();
		String order_id = "";
		String order_idQuery = "select c_order_id from c_order where DocumentNo ='" + poNumber + "'";
		ResultSet order_idResult = s.executeQuery(order_idQuery);
		if (order_idResult.next()) {
			order_id = order_idResult.getString("c_order_id");
		}
		String pendingRolequery = "SELECT pendingapproval FROM escm_purorderacthist WHERE c_order_id = '" + order_id
				+ "' ORDER BY created DESC LIMIT 1";
		ResultSet pendingRoleResult = s.executeQuery(pendingRolequery);
		if (pendingRoleResult.next()) {
			pendingRole = pendingRoleResult.getString("pendingapproval");
			String pendingRole_IdQuery = "select ad_role_id from ad_role where name ='" + pendingRole + "'";
			ResultSet pendingRole_IdResult = s.executeQuery(pendingRole_IdQuery);
			if (pendingRole_IdResult.next()) {
				pendingRole_Id = pendingRole_IdResult.getString("ad_role_id");
				// roleId = pendingRole_Id;
			}
		}

		PendingRoleResult.put("pendingRole", pendingRole);
		PendingRoleResult.put("pendingRole_Id", pendingRole_Id);
		return PendingRoleResult;
	}

	public String getPendingUser(String pendingRole) throws SQLException {
		String pendingUser = "";
		String pendingUserQuery = "SELECT username FROM ad_user "
				+ "JOIN ad_user_roles ON ad_user.ad_user_id = ad_user_roles.ad_user_id "
				+ "JOIN ad_role ON ad_user_roles.ad_role_id = ad_role.ad_role_id " + "WHERE ad_role.name = '"
				+ pendingRole + "' " + "AND ad_user_roles.isactive = 'Y' " + "AND username <> 'Openbravo' " + "LIMIT 1";
		ResultSet pendingUserResult = s.executeQuery(pendingUserQuery);
		if (pendingUserResult.next()) {
			pendingUser = pendingUserResult.getString("username");

		}

		// Update default role as pending role for pending user
		String updateDefaultRoleQuery = "UPDATE ad_user SET Default_Ad_Role_ID = "
				+ "(SELECT ad_role_id FROM ad_role WHERE name = '" + pendingRole + "') " + "WHERE username = '"
				+ pendingUser + "'";
		s.executeUpdate(updateDefaultRoleQuery);
		return pendingUser;
	}

	public void documentNoFilter(String poNumber) throws InterruptedException {
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='documentNo']"))).sendKeys(poNumber);
		Thread.sleep(1000);
		WebElement filteredRow = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("(//td/div/nobr[contains(text(),'" + poNumber + "')])[1]")));

		action.moveToElement(filteredRow).doubleClick().build().perform();
	}

	public String isEncumbered(String poNumber) throws SQLException {
		String isEncumbered = "N";
		String isEncumberedQuery = "select em_efin_encumbered from c_order where DocumentNo = '" + poNumber + "'";
		ResultSet isEncumberedQueryResult = s.executeQuery(isEncumberedQuery);
		if (isEncumberedQueryResult.next()) {
			isEncumbered = isEncumberedQueryResult.getString("em_efin_encumbered");

		}
		return isEncumbered;
	}

	public String applyUniqueCode(String isEncumbered) throws SQLException, InterruptedException {
		saveHeader();

		if (isEncumbered.equals("N")) {
			Thread.sleep(3000);
			WebElement applyUniqueCodetoLines = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("//td[contains(@class,'OBToolbarIconButton_icon_escm_po_apply_uniquecode')]")));
			applyUniqueCodetoLines.click();

			WebElement applyUniqueCodePopUpOkButton = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//td[@class='OBFormButton' and contains(text(),'Yes')]")));
			applyUniqueCodePopUpOkButton.click();
		}
		return isEncumbered;
	}

	public void POApproval(String windowName, String accountNumber) throws InterruptedException, SQLException {
		while (true) {
			poNumber = getPoNumber();
			Map<String, String> PendingRoleResult = getPendingRole(poNumber);

			pendingRole = PendingRoleResult.get("pendingRole");
			String pendingRoleId = PendingRoleResult.get("pendingRole_Id");

			if (pendingRole.isEmpty() || pendingRole.equals(null)) {
				break;
			}

			String userName = getPendingUser(pendingRole);
			login(userName, "12");

			openWindow(windowName);
			documentNoFilter(poNumber);

			Map<String, Boolean> UniqueCodeResult1 = selectUniqueCode(pendingRoleId, accountNumber);
			isBudgetControl = UniqueCodeResult1.get("isBudgetControl");
			isUniqueCodeApplied = UniqueCodeResult1.get("isUniqueCodeApplied");

			isEncumbered = isEncumbered(poNumber);
			applyUniqueCode(isEncumbered);

			submitOrApprove();
			submitMessage(poNumber,windowName);

			logout();
			Thread.sleep(2000);
		}
	}
	
	public void addCostCenter(String windowName) throws InterruptedException, SQLException {
		openWindow(windowName);
		documentNoFilter(poNumber);
		Thread.sleep(2500);
		
		String costCenterQuery = "select Value from C_SalesRegion where EM_Efin_Showinpo='Y'";	
		ResultSet costCenterQueryResult = s.executeQuery(costCenterQuery);
		List<String> costCenters = new ArrayList<>();
		while(costCenterQueryResult.next()) {
			costCenters.add(costCenterQueryResult.getString("Value"));
		}
		
		By costCenterLocator = By.xpath("(//input[@name='efinSalesregion'])[2]");
		wait.until(ExpectedConditions.presenceOfElementLocated(costCenterLocator));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
				driver.findElement(costCenterLocator));
		((JavascriptExecutor) driver).executeScript("arguments[0].click();",
				driver.findElement(costCenterLocator));
		WebElement costCenter = wait.until(ExpectedConditions.elementToBeClickable(costCenterLocator));
		
		
		String costCenterEmptyQuery="select EM_Efin_Salesregion_ID from c_order where documentno='"+poNumber+"'";
		ResultSet costCenterEmptyQueryResult = s.executeQuery(costCenterEmptyQuery);
		if(costCenterEmptyQueryResult.next()) {
			String costCenterEmpty = costCenterEmptyQueryResult.getString("EM_Efin_Salesregion_ID");
			if(costCenterEmpty==null) {
				costCenter.sendKeys(costCenters.get(0));
				Thread.sleep(1500);
				action.sendKeys(Keys.ENTER).build().perform();
			}	
		}
	}
	
	public void addLedgerAccount(String product,String accountNumber) throws InterruptedException, SQLException {
		
		navigateToPOLinesTab();
		WebElement filteredLineRow = wait.until(
				ExpectedConditions.elementToBeClickable(By.xpath("//div/nobr[contains(text(),'" + product + "')]")));
		action.moveToElement(filteredLineRow).doubleClick().build().perform();
		
		String ledgerAccountEmptyQuery ="select EM_Efin_Ledgeraccount_ID from c_orderline \r\n"
				+ "join c_order on \r\n"
				+ "c_order.c_order_id = c_orderline.c_order_id\r\n"
				+ "where c_order.documentno ='"+poNumber+"'";
		
		String ledgerAccountsQuery ="select CEV2.value as ledger_Value\r\n"
				+ "FROM efin_accountmap EAM\r\n"
				+ "JOIN C_ElementValue CEV1 ON EAM.C_ElementValue_ID = CEV1.C_ElementValue_ID\r\n"
				+ "JOIN C_ElementValue CEV2 ON EAM.GL_Account_ID = CEV2.C_ElementValue_ID\r\n"
				+ "WHERE CEV1.value = '"+accountNumber+"' and CEV2.value in(select C_ElementValue.value from efin_product_acct \r\n"
				+ "join m_product\r\n"
				+ "on efin_product_acct.m_product_id = m_product.m_product_id\r\n"
				+ "join C_ElementValue\r\n"
				+ "on efin_product_acct.P_Expense_Acct = C_ElementValue.C_ElementValue_id\r\n"
				+ "where m_product.Value ='"+product+"' and efin_product_acct.isactive='Y'\r\n"
				+ ")";
		ResultSet ledgerAccountsQueryResult = s.executeQuery(ledgerAccountsQuery);
		List<String> ledgerAccounts = new ArrayList<>();

		while(ledgerAccountsQueryResult.next()) {
			ledgerAccounts.add(ledgerAccountsQueryResult.getString("ledger_Value"));
		}
		
		By ledgerLocator = By.xpath("(//input[@name='efinLedgeraccount'])[2]");
		wait.until(ExpectedConditions.presenceOfElementLocated(ledgerLocator));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
				driver.findElement(ledgerLocator));
		Thread.sleep(1500);
		WebElement ledgerAccount = wait.until(ExpectedConditions.elementToBeClickable(ledgerLocator));
		ledgerAccount.clear();
		WebElement reLoadedLedgerAccount = wait.until(ExpectedConditions.elementToBeClickable(ledgerLocator));
		reLoadedLedgerAccount.click();
		Thread.sleep(500);
		
		ResultSet ledgerAccountEmptyQueryResult = s.executeQuery(ledgerAccountEmptyQuery);
		
		if(ledgerAccountEmptyQueryResult.next()) {
			String ledgerAccountEmpty = ledgerAccountEmptyQueryResult.getString("EM_Efin_Ledgeraccount_ID");
			if(ledgerAccountEmpty==null) {
				reLoadedLedgerAccount.sendKeys(ledgerAccounts.get(0));
			}	
		}
		Thread.sleep(1500);
		action.sendKeys(Keys.ENTER).build().perform();
		
		saveLine();
		
	}

}
