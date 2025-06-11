package Demo;

import java.nio.channels.SelectableChannel;
import java.time.Duration;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RDVWithoutPOM {

	public static void main(String[] args) throws InterruptedException {
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		Actions action = new Actions(driver);
		driver.get("http://qualiantracker.dyndns.org:9090/grpmain/security/Login");

		// Login
		driver.findElement(By.xpath("//input[@id='user']")).sendKeys("Openbravo");
		driver.findElement(By.xpath("//input[@id='password']")).sendKeys("12");
		driver.findElement(By.xpath("//button[@id='buttonOK']")).click();

		// Open Window
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]")))
				.click();
		WebElement quickLaunchTextBox = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
		quickLaunchTextBox.sendKeys("Receipt delivery verification");
		Thread.sleep(500);
		action.sendKeys(Keys.ENTER).build().perform();

		// Create new header
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//td[contains(@class,'OBToolbarIconButton_icon_newDoc')]")))
				.click();

		// Pass PO
		Thread.sleep(1000);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='salesOrder'])[2]")));
		driver.findElement(By.xpath("(//input[@name='salesOrder'])[2]")).sendKeys("1018033");
		Thread.sleep(1000);
		action.sendKeys(Keys.ENTER).build().perform();

		// Save Header
		Thread.sleep(1000);
		WebElement headerSaveIcon = null;
		int headerSaveIconAttempt = 0;
		while (headerSaveIconAttempt < 2) {
			try {
				headerSaveIcon = wait.until(ExpectedConditions.elementToBeClickable(
						By.xpath("//td[contains(@class,'OBToolbarIconButton_icon_save OBToolbarIconButton')]")));
				headerSaveIcon.click();
				wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(
						"(//td[contains(@class,'OBToolbarIconButton_icon_save OBToolbarIconButtonDisabled')])[1]")));
				break;
			} catch (Exception e) {
				headerSaveIconAttempt++;
			}
		}

		// undo button
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
				By.xpath("//td[@class='OBToolbarIconButton_icon_undo OBToolbarIconButton']")));
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//td[@class='OBToolbarIconButton_icon_undo OBToolbarIconButton']")))
				.click();

		// PO filter
		Thread.sleep(2000);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='salesOrder']")));
		driver.findElement(By.xpath("//input[@name='salesOrder']")).sendKeys("1018033 ");
		Thread.sleep(1000);
		action.sendKeys(Keys.ENTER).build().perform();

		// Select filtered record
		Thread.sleep(1000);
		WebElement filteredPORow = wait.until(
				ExpectedConditions.elementToBeClickable(By.xpath("(//div/nobr[contains(text(),'1018033')])[1]")));
		action.moveToElement(filteredPORow).click().perform();
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", filteredPORow);

		// Navigate to Transaction version
		WebElement linesTab = wait.until(
				ExpectedConditions.elementToBeClickable(By.xpath("//td[contains(text(),'Transaction versions')]")));
		action.moveToElement(linesTab).doubleClick().build().perform();
		Thread.sleep(500);

		// Create new line
		Thread.sleep(1500);
		int newLineAttempt = 0;
		while (newLineAttempt < 2) {
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(
						By.xpath("(//td[contains(@class,'OBToolbarIconButton_icon_newDoc')])[2]"))).click();
				break;
			} catch (Exception e) {

			}
			newLineAttempt++;
		}

		// Approval type
		Thread.sleep(1000);
		By approvalTypeLocator = By.xpath("(//input[@name='approvalType'])[2]");
		wait.until(ExpectedConditions.presenceOfElementLocated(approvalTypeLocator));
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(approvalTypeLocator));
		driver.findElement(approvalTypeLocator).sendKeys("Running RDV");

		Thread.sleep(1000);
		action.sendKeys(Keys.ENTER).build().perform();

		// Certificate Number
		Set<Integer> usedNumbers = new HashSet<>();
		int randomNumber;
		do {
			randomNumber = new Random().nextInt(50);
		} while (!usedNumbers.add(randomNumber));

		System.out.println(usedNumbers);
		Thread.sleep(1000);
		By certificateNoLocator = By.xpath("(//input[@name='certificateNo'])[2]");
		wait.until(ExpectedConditions.presenceOfElementLocated(certificateNoLocator));
		WebElement certificateNoInput = driver.findElement(certificateNoLocator);
		certificateNoInput.sendKeys(String.valueOf(randomNumber));

		// Save Line
		Thread.sleep(1000);
		WebElement lineSaveIcon = null;
		int lineSaveIconAttempt = 0;
		while (lineSaveIconAttempt < 2) {
			try {
				lineSaveIcon = wait.until(ExpectedConditions
						.elementToBeClickable(By.xpath("(//td[contains(@class,'OBToolbarIconButton_icon_save')])[3]")));
				lineSaveIcon.click();
				Thread.sleep(2000);
				break;
			} catch (Exception e) {
				lineSaveIconAttempt++;
			}
		}

		// Match All
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Match All')]")));
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Match All')]")))
				.click();

		Thread.sleep(3000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainframe")));
		WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonOK")));
		okButton.click();
		driver.switchTo().defaultContent();

		// Hold action
		Thread.sleep(1000);
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Hold Action Txn')]")));
		wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Hold Action Txn')]"))).click();

		Thread.sleep(2000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));

		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Add')]")));
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Add')]"))).click();

		WebElement holdPopUpDropDown = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//select[@name='holdtype']")));
		Select holdName = new Select(holdPopUpDropDown);
		holdName.selectByContainsVisibleText("إيقاف اتعاب الإشراف");

		// hold amount
		WebElement holdAmount = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//input[@name='holdamount'])[2]")));
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", holdAmount);
		holdAmount.sendKeys("10");
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Save')]"))).click();
		Thread.sleep(1000);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='Dialog_OK']"))).click();
		Thread.sleep(1000);
		driver.switchTo().defaultContent();
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("(//div[contains(@class,'OBPopupIconClose') and contains(@onscroll,'closeButton')])[1]")))
				.click();

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
		Select penaltyName = new Select(penaltyPopUpDropDown);
		penaltyName.selectByContainsVisibleText("مقابل غرامة تأخير 10%");

		// Select revenue account
		WebElement accountDropDown = wait
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//select[@name='accounttype']")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", accountDropDown);
		Select accountName = new Select(accountDropDown);
		accountName.selectByContainsVisibleText("Adjustment");

		WebElement uniqueCodeDropDown = wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("(//span[@class='select2-selection select2-selection--single'])[2]")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", uniqueCodeDropDown);
		Thread.sleep(500);
		action.moveToElement(
				driver.findElement(By.xpath("(//span[@class='select2-selection select2-selection--single'])[2]")))
				.click().build().perform();

		Thread.sleep(500);
		WebElement accountNumber = wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("(//span[@class='select2-search select2-search--dropdown'])/input")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", accountNumber);
		accountNumber.sendKeys("1431");

		Thread.sleep(500);
		action.sendKeys(Keys.ENTER).build().perform();

		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Save')]"))).click();
		Thread.sleep(1000);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='Dialog_OK']"))).click();
		Thread.sleep(1000);
		driver.switchTo().defaultContent();

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
		penaltyName1.selectByContainsVisibleText("In exchange for payments to another contractor");

		WebElement penaltyAmount = wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//input[@name='penaltyamount'])[2]")));
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", penaltyAmount);
		penaltyAmount.sendKeys("10");

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
		externalContractorName.sendKeys("مكتب عبدالرحمن بن عبدالله النور للاستشارات الهندسية");

		Thread.sleep(500);
		action.sendKeys(Keys.ENTER).build().perform();

		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//div[@class='ui-pg-div' and contains(text(),'Save')]"))).click();
		Thread.sleep(1000);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='Dialog_OK']"))).click();
		Thread.sleep(1000);
		driver.switchTo().defaultContent();

		// Submit
		Thread.sleep(2000);
		WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
				"//td[@class='OBToolbarTextButton' and (contains(text(),'Submit') or contains(text(),'Approve'))]")));
		action.moveToElement(submitButton).click().build().perform();
		Thread.sleep(3000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainframe")));
		WebElement okButton1 = wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonOK")));
		okButton1.click();
		driver.switchTo().defaultContent();

		// Generate amarsarf
		wait.until(ExpectedConditions.presenceOfElementLocated(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Generate Amarsaraf')]")));
		wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Generate Amarsaraf')]"))).click();

		Thread.sleep(3000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainframe")));
		WebElement okButton2 = wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonOK")));
		okButton2.click();
		driver.switchTo().defaultContent();
	}

}
