package Demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

import org.checkerframework.checker.units.qual.s;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

public class RDVInvoiceWithNoDeductionWithoutPOM {
	String noDeductionTxrnId = "9804668478F646E48FF4DEF459D38E76";

	public static String DB_URL = "jdbc:postgresql://localhost:5932/mainaccrual";
	public static String DB_USER = "tad";
	public static String DB_PASSWORD = "tad";
	public Connection con;
	public Statement s;

	String tempDocNumber;
	String DocNumber;
	String Netmatch_Amt;
	
	String Line_Net_Amt;
	
	double mainLine;	
	double taxLine;
	double netMatchAmount;
	
	boolean amountValidations=false;

	@Test()
	public void InvoiceWithNoDeduction() throws SQLException, InterruptedException {
		con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		s = con.createStatement();
	

		// Get Temporary Document Number
		String documentNoQuery = "select DocumentNo from C_Invoice where C_Invoice_id in \r\n"
				+ "(select C_Invoice_ID from Efin_RDVTxn where Efin_RDVTxn_id ='" + noDeductionTxrnId + "')";
		ResultSet tempDocNumQueryResult = s.executeQuery(documentNoQuery);
		while (tempDocNumQueryResult.next()) {
			tempDocNumber = tempDocNumQueryResult.getString("DocumentNo");
			System.out.println(tempDocNumber);
			break;
		}

		// Open Invoice Window and Create New
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		Actions action = new Actions(driver);
		driver.get("http://qualiantracker.dyndns.org:9090/grpmain/security/Login");

		// Login
		driver.findElement(By.xpath("//input[@id='user']")).sendKeys("4350143");
		driver.findElement(By.xpath("//input[@id='password']")).sendKeys("12");
		driver.findElement(By.xpath("//button[@id='buttonOK']")).click();

		// Open Window... create new Method
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]")))
				.click();
		WebElement quickLaunchTextBox = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
		quickLaunchTextBox.sendKeys("Purchase Invoice");
		Thread.sleep(500);
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@class='listTable']//td/div/nobr[text()='Purchase Invoice']")))
				.click();

		WebElement documentNoFilter = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='documentNo']")));
		documentNoFilter.sendKeys(tempDocNumber);

		WebElement filteredRow = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//div[@role='presentation']/nobr[text()='" + tempDocNumber + "']")));
		action.moveToElement(filteredRow).doubleClick().build().perform();

		WebElement mofReqNum = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinMofreqno']")));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", mofReqNum);
		mofReqNum.sendKeys("790495758477");
		
		Thread.sleep(1000);
		for (int i = 0; i < 3; i++) {
			try {
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//textarea[@name='description']")))
						.sendKeys("790495758477");
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}

		// Tax Method
		for (int i = 0; i < 3; i++) {
			try {
				WebElement isTaxLine = wait.until(ExpectedConditions.elementToBeClickable(
						By.xpath("//div[@class='OBFormFieldLabel' and text()='Is Tax Line']/span")));
				isTaxLine.click();
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}

		for (int i = 0; i < 3; i++) {
			try {
				WebElement taxMethod = wait
						.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinTaxMethod']")));
				taxMethod.sendKeys("احتساب على الوزارة 15%");
				action.sendKeys(Keys.ENTER).build().perform();
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}

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
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}

		for (int i = 0; i < 3; i++) {
			try {
				Thread.sleep(1000);
				WebElement inwardDate = wait.until(ExpectedConditions
						.presenceOfElementLocated(By.xpath("//input[@name='efinInwarddateGreg_dateTextField']")));
				inwardDate.click();
				inwardDate.sendKeys("17-06-2025");
				Thread.sleep(500);
				action.sendKeys(Keys.TAB).build().perform();
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}

		for (int i = 0; i < 3; i++) {
			try {
				wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='efinSupinvno']")))
						.sendKeys("1223");
				break;
			} catch (Exception e) {
				Thread.sleep(500);
			}
		}

		Thread.sleep(1000);
		WebElement inwardDate = wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//input[@name='efinSupinvdateG_dateTextField']")));
		inwardDate.click();
		inwardDate.sendKeys("17-06-2025");
		Thread.sleep(500);
		action.sendKeys(Keys.TAB).build().perform();

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

		// undo Icon
		Thread.sleep(1000);
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
				By.xpath("//td[@class='OBToolbarIconButton_icon_undo OBToolbarIconButton']")));
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//td[@class='OBToolbarIconButton_icon_undo OBToolbarIconButton']")))
				.click();

		// Add Tax Lines
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
		
		//Amount Validation
		String netMatchAmtQuery = 
				"select Netmatch_Amt from Efin_RDVTxn where Efin_RDVTxn_id ='"+noDeductionTxrnId+"'";
		ResultSet netMatchAmtQueryResult = s.executeQuery(netMatchAmtQuery);
		while (netMatchAmtQueryResult.next()) {
			Netmatch_Amt = netMatchAmtQueryResult.getString("Netmatch_Amt");
			netMatchAmount = Double.parseDouble(Netmatch_Amt);
			System.out.println(netMatchAmount);
			break;
		}
		
		String noDeductionLineNetAmountWithoutTaxQuery="select LineNetAmt from C_InvoiceLine where C_Invoice_id in \r\n"
				+ "(select C_Invoice_id from C_Invoice where DocumentNo='"+tempDocNumber+"') and EM_Efin_Istax='N'";
		ResultSet noDeductionLineNetAmountWithoutTaxQueryResult = s.executeQuery(noDeductionLineNetAmountWithoutTaxQuery);
		while (noDeductionLineNetAmountWithoutTaxQueryResult.next()) {
			Line_Net_Amt = noDeductionLineNetAmountWithoutTaxQueryResult.getString("LineNetAmt");
			mainLine = Double.parseDouble(Line_Net_Amt);
			System.out.println(mainLine);
			break;
		}
		
		String noDeductionLineNetAmountWithTaxQuery="select LineNetAmt from C_InvoiceLine where C_Invoice_id in \r\n"
				+ "(select C_Invoice_id from C_Invoice where DocumentNo='"+tempDocNumber+"') and EM_Efin_Istax='Y'";
		ResultSet noDeductionLineNetAmountWithTaxQueryResult = s.executeQuery(noDeductionLineNetAmountWithTaxQuery);
		while (noDeductionLineNetAmountWithTaxQueryResult.next()) {
			Line_Net_Amt = noDeductionLineNetAmountWithTaxQueryResult.getString("LineNetAmt");
			taxLine = Double.parseDouble(Line_Net_Amt);
			System.out.println(taxLine);
			break;
		}
		
		if(netMatchAmount==mainLine&&taxLine==mainLine*0.15) {
			amountValidations=true;
		}
		System.out.println(amountValidations);
		// submit
		Thread.sleep(2000);
		WebElement submitButton = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//td[@class='OBToolbarTextButton' and contains(text(),'Submit')]")));
		action.moveToElement(submitButton).click().build().perform();
		Thread.sleep(3000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainframe")));
		WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonOK")));
		okButton.click();

		// Get Document Number
		ResultSet DocNumQueryResult = s.executeQuery(documentNoQuery);
		while (DocNumQueryResult.next()) {
			DocNumber = DocNumQueryResult.getString("DocumentNo");
			System.out.println(DocNumber);
			break;
		}

	}
}
