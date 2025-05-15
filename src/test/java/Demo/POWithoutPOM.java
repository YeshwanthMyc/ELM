package Demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class POWithoutPOM {
	public static String DB_URL = "jdbc:postgresql://localhost:5932/mainaccrual";
	public static String DB_USER = "tad";
	public static String DB_PASSWORD = "tad";

	public static void main(String[] args) throws InterruptedException, SQLException {
		boolean isSuccess = false;
		Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		Statement s = con.createStatement();
		// Gregorian Date
		LocalDate today = LocalDate.now();
		LocalDate futuredate = today.plusDays(5);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
		String currentDate = today.format(formatter);
		String futureDate = futuredate.format(formatter);

		// Hijri Date
		LocalDate HijriTodayBase = LocalDate.now();
		LocalDate HijriFutureDateBase = HijriTodayBase.plusDays(5);
		HijrahDate hijriToday = HijrahChronology.INSTANCE.date(HijriTodayBase);
		HijrahDate hijriFutureDate = HijrahChronology.INSTANCE.date(HijriFutureDateBase);
		DateTimeFormatter hijriFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");

		String hijricurrentDate = hijriToday.format(hijriFormatter);
		String hijrifutureDate = hijriFutureDate.format(hijriFormatter);

		// Driver Details
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		Actions action = new Actions(driver);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(7));
		WebDriverWait oneMinuteWait = new WebDriverWait(driver, Duration.ofSeconds(60));
		Wait<WebDriver> fluentWait = new FluentWait<>(driver).withTimeout(Duration.ofSeconds(60))
				.pollingEvery(Duration.ofSeconds(5)).ignoring(NoSuchElementException.class);

		// Launch Application
		driver.get("http://qualiantracker.dyndns.org:9090/grpmain/security/Login");
		String userName = "Openbravo";
		String roleId = "D276B8C8F5984648A4A144CA7D07A3C2";

		boolean isBudgetControl = false;
		boolean isUniqueCodeApplied = false;
		String poNumber="";

		try {
			// Login
			driver.findElement(By.xpath("//input[@id='user']")).sendKeys(userName);
			driver.findElement(By.xpath("//input[@id='password']")).sendKeys("12");
			driver.findElement(By.xpath("//button[@id='buttonOK']")).click();

			// Quick Launch
			wait.until(
					ExpectedConditions.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]")))
					.click();

			// select window from quick launch
			WebElement quickLaunchTextBox = wait.until(ExpectedConditions
					.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
			quickLaunchTextBox.sendKeys("Purchase Order and Contracts summary");
			Thread.sleep(500);
			action.sendKeys(Keys.ENTER).build().perform();

			// Create new PO
			wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.xpath("//td[contains(@class,'OBToolbarIconButton_icon_newDoc')]")))
					.click();

			// Maximize header
			WebElement poHeader = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
					"(//div/div/table/tbody/tr/td[@class='OBTabBarButtonMainTitleSelected']//table/tbody/tr/td)[1]")));
			action.doubleClick(poHeader).build().perform();

			// Process type
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='escmPoType']")))
					.sendKeys("Direct Purchasing");
			action.sendKeys(Keys.ENTER).build().perform();

			// Reference Number
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='escmReferenceNo']")))
					.sendKeys("12345");
			action.sendKeys(Keys.TAB).build().perform();

			// Contract Category
			Thread.sleep(1000);
			WebElement contractCategory = wait
					.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='escmContactType'])[2]")));
			contractCategory.sendKeys("صيانة طرق روتينية -مبلغ");
			action.sendKeys(Keys.ENTER).build().perform();
			Thread.sleep(1000);

			// Project Name
			WebElement projectName = wait.until(
					ExpectedConditions.visibilityOfElementLocated(By.xpath("//textarea[@name='escmProjectname']")));
			projectName.sendKeys("test");

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
							((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", uniqueCodeSelector); 
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
					WebElement accountField = wait.until(ExpectedConditions
							.elementToBeClickable(By.xpath("//input[@type='TEXT' and @name='account']")));
					accountField.sendKeys("400200300");
					action.sendKeys(Keys.ENTER).build().perform();
					WebElement listOfAccountsLocator = oneMinuteWait.until(ExpectedConditions
							.visibilityOfElementLocated(By.xpath("//tr/td[5]/div/nobr[contains(text(),'400200300')]")));
					action.moveToElement(listOfAccountsLocator).sendKeys(Keys.ENTER).build().perform();
					Thread.sleep(1500);
					// Get list of unique codes
					List<WebElement> accounts = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
							By.xpath("//tr/td[5]/div/nobr[contains(text(),'400200300')]")));
					// Select 1st unique code
					accounts.get(0).click();

					// close unique code pop-up
					try {
						WebElement popUpOkButton = wait.until(
								ExpectedConditions.elementToBeClickable(By.xpath("(//td[@class='OBFormButton'])[3]")));
						popUpOkButton.click();
					} catch (Exception e) {
						System.out.println("Popup OK button not found, proceeding to next step.");
					}

					WebElement selectUniqueCode = wait.until(
							ExpectedConditions.elementToBeClickable(By.xpath("(//td[@class='OBFormButton'])[1]")));
					selectUniqueCode.click();
				}
			}

			

			// AwardNumber
			WebElement awardNo = wait
					.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='escmAwardNo']")));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", awardNo); 
			action.moveToElement(awardNo).perform();
			awardNo.sendKeys("123");

			// AwardDate
			WebElement awardDate = wait.until(
					ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='escmAwarddateG_dateTextField']")));
			awardDate.sendKeys(currentDate);
			action.moveToElement(driver.findElement(By.tagName("body"))).click().perform();
			Thread.sleep(1000);

			// LetterDate
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

			// Supplier
			int supplierAttempt = 0;
			WebElement supplier = null;
			while (supplierAttempt < 2) {
				try {
					supplier = wait.until(ExpectedConditions
							.visibilityOfElementLocated(By.xpath("(//input[@name='businessPartner'])[2]")));
					supplier.click();
					action.moveToElement(supplier).sendKeys("مكتب عبدالرحمن عبدالله النور لإستشارات هندسة");
					Thread.sleep(1000);
					action.sendKeys(Keys.ENTER).build().perform();
					action.sendKeys(Keys.TAB);
					break;
				} catch (Exception e) {

					supplierAttempt++;
				}
			}

			// City
			Thread.sleep(1000);
			WebElement city = null;
			int cityAttempt = 0;
			while (cityAttempt < 2) {
				try {
					city = wait.until(
							ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='escmCCity'])[2]")));
					city.sendKeys("الباحة");
					city.sendKeys(Keys.ENTER);
					Thread.sleep(2000);
					break;
				} catch (Exception e) {
					cityAttempt++;
				}
			}

			// MOF Dates
			Thread.sleep(2000);
			WebElement sendMOFDate = wait.until(ExpectedConditions
					.presenceOfElementLocated(By.xpath("//input[@name='eSCMMOFDateG_dateTextField']")));

			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sendMOFDate);

			wait.until(ExpectedConditions.elementToBeClickable(sendMOFDate));
			sendMOFDate.sendKeys(currentDate);
			sendMOFDate.sendKeys(Keys.TAB, Keys.TAB);

			Thread.sleep(1000);
			WebElement receiveMOFDate = wait.until(ExpectedConditions
					.elementToBeClickable(By.xpath("//input[@name='eSCMReceiveMOFDateG_dateTextField']")));
			receiveMOFDate.sendKeys(currentDate);
			receiveMOFDate.sendKeys(Keys.TAB, Keys.TAB);

			// Header Save
			Thread.sleep(1000);
			WebElement headerSaveIcon = null;
			int headerSaveIconAttempt = 0;
			while (headerSaveIconAttempt < 2) {
				try {
					headerSaveIcon = wait.until(ExpectedConditions.elementToBeClickable(
							By.xpath("//td[contains(@class,'OBToolbarIconButton_icon_save OBToolbarIconButton')]")));
					headerSaveIcon.click();
					break;
				} catch (Exception e) {
					headerSaveIconAttempt++;
				}
			}

			// Attachment
			Thread.sleep(1000);
			WebElement attachmentIcon = wait.until(ExpectedConditions
					.elementToBeClickable(By.xpath("//td[contains(@class,'OBToolbarIconButton_icon_attach')]")));
			action.moveToElement(attachmentIcon).click().build().perform();

			WebElement attachmentFile = wait
					.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='inpname']")));
			attachmentFile.sendKeys("C:\\Users\\QUALIAN\\OneDrive - Qualian Technologies Pvt Ltd\\Documents\\yesh.txt");

			Thread.sleep(1000);
			WebElement attachmentSubmit = wait
					.until(ExpectedConditions.elementToBeClickable(By.xpath("(//td[contains(text(),'Submit')])[2]")));
			attachmentSubmit.click();

			if (attachmentSubmit.isEnabled()) {
				action.moveToElement(attachmentSubmit).click().build().perform();
			}

			// Lines tab
			WebElement linesTab = wait.until(
					ExpectedConditions.elementToBeClickable(By.xpath("//td[contains(text(),'Lines Attributes')]")));
			action.moveToElement(linesTab).doubleClick().build().perform();

			// create new Line
			Thread.sleep(1000);
			wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("(//td[contains(@class,'OBToolbarIconButton_icon_newDoc')])[2]"))).click();

			// Select product
			WebElement product = null;
			int productAttempt = 0;
			while (productAttempt < 2) {
				try {
					product = wait
							.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='product'])[2]")));
					product.sendKeys("01010001");
					Thread.sleep(500);
					product.sendKeys(Keys.ENTER);
					break;
				} catch (Exception e) {

					productAttempt++;
				}
			}

			// Enter Quantity
			WebElement lineQuantity = null;
			int lineQuantityAttempt = 0;
			while (lineQuantityAttempt < 2) {
				try {
					Thread.sleep(1000);
					lineQuantity = wait.until(ExpectedConditions
							.visibilityOfElementLocated(By.xpath("(//input[@name='orderedQuantity'])[2]")));
					lineQuantity.clear();
					action.moveToElement(lineQuantity).sendKeys("1").build().perform();
					action.sendKeys(Keys.TAB).build().perform();
					break;
				} catch (Exception e) {
					lineQuantityAttempt++;
				}
			}

			// Enter Price
			WebElement negotiatedUnitPrice = null;
			int negotiatedUnitPriceAttempt = 0;
			while (negotiatedUnitPriceAttempt < 2) {
				try {
					Thread.sleep(1000);
					negotiatedUnitPrice = wait.until(
							ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='unitPrice'])[2]")));
					Thread.sleep(1000);
					negotiatedUnitPrice.clear();
					negotiatedUnitPrice.sendKeys("5000");
					break;
				} catch (Exception e) {
					negotiatedUnitPriceAttempt++;

				}
			}

			// line save
			Thread.sleep(1000);
			WebElement lineSaveIcon = null;
			int lineSaveIconAttempt = 0;
			while (lineSaveIconAttempt < 2) {
				try {
					lineSaveIcon = wait.until(ExpectedConditions.elementToBeClickable(
							By.xpath("(//td[contains(@class,'OBToolbarIconButton_icon_save')])[3]")));
					lineSaveIcon.click();
					Thread.sleep(2000);
					break;
				} catch (Exception e) {
					lineSaveIconAttempt++;
				}
			}

			
			String lineTotalQuery = "select LineNetAmt from c_orderline\r\n"
					+ "join c_order \r\n"
					+ "on c_orderline.c_order_id = c_order.c_order_id\r\n"
					+ "order by c_order.created desc \r\n"
					+ "limit 1 ";
			ResultSet lineTotalResult = s.executeQuery(lineTotalQuery);
			
			if(lineTotalResult.next()) {
				String lineNetAmtResult = lineTotalResult.getString("LineNetAmt");
			    System.out.println("LineNetAmt: " + lineNetAmtResult);
			    double lineNetAmount = Double.parseDouble(lineNetAmtResult);
			    if(lineNetAmount>=10000) {
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
			}
			

			// Navigate to header
			WebElement POHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
					"//td/table/tbody/tr/td[@class='OBTabBarButtonMainTitleSelectedInactive' and contains(text(),'Purchase Order')]")));
			action.moveToElement(POHeader).doubleClick().build().perform();

			// Submit PO
			Thread.sleep(2000);
			WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
					By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Submit')]")));
			action.moveToElement(submitButton).click().build().perform();
			Thread.sleep(3000);
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainframe")));
			WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonOK")));
			okButton.click();

			// Success message
			driver.switchTo().defaultContent();
			fluentWait.until(
					ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@id='Processing_Container']")));
			Thread.sleep(2000);
			WebElement successMessageLocator = null;
			int successMessageLocatorAttempt = 0;
			while (successMessageLocatorAttempt < 2) {
				try {
					successMessageLocator = wait.until(ExpectedConditions.presenceOfElementLocated(
							By.xpath("//div[contains(@class, 'OBMessageBarDescriptionText_success')]")));
					String actualMessage = successMessageLocator.getText();
					String expectedMessage = "Process completed successfully.";
					if (actualMessage.contains(expectedMessage)) {
						isSuccess = true;
						
					} else {
						System.out.println("Success Message Not Matched");
					}
					break;
				} catch (Exception e) {
					successMessageLocatorAttempt++;
				}
			}

			// logout
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//img[@name='isc_10main']")));
			driver.findElement(By.xpath("//img[@name='isc_10main']")).click();
			Thread.sleep(2000); // Retained for dynamic loading
			action.sendKeys(Keys.ENTER).build().perform();

			while (true) {
				String poDetailsQuery = "select DocumentNo,c_order_id from c_order order by created desc limit 1";
				poNumber = "";
				String order_id = "";
				ResultSet poResult = s.executeQuery(poDetailsQuery);
				if (poResult.next()) {
					poNumber = poResult.getString("DocumentNo");
					order_id = poResult.getString("c_order_id");

				} else {
					throw new SQLException("No PO document found in the database.");
				}

				// get pending Role
				String pendingRole = "";
				String pendingUser = "";
				String pendingRole_Id ="";
				String pendingRolequery = "SELECT pendingapproval FROM escm_purorderacthist WHERE c_order_id = '"
						+ order_id + "' ORDER BY created DESC LIMIT 1";
				ResultSet pendingRoleResult = s.executeQuery(pendingRolequery);
				if (pendingRoleResult.next()) {
					pendingRole = pendingRoleResult.getString("pendingapproval");
					String pendingRole_IdQuery = "select ad_role_id from ad_role where name ='"+pendingRole+"'";
					ResultSet pendingRole_IdResult = s.executeQuery(pendingRole_IdQuery);
					if(pendingRole_IdResult.next()) {
						pendingRole_Id=pendingRole_IdResult.getString("ad_role_id");
						roleId  = pendingRole_Id;
					}
				}

				if (pendingRole.isEmpty()) {
					break;
				}
				// get pending user
				String pendingUserQuery = "SELECT username FROM ad_user "
						+ "JOIN ad_user_roles ON ad_user.ad_user_id = ad_user_roles.ad_user_id "
						+ "JOIN ad_role ON ad_user_roles.ad_role_id = ad_role.ad_role_id " + "WHERE ad_role.name = '"
						+ pendingRole + "' " + "AND ad_user_roles.isactive = 'Y' " + "AND username <> 'Openbravo' "
						+ "LIMIT 1";
				ResultSet pendingUserResult = s.executeQuery(pendingUserQuery);
				if (pendingUserResult.next()) {
					pendingUser = pendingUserResult.getString("username");

				}

				// Update default role as pending role for pending user
				String updateDefaultRoleQuery = "UPDATE ad_user SET Default_Ad_Role_ID = "
						+ "(SELECT ad_role_id FROM ad_role WHERE name = '" + pendingRole + "') " + "WHERE username = '"
						+ pendingUser + "'";
				int rows = s.executeUpdate(updateDefaultRoleQuery);

				// Re - Login
				Thread.sleep(3000);
				driver.findElement(By.xpath("//input[@id='user']")).sendKeys(pendingUser);
				driver.findElement(By.xpath("//input[@id='password']")).sendKeys("12");
				driver.findElement(By.xpath("//button[@id='buttonOK']")).click();

				wait.until(ExpectedConditions
						.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]"))).click();

				// select window from quick launch
				WebElement quickLaunchTextBox1 = wait.until(ExpectedConditions
						.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
				quickLaunchTextBox1.sendKeys("Purchase Order and Contracts summary");
				Thread.sleep(500);
				action.sendKeys(Keys.ENTER).build().perform();

				WebElement poHeader1 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
						"(//div/div/table/tbody/tr/td[@class='OBTabBarButtonMainTitleSelected']//table/tbody/tr/td)[1]")));
				action.doubleClick(poHeader1).build().perform();

				wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='documentNo']")))
						.sendKeys(poNumber);
				Thread.sleep(1000);
				WebElement filteredRow = wait.until(ExpectedConditions
						.elementToBeClickable(By.xpath("(//td/div/nobr[contains(text(),'" + poNumber + "')])[1]")));

				action.moveToElement(filteredRow).doubleClick().build().perform();
				
				String isBudgetControlQuery1 = "SELECT Property FROM AD_Preference WHERE VisibleAt_Role_ID = '" + roleId
						+ "' and Property='ESCM_BudgetControl' and value ='Y'";
				ResultSet isBudgetControlResult1 = s.executeQuery(isBudgetControlQuery1);
				if (isBudgetControlResult1.next()) {
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
								((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", uniqueCodeSelector); 
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
						WebElement accountField = wait.until(ExpectedConditions
								.elementToBeClickable(By.xpath("//input[@type='TEXT' and @name='account']")));
						accountField.sendKeys("400200300");
						action.sendKeys(Keys.ENTER).build().perform();
						WebElement listOfAccountsLocator = oneMinuteWait.until(ExpectedConditions
								.visibilityOfElementLocated(By.xpath("//tr/td[5]/div/nobr[contains(text(),'400200300')]")));
						action.moveToElement(listOfAccountsLocator).sendKeys(Keys.ENTER).build().perform();
						Thread.sleep(1500);
						// Get list of unique codes
						List<WebElement> accounts = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
								By.xpath("//tr/td[5]/div/nobr[contains(text(),'400200300')]")));
						// Select 1st unique code
						accounts.get(0).click();

						// accounts.get(0).click();

						// close unique code pop-up
						try {
							WebElement popUpOkButton = wait.until(
									ExpectedConditions.elementToBeClickable(By.xpath("(//td[@class='OBFormButton'])[3]")));
							popUpOkButton.click();
						} catch (Exception e) {
							System.out.println("Popup OK button not found, proceeding to next step.");
						}

						WebElement selectUniqueCode = wait.until(
								ExpectedConditions.elementToBeClickable(By.xpath("(//td[@class='OBFormButton'])[1]")));
						selectUniqueCode.click();
					}
				}
				// Header Save
				Thread.sleep(1000);
				WebElement headerSaveIcon1 = null;
				int headerSaveIconAttempt1 = 0;
				while (headerSaveIconAttempt < 2) {
					try {
						headerSaveIcon1 = wait.until(ExpectedConditions.elementToBeClickable(
								By.xpath("//td[contains(@class,'OBToolbarIconButton_icon_save OBToolbarIconButton')]")));
						headerSaveIcon1.click();
						break;
					} catch (Exception e) {
						headerSaveIconAttempt1++;
					}
				}
				String isEncumbered="N";
				String isEncumberedQuery = "select em_efin_encumbered from c_order where DocumentNo = '"+poNumber+"'";
				ResultSet isEncumberedQueryResult = s.executeQuery(isEncumberedQuery);
				if(isEncumberedQueryResult.next()) {
					isEncumbered = isEncumberedQueryResult.getString("em_efin_encumbered");
					
				}
				if(isEncumbered.equals("N")) {
					Thread.sleep(3000);
					WebElement applyUniqueCodetoLines = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(@class,'OBToolbarIconButton_icon_escm_po_apply_uniquecode')]")));
					applyUniqueCodetoLines.click();
					
					WebElement applyUniqueCodePopUpOkButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[@class='OBFormButton' and contains(text(),'Yes')]")));
					applyUniqueCodePopUpOkButton.click();
				}
				

				WebElement approve = wait.until(ExpectedConditions.elementToBeClickable(
						By.xpath("(//td[@class='OBToolbarTextButton' and contains(text(),'Approve')])[1]")));
				action.moveToElement(approve).click().build().perform();
				Thread.sleep(3000);
				wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
				wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
				wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainframe")));
				WebElement okButton1 = wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonOK")));
				okButton1.click();
				// Success message
				driver.switchTo().defaultContent();
				fluentWait.until(
						ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@id='Processing_Container']")));
				Thread.sleep(2000);
				WebElement successMessageLocator1 = null;
				int successMessageLocatorAttempt1 = 0;
				while (successMessageLocatorAttempt1 < 2) {
					try {
						successMessageLocator1 = wait.until(ExpectedConditions.presenceOfElementLocated(
								By.xpath("//div[contains(@class, 'OBMessageBarDescriptionText_success')]")));
						String actualMessage = successMessageLocator1.getText();
						String expectedMessage = "Process completed successfully.";
						if (actualMessage.contains(expectedMessage)) {
							isSuccess = true;
						} else {
							System.out.println("Success Message Not Matched");
						}
						break;
					} catch (Exception e) {
						successMessageLocatorAttempt1++;
					}
				}
				// logout
				Thread.sleep(2000);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//img[@name='isc_10main']")));
				driver.findElement(By.xpath("//img[@name='isc_10main']")).click();
				Thread.sleep(2000); // Retained for dynamic loading
				action.sendKeys(Keys.ENTER).build().perform();
			}
			System.out.println(poNumber);
		}

		finally {
			if (isSuccess) {
				
				driver.close();
				System.out.println("Driver closed");
			}
		}

	}

}
