package Demo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class POReceiptWithoutPOM {

	public static void main(String[] args) throws InterruptedException {
		String txrnType = "Project Receiving";
		String receiveType ="Amt";
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		Actions action = new Actions(driver);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(7));

		// Dates
		LocalDate today = LocalDate.now();
		LocalDate futuredate = today.plusDays(5);

		// Gregorian Date
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
		String currentDate = today.format(formatter);
		String futureDate = futuredate.format(formatter);

		// Hijri Date
		HijrahDate hijriToday = HijrahChronology.INSTANCE.date(today);
		HijrahDate hijriFutureDate = HijrahChronology.INSTANCE.date(futuredate);
		DateTimeFormatter hijriFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");
		String hijricurrentDate = hijriToday.format(hijriFormatter);
		String hijrifutureDate = hijriFutureDate.format(hijriFormatter);

		driver.get("http://qualiantracker.dyndns.org:9090/grpmain/security/Login");
		driver.findElement(By.xpath("//input[@id='user']")).sendKeys("Openbravo");
		driver.findElement(By.xpath("//input[@id='password']")).sendKeys("12");
		driver.findElement(By.xpath("//button[@id='buttonOK']")).click();

		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]")))
				.click();

		// select window from quick launch
		WebElement quickLaunchTextBox = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
		quickLaunchTextBox.sendKeys("PO Receipt");
		Thread.sleep(500);
		action.sendKeys(Keys.ENTER).build().perform();

		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.xpath("//td[contains(@class,'OBToolbarIconButton_icon_newDoc')]")))
				.click();

		// transactionType
		WebElement transactionType = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='escmReceivingtype'])[2]")));
		transactionType.clear();

		if (txrnType.equalsIgnoreCase("Project Receiving")) {
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='escmReceivingtype'])[2]")))
					.sendKeys("Project receiving");
			action.sendKeys(Keys.ENTER).build().perform();
			Thread.sleep(1000);
		}

		if (txrnType.equalsIgnoreCase("Site Receiving")) {

			WebElement receivingType = wait.until(
					ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='escmReceivingtype'])[2]")));
			receivingType.sendKeys("Site Receiving");
			action.sendKeys(Keys.ENTER).build().perform();

			Thread.sleep(1000);

			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='movementDate_dateTextField']")))
					.sendKeys(hijricurrentDate);
			action.sendKeys(Keys.TAB).build().perform();

			Thread.sleep(1000);
			wait.until(ExpectedConditions.elementToBeClickable(
					By.xpath("((//input[@name='warehouse'])[2]/ancestor::td/following-sibling::td)[1]"))).click();
			Thread.sleep(1000);

			action.sendKeys(Keys.DOWN).sendKeys(Keys.ENTER).build().perform();
			Thread.sleep(1000);
			
			WebElement beneficiary = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='escmTobeneficiary']")));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", beneficiary);
			beneficiary.sendKeys("Department");
			action.sendKeys(Keys.ENTER).build().perform();
			Thread.sleep(1000);
			
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='escmTobenefiName']"))).sendKeys("0200100");
			action.sendKeys(Keys.ENTER).build().perform();
			Thread.sleep(1000);
			
		}

		// PO
		WebElement poNumber = wait
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='salesOrder']")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", poNumber);
		Thread.sleep(500);
		poNumber.sendKeys("1018006");
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
				break;
			} catch (Exception e) {
				headerSaveIconAttempt++;
			}
		}

		Thread.sleep(1000);
		if(receiveType.equalsIgnoreCase("Amt")) {
			WebElement addLines = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("(//td[normalize-space()='Add Lines'])[2]")));
			addLines.click();
		}
		
		if(receiveType.equalsIgnoreCase("Qty")) {
			WebElement addLines = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("(//td[normalize-space()='Add Lines'])[1]")));
			addLines.click();
		}
		

		WebElement popUpItem = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//td/div/nobr[.='01010001']")));
		popUpItem.click();

		if(receiveType.equalsIgnoreCase("Amt")) {
			WebElement amount = wait
					.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='amount'])[1]")));
			amount.sendKeys("100");
		}
		if(receiveType.equalsIgnoreCase("Qty")) {
			WebElement amount = wait
					.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='requestedQty'])[1]")));
			amount.sendKeys("1");
		}

		WebElement popUpOkButton = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//td[@class='OBFormButton' and contains(text(),'Done')]")));
		popUpOkButton.click();

		// submit
		Thread.sleep(2000);
		WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
				"//td[@class='OBToolbarTextButton' and (contains(text(),'Submit') or contains(text(),'Approve'))]")));
		action.moveToElement(submitButton).click().build().perform();
		Thread.sleep(3000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainframe")));
		WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonOK")));
		okButton.click();
		driver.switchTo().defaultContent();
	}

}
