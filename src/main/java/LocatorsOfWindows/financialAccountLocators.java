package LocatorsOfWindows;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import CommonUtilities.ReusableUtilities;

public class financialAccountLocators extends ReusableUtilities {
	WebDriver driver;

	public financialAccountLocators(WebDriver driver, WebDriverWait wait, Actions action) throws SQLException {
		super(driver, wait, action);
		this.driver = driver;
		getConnection();
	}

	public void selectMOFAccount() {
		WebElement mofAccount = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//td[@class='OBGridCell']/div/nobr[contains(text(),'MOF')]")));
		action.moveToElement(mofAccount).click().build().perform();
	}

	public void clickAddMultiplePayment() {
		WebElement add_Multiple_Payment = wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//td[@class='OBToolbarTextButton' and (contains(text(),'Add Multiple Payments'))]")));
		action.moveToElement(add_Multiple_Payment).click().build().perform();
	}

	public void selectInvoiceFromPopUp(String invDocNumber) {
		WebElement invoice_Filter = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinInvoice$documentNo']")));
		invoice_Filter.sendKeys(invDocNumber);

		List<WebElement> add_Invoices_From_PopUp = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By
				.xpath("//table[@class='listTable']//tbody/tr/td[contains(@class,'OBGridCell')]/div/nobr[contains(text(),'"
						+ invDocNumber + "')]")));

		for (int i = 0; i < add_Invoices_From_PopUp.size(); i++) {
			List<WebElement> invoices = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(
					"//table[@class='listTable']//tbody/tr/td[contains(@class,'OBGridCell')]/div/nobr[contains(text(),'"
							+ invDocNumber + "')]")));

			wait.until(ExpectedConditions.elementToBeClickable(invoices.get(i))).click();
		}

		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//td[@class='OBFormButton' and contains(text(),'Done')]"))).click();
	}

	public void selectLatestPayment(String invDocNumber) throws InterruptedException {
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinDocumentNo']")))
				.sendKeys(invDocNumber);
		Thread.sleep(5000);
		List<WebElement> payment_Sequence_Number = oneMinuteWait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By
				.xpath("//td[contains(@class,'OBGridCell')]/div/nobr[text()='"+invDocNumber+"']/ancestor::td/following-sibling::td[2]\r\n"
						+ "")));

		WebElement maxElement = payment_Sequence_Number.stream().filter(e -> !e.getText().trim().isEmpty())
				.max(Comparator.comparingInt(e -> Integer.parseInt(e.getText().trim()))).orElse(null);
		if (maxElement != null) {
			maxElement.click();
		} else {
			System.out.println("No payment Sequence is present");
		}
	}
	
	public void post() throws InterruptedException {
		WebElement post_Button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//td[@class='OBToolbarTextButton' and (contains(text(),'Post'))])[2]")));
		action.moveToElement(post_Button).click().build().perform();
		Thread.sleep(2000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[.='OK']"))).click();
		driver.switchTo().defaultContent();
	}
}
