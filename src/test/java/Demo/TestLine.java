package Demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestLine {
	public static String DB_URL = "jdbc:postgresql://localhost:5932/mainaccrual";
	public static String DB_USER = "tad";
	public static String DB_PASSWORD = "tad";
	public static Connection con;
	public static Statement s;
	public static void main(String[] args) throws InterruptedException, SQLException {

		String poNumber = "1017987";
		String windowName = "Purchase Order and Contracts summary";
		String product = "01010001";
		
		
		
		
		WebDriver driver = new ChromeDriver();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		Actions action = new Actions(driver);
		driver.get("http://qualiantracker.dyndns.org:9090/grpmain/security/Login");
		driver.findElement(By.xpath("//input[@id='user']")).sendKeys("Openbravo");
		driver.findElement(By.xpath("//input[@id='password']")).sendKeys("12");
		driver.findElement(By.xpath("//button[@id='buttonOK']")).click();

		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]")))
				.click();

		// select window from quick launch
		WebElement quickLaunchTextBox = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
		quickLaunchTextBox.sendKeys(windowName);
		Thread.sleep(500);
		action.sendKeys(Keys.ENTER).build().perform();

		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='documentNo']"))).sendKeys(poNumber);
		Thread.sleep(1000);
		WebElement filteredRow = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("(//td/div/nobr[contains(text(),'" + poNumber + "')])[1]")));

		action.moveToElement(filteredRow).doubleClick().build().perform();

		WebElement linesTab = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[contains(text(),'Lines Attributes')]")));
		action.moveToElement(linesTab).doubleClick().build().perform();
		Thread.sleep(500);

		WebElement filteredLineRow = wait.until(
				ExpectedConditions.elementToBeClickable(By.xpath("//div/nobr[contains(text(),'" + product + "')]")));
		action.moveToElement(filteredLineRow).doubleClick().build().perform();

		con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		s = con.createStatement();
		String ledgerAccountsQuery ="select CEV2.value as ledger_Value\r\n"
				+ "FROM efin_accountmap EAM\r\n"
				+ "JOIN C_ElementValue CEV1 ON EAM.C_ElementValue_ID = CEV1.C_ElementValue_ID\r\n"
				+ "JOIN C_ElementValue CEV2 ON EAM.GL_Account_ID = CEV2.C_ElementValue_ID\r\n"
				+ "WHERE CEV1.value = '400200300' and CEV2.value in(select C_ElementValue.value from efin_product_acct \r\n"
				+ "join m_product\r\n"
				+ "on efin_product_acct.m_product_id = m_product.m_product_id\r\n"
				+ "join C_ElementValue\r\n"
				+ "on efin_product_acct.P_Expense_Acct = C_ElementValue.C_ElementValue_id\r\n"
				+ "where m_product.Value ='01010001' and efin_product_acct.isactive='Y'\r\n"
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
		reLoadedLedgerAccount.sendKeys(ledgerAccounts.get(0));
		Thread.sleep(1500);
		action.sendKeys(Keys.ENTER).build().perform();
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
