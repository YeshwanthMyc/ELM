package LocatorsOfWindows;

import java.sql.SQLException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import CommonUtilities.ReusableUtilities;

public class POReceiptLocators extends ReusableUtilities{
	WebDriver driver;
	public POReceiptLocators(WebDriver driver, WebDriverWait wait, Actions action) throws SQLException {
		super(driver, wait, action);
		this.driver = driver;
		getConnection();
	}
	
	public void transactionType(String txrnType,String hijricurrentDate,String department) throws InterruptedException {
		WebElement transactionType = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='escmReceivingtype'])[2]")));
		transactionType.clear();

		if (txrnType.equalsIgnoreCase("Project Receiving")) {
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='escmReceivingtype'])[2]")))
					.sendKeys(txrnType);
			action.sendKeys(Keys.ENTER).build().perform();
			Thread.sleep(1000);
		}

		if (txrnType.equalsIgnoreCase("Site Receiving")) {

			WebElement receivingType = wait.until(
					ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='escmReceivingtype'])[2]")));
			receivingType.sendKeys(txrnType);
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
			
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='escmTobenefiName']"))).sendKeys(department);
			action.sendKeys(Keys.ENTER).build().perform();
			Thread.sleep(1000);
			
		}
	}
	
	public void passPO(String poDocNumber) throws InterruptedException {
		WebElement poNumber = wait
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='salesOrder']")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", poNumber);
		Thread.sleep(500);
		poNumber.sendKeys(poDocNumber);
		action.sendKeys(Keys.ENTER).build().perform();
	}
	
	public void addLines(String contractType) throws InterruptedException {
		Thread.sleep(1000);
		if(contractType.equalsIgnoreCase("Amt")) {
			WebElement addLines = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("(//td[normalize-space()='Add Lines'])[2]")));
			addLines.click();
		}
		
		if(contractType.equalsIgnoreCase("Qty")) {
			WebElement addLines = wait.until(ExpectedConditions.visibilityOfElementLocated(
					By.xpath("(//td[normalize-space()='Add Lines'])[1]")));
			addLines.click();
		}
	}
	
	public void popUpAction(String contractType,String productCode,String receiptAmount,String receiptQty) {
		WebElement popUpItem = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//td/div/nobr[.='"+productCode+"']")));
		popUpItem.click();

		if(contractType.equalsIgnoreCase("Amt")) {
			WebElement amount = wait
					.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='amount'])[1]")));
			amount.sendKeys(receiptAmount);
		}
		if(contractType.equalsIgnoreCase("Qty")) {
			WebElement amount = wait
					.until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@name='requestedQty'])[1]")));
			amount.sendKeys(receiptQty);
		}

		WebElement popUpOkButton = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//td[@class='OBFormButton' and contains(text(),'Done')]")));
		popUpOkButton.click();
	}
}
