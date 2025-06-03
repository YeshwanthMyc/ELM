package Demo;

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
		driver.findElement(By.xpath("(//input[@name='salesOrder'])[2]")).sendKeys("1018033 ");
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
				break;
			} catch (Exception e) {
				headerSaveIconAttempt++;
			}
		}

		// Logout
		WebElement mainIcon = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@name='isc_10main']")));
		Thread.sleep(500);
		action.moveToElement(mainIcon).click().build().perform();

		try {
			action.sendKeys(Keys.ENTER).pause(Duration.ofMillis(1000)).sendKeys(Keys.ENTER).build().perform();
			WebElement userNameLocator = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='user']")));
			if (userNameLocator.isDisplayed()) {

			}
		} catch (Exception e) {

			action.sendKeys(Keys.ENTER).pause(Duration.ofMillis(1000)).sendKeys(Keys.ENTER).build().perform();
		}

		// Login
		driver.findElement(By.xpath("//input[@id='user']")).sendKeys("Openbravo");
		driver.findElement(By.xpath("//input[@id='password']")).sendKeys("12");
		driver.findElement(By.xpath("//button[@id='buttonOK']")).click();

		// Open Window
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]")))
				.click();
		WebElement quickLaunchTextBox1 = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
		quickLaunchTextBox1.sendKeys("Receipt delivery verification");
		Thread.sleep(500);
		action.sendKeys(Keys.ENTER).build().perform();

		// PO filter
		Thread.sleep(2000);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='salesOrder']")));
		driver.findElement(By.xpath("//input[@name='salesOrder']")).sendKeys("1018033 ");
		Thread.sleep(1000);
		action.sendKeys(Keys.ENTER).build().perform();
		
		//Select filtered record
		Thread.sleep(1000);
		WebElement filteredPORow = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div/nobr[contains(text(),'1018033')])[1]")));
		action.moveToElement(filteredPORow).click().perform();
		((JavascriptExecutor) driver).executeScript("arguments[0].click();",filteredPORow );

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

	}

}
