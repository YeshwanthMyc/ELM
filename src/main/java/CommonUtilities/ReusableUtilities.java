package CommonUtilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ReusableUtilities {

	// DB Details
	public static String DB_URL = "jdbc:postgresql://localhost:5932/mainaccrual";
	public static String DB_USER = "tad";
	public static String DB_PASSWORD = "tad";
	public Connection con;
	public Statement s;

	public WebDriver driver;
	public WebDriverWait wait;
	public WebDriverWait oneMinuteWait;
	public Actions action;

	public ReusableUtilities(WebDriver driver, WebDriverWait wait, Actions action) {
		this.driver = driver;
		this.wait = wait;
		this.oneMinuteWait = new WebDriverWait(driver, Duration.ofSeconds(60));
		this.action = action;
	}

	public void getConnection() throws SQLException {
		con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		s = con.createStatement();
	}

	public void login(String userName, String password) {
		// Login
		driver.findElement(By.xpath("//input[@id='user']")).sendKeys(userName);
		driver.findElement(By.xpath("//input[@id='password']")).sendKeys(password);
		driver.findElement(By.xpath("//button[@id='buttonOK']")).click();
	}

	public void logout() throws InterruptedException {
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
	}

	public void openWindow(String windowName) throws InterruptedException {
		// Quick Launch
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//div[@class='OBNavBarComponent']/div[1])[2]")))
				.click();

		// select window from quick launch
		WebElement quickLaunchTextBox = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//table[@role='presentation']/tbody/tr/td/div/input")));
		quickLaunchTextBox.sendKeys(windowName);
		Thread.sleep(500);
		action.sendKeys(Keys.ENTER).build().perform();
	}

	public void createNewHeader() throws InterruptedException {
		Thread.sleep(1000);
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.xpath("//td[contains(@class,'OBToolbarIconButton_icon_newDoc')]")))
				.click();
	}

	public void createNewLine() throws InterruptedException {
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

	}

	public void maximizeHeader() {
		WebElement header = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
				"(//div/div/table/tbody/tr/td[@class='OBTabBarButtonMainTitleSelected']//table/tbody/tr/td)[1]")));
		action.doubleClick(header).build().perform();
	}

	public void saveHeader() throws InterruptedException {
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
	}

	public void saveLine() throws InterruptedException {
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

	public void addAttachment(String attachmentFilePath) throws InterruptedException {
		Thread.sleep(1000);
		WebElement attachmentIcon = wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//td[contains(@class,'OBToolbarIconButton_icon_attach')]")));
		action.moveToElement(attachmentIcon).click().build().perform();

		WebElement attachmentFile = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@name='inpname']")));
		attachmentFile.sendKeys(attachmentFilePath);

		Thread.sleep(1000);
		WebElement attachmentSubmit = wait
				.until(ExpectedConditions.elementToBeClickable(By.xpath("(//td[contains(text(),'Submit')])[2]")));
		attachmentSubmit.click();

		if (attachmentSubmit.isEnabled()) {
			action.moveToElement(attachmentSubmit).click().build().perform();
		}
	}

	public void submitOrApprove() throws InterruptedException {
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

	public String submitMessage(String poNumber, String windowName) throws InterruptedException {
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//div[@id='Processing_Container']")));
		Thread.sleep(2000);
		WebElement MessageLocator = null;
		WebElement errorMessageLocator = null;
		int MessageLocatorAttempt = 0;
		String actualMessage = "";
		while (MessageLocatorAttempt < 2) {
			try {
				MessageLocator = wait.until(ExpectedConditions.presenceOfElementLocated(
						By.xpath("//div[contains(@class, 'OBMessageBarDescriptionText')]/div/b")));
				actualMessage = MessageLocator.getText();
				String expectedMessage = "Success";
				if (actualMessage.equalsIgnoreCase(expectedMessage)) {
					System.out.println(windowName + ": Submitted Successfully");

				} else {
					if (windowName.equalsIgnoreCase("PO Receipt")) {
						if (actualMessage.equalsIgnoreCase("Warning")) {
							String warningMessageQuery = "select EM_Efin_Posting_Errormsg from m_inout \r\n"
									+ "where c_order_id in(select c_order_id from c_order where documentNo='" + poNumber
									+ "')\r\n" + "\r\n" + "";
							ResultSet warningMessageQueryResult = s.executeQuery(warningMessageQuery);
							if (warningMessageQueryResult.next()) {
								actualMessage = warningMessageQueryResult.getString("EM_Efin_Posting_Errormsg");
							}
						}
					}

					if (actualMessage.equalsIgnoreCase("Error")) {
						errorMessageLocator = wait.until(ExpectedConditions.presenceOfElementLocated(
								By.xpath("//div[contains(@class, 'OBMessageBarDescriptionText_error')]/div")));
						String fullText = errorMessageLocator.getText();
						String[] lines = fullText.split("\\n");
						if (lines.length > 1) {
							actualMessage = lines[1];
						}else {
							actualMessage = lines[0];
						}
						
					}
				}

				return actualMessage;
			} catch (Exception e) {
				MessageLocatorAttempt++;
				return "Exception:" + e.getMessage();
			}
		}
		return actualMessage;
	}

	public void undoIcon() {
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
				By.xpath("//td[@class='OBToolbarIconButton_icon_undo OBToolbarIconButton']")));
		wait.until(ExpectedConditions
				.elementToBeClickable(By.xpath("//td[@class='OBToolbarIconButton_icon_undo OBToolbarIconButton']")))
				.click();
	}

	public void popUpOkButton() throws InterruptedException {
		Thread.sleep(3000);
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("OBClassicPopup_iframe")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("process")));
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.name("mainframe")));
		WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("buttonOK")));
		okButton.click();
		driver.switchTo().defaultContent();
	}

}
