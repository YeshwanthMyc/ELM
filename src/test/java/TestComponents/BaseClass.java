package TestComponents;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import CommonUtilities.ReusableUtilities;

public class BaseClass {
	public static WebDriver driver;
	public static WebDriverWait wait;
	public static Actions action;
	public static Properties prop;

	// Dates
	LocalDate today = LocalDate.now();
	LocalDate futuredate = today.plusDays(5);

	// Gregorian Date
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
	public String currentDate = today.format(formatter);
	public String futureDate = futuredate.format(formatter);

	// Hijri Date
	HijrahDate hijriToday = HijrahChronology.INSTANCE.date(today);
	HijrahDate hijriFutureDate = HijrahChronology.INSTANCE.date(futuredate);
	DateTimeFormatter hijriFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");
	public String hijricurrentDate = hijriToday.format(hijriFormatter);
	public String hijrifutureDate = hijriFutureDate.format(hijriFormatter);

	public static String poApprovalType = System.getProperty("approvalType", "Single");
	public static String contractType = System.getProperty("contractType", "Amt");
	public static String txrnType = System.getProperty("txrnType", "Project Receiving");

	public BaseClass() {
		try {
			prop = new Properties();
			FileInputStream fis = new FileInputStream(
					System.getProperty("user.dir") + "/src/main/java/GlobalProperties/GlobalData.properties");
			prop.load(fis);
		} catch (Exception e) {

		}
		

	}

	@BeforeSuite
	public void setUpDriver() {
		String browserName = prop.getProperty("browser");
		if (browserName.equalsIgnoreCase("chrome")) {
			driver = new ChromeDriver();
		}

		else if (browserName.equalsIgnoreCase("firefox")) {
			driver = new FirefoxDriver();
		}

		else {
			throw new RuntimeException("Unsupported browser: " + browserName);
		}

		wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		action = new Actions(driver);
	}

	@AfterSuite
	public void quitBrowser() {
		driver.quit();
	}

	@BeforeMethod
	public void launchApplication() {
		String currentUrl = driver.getCurrentUrl();
		String url = prop.getProperty("url");
		if (currentUrl == null || currentUrl.equals("about:blank") || !currentUrl.equals(url)) {
			driver.get(url);
		}
		driver.manage().window().maximize();
	}

	@AfterMethod(alwaysRun = true)
	public void logout() throws InterruptedException {
		ReusableUtilities.logout();
	}

	public List<HashMap<String, String>> getJSONData(String filePath) throws IOException {
		File file = new File(filePath);
		String jsonData = FileUtils.readFileToString(file, "UTF-8");

		ObjectMapper mapper = new ObjectMapper();

		List<HashMap<String, String>> data = mapper.readValue(jsonData,
				new TypeReference<List<HashMap<String, String>>>() {
				});
		return data;

	}

	public Object[][] getData(String filename) throws IOException {
		List<HashMap<String, String>> data = getJSONData(
				System.getProperty("user.dir") + "\\src\\test\\java\\TestData\\" + filename);
		Object[][] dataProvider = new Object[data.size()][];

		for (int i = 0; i < data.size(); i++) {
			HashMap<String, String> originalData = data.get(i);
			HashMap<String, String> finalData = new HashMap<>();

			for (String key : originalData.keySet()) {
				String jenkinsValue = System.getProperty(key);

				if (jenkinsValue != null && !jenkinsValue.isEmpty()) {
					finalData.put(key, jenkinsValue);
				} else {
					finalData.put(key, originalData.get(key));
				}
			}

			dataProvider[i] = new Object[] { finalData };
		}

		return dataProvider;
	}

	@DataProvider(name = "poData")
	public Object[][] getPOData() throws IOException {
		return getData("purchaseOrder.json");
	}

	@DataProvider(name = "poReceiptData")
	public Object[][] getPOReceiptData() throws IOException {
		return getData("POReceipt.json");

	}

	@DataProvider(name = "RDVData")
	public Object[][] getRDVData() throws IOException {
		return getData("RDV.json");

	}

}
