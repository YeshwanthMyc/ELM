package Demo;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import CommonUtilities.ReusableUtilities;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import LocatorsOfWindows.InvoiceLocators;
import LocatorsOfWindows.PaymentOutLocators;
import TestComponents.BaseClass;
import TestComponents.RetryAnalyzer;

public class Testing extends BaseClass{
	@BeforeClass
	public void setupData() {
		commonData();
		poData();
		receiptData();
		rdvData();
		invData();
	}
	@Test(dataProvider ="PaymentOutData")
	public void InvoiceWithNoDeduction(HashMap<String, String> data) throws SQLException, InterruptedException {
		
		ReusableUtilities rs = new ReusableUtilities(driver,wait,action);
		rs.login("Openbravo", "12");
		rs.openWindow("Financial Account");
		
		//Select MOF Account row
		WebElement mofAccount= wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@class='OBGridCell']/div/nobr[contains(text(),'MOF')]")));
		action.moveToElement(mofAccount).click().build().perform();
		
		//Click Add Multiple Payment button
		WebElement add_Multiple_Payment = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@class='OBToolbarTextButton' and (contains(text(),'Add Multiple Payments'))]")));
		action.moveToElement(add_Multiple_Payment).click().build().perform();
		
		//Filter the invoice and selct the invoice in Multiple Payment popup
		WebElement invoice_Filter=wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinInvoice$documentNo']")));
		invoice_Filter.sendKeys("250000176");
		
		List<WebElement> add_Invoices_From_PopUp = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy
				(By.xpath("//table[@class='listTable']//tbody/tr/td[contains(@class,'OBGridCell')]/div/nobr[contains(text(),'250000176')]")));
		for(int i=0;i<add_Invoices_From_PopUp.size();i++) {
			add_Invoices_From_PopUp = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy
					(By.xpath("//table[@class='listTable']//tbody/tr/td[contains(@class,'OBGridCell')]/div/nobr[contains(text(),'250000176')]")));
			add_Invoices_From_PopUp.get(i).click();
		}
		
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@class='OBFormButton' and contains(text(),'Done')]"))).click();
		
		//select the latest payment
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinDocumentNo']"))).sendKeys("250000171");
		
		List<WebElement> payment_Sequence_Number = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//td[contains(@class,'OBGridCell')]/div/nobr[text()='250000171']/ancestor::td/following-sibling::td[2]\r\n"
				+ "")));
		
		WebElement maxElement = payment_Sequence_Number.stream().filter(e -> !e.getText().trim().isEmpty())
				.max(Comparator.comparingInt(e -> Integer.parseInt(e.getText().trim()))).orElse(null);
		if (maxElement != null) {
		    maxElement.click();
		} else {
		    System.out.println("No payment Sequence is present");
		}
		
		//Post
		WebElement post_Button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//td[@class='OBToolbarTextButton' and (contains(text(),'Post'))])[2]")));
		action.moveToElement(post_Button).click().build().perform();
		Thread.sleep(2000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[.='OK']"))).click();
		driver.switchTo().defaultContent();
		
		
		
	}

}
