package LocatorsOfWindows;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import CommonUtilities.ReusableUtilities;

public class ReceiptDeliveryVerificationLocators extends ReusableUtilities {
	WebDriver driver;
	Set<Integer> usedNumbers = new HashSet<>();

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
		WebElement filteredPORow = wait.until(
				ExpectedConditions.elementToBeClickable(By.xpath("(//div/nobr[contains(text(),'"+poNumber+"')])[1]")));
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
		Thread.sleep(1000);
		By approvalTypeLocator = By.xpath("(//input[@name='approvalType'])[2]");
		wait.until(ExpectedConditions.presenceOfElementLocated(approvalTypeLocator));
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(approvalTypeLocator));
		driver.findElement(approvalTypeLocator).sendKeys("Running RDV");

		Thread.sleep(1000);
		action.sendKeys(Keys.ENTER).build().perform();
	}

	public void certificateNumber() throws InterruptedException {
		int randomNumber;
		do {
			randomNumber = new Random().nextInt(50);
		} while (!usedNumbers.add(randomNumber));
		Thread.sleep(1000);
		By certificateNoLocator = By.xpath("(//input[@name='certificateNo'])[2]");
		wait.until(ExpectedConditions.presenceOfElementLocated(certificateNoLocator));
		WebElement certificateNoInput = driver.findElement(certificateNoLocator);
		certificateNoInput.sendKeys(String.valueOf(randomNumber));
	}
	
	public void matchAll() {
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Match All')]")));
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Match All')]"))).click();
	}
	
	public void generateAmarsaraf() {
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Generate Amarsaraf')]")));
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Generate Amarsaraf')]"))).click();
	}

}
