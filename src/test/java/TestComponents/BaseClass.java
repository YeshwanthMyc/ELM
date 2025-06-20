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
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
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

	// Common Data
	public static String originalMessage;
	// Dates
	public static String currentDate;
	public static String futuredate;
	public static String futureDate;
	public static String hijricurrentDate;
	public static String hijrifutureDate;
	
	public static String actualMessageForSubmittext;
	public static Map<String, Object> SubmitMessageresult;
	public static boolean submitMessageSuccessResult;
	public static Map<String, Object> approvalresult;
	public static void commonData() {
		originalMessage = null;

		LocalDate today = LocalDate.now();
		LocalDate futuredate = today.plusDays(5);

		// Gregorian Date
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
		currentDate = today.format(formatter);
		futureDate = futuredate.format(formatter);

		// Hijri Date
		HijrahDate hijriToday = HijrahChronology.INSTANCE.date(today);
		HijrahDate hijriFutureDate = HijrahChronology.INSTANCE.date(futuredate);
		DateTimeFormatter hijriFormatter = DateTimeFormatter.ofPattern("d/M/yyyy");
		hijricurrentDate = hijriToday.format(hijriFormatter);
		hijrifutureDate = hijriFutureDate.format(hijriFormatter);
		

		actualMessageForSubmittext=null;
		SubmitMessageresult=null;
		submitMessageSuccessResult=false;
		approvalresult=null;

	}

	// PO Data
	public static String poApprovalType;
	public static String contractType;
	public static String txrnType;

	public static void poData() {
		poApprovalType = System.getProperty("POapprovalType", "Single");
		contractType = System.getProperty("contractType", "Amt");
		txrnType = System.getProperty("txrnType", "Project Receiving");
	}

	// Receipt Data
	public static String poDocNumber; // Will Be used in Receipt and RDV
	public static String productCode;
	public static double receiptAmount;
	public static double receiptQty;

	public static void receiptData() {
		poDocNumber = "";
		productCode = "";
		receiptAmount = 0;
		receiptQty = 0;
	}

	// RDV Data
	public static String RDVApprovalType;

	public static double matchedAmt;
	public static double holdAmt;
	public static double penaltyAmt;
	public static double externalpenaltyAmt;

	public static double noDeductionNetMatchedAmt;
	public static String noDeductionTxrnId;
	public static String noDeductionInvoioceId;
	
	public static String holdDeductionTxrnId;
	
	public static String penaltyDeductionTxrnId;
	
	public static String extPenaltyDeductionTxrnId;
	
	public static String allDeductionTxrnId;

	public static boolean matchAllSuccess;
	public static boolean holdSuccess;
	public static boolean penaltySuccess;
	public static boolean externalpenaltySuccess;

	public static boolean submitMessageSuccess;
	public static boolean generateAmarsarafMessageSuccess;
	
	public static String penaltyName;
	public static String revenueAccount;
	public static String externalPenaltyName;
	public static String externalPenaltySupplierName;

	public static void rdvData() {
		RDVApprovalType = System.getProperty("RDVapprovalType", "Single");

		// Initial values of Match/Hold/Penalty/External Penalty
		matchedAmt = 0;
		holdAmt = 0;
		penaltyAmt = 0;
		externalpenaltyAmt = 0;

		// No Deduction Method Result
		noDeductionNetMatchedAmt = 0;
		noDeductionTxrnId =null;
		noDeductionInvoioceId = null;
		
		//Hold Deduction Result
		holdDeductionTxrnId=null;
		
		//Penalty Deduction Result
		penaltyDeductionTxrnId=null;
		
		//Ext penalty Deduction Result
		extPenaltyDeductionTxrnId=null;
		
		//All Deduction Result
		allDeductionTxrnId=null;

		// Initial Values for Verification
		matchAllSuccess = false;
		holdSuccess = false;
		penaltySuccess = false;
		externalpenaltySuccess = false;

		submitMessageSuccess = false;
		generateAmarsarafMessageSuccess = false;
		
		//Data For Invoice
		penaltyName=null;
		revenueAccount=null;
		externalPenaltyName=null;
		externalPenaltySupplierName=null;

	}
	
	public static String invDocNumber;
	public static String Deduction;
	public static void invData() {
		 invDocNumber=null;
		 Deduction=System.getProperty("Deduction", "External Penalty");
	}

	public BaseClass() {
		try {
			prop = new Properties();
			FileInputStream fis = new FileInputStream(
					System.getProperty("user.dir") + "/src/main/java/GlobalProperties/GlobalData.properties");
			prop.load(fis);
		} catch (Exception e) {

		}

	}

	@BeforeSuite(alwaysRun = true)
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

	@AfterSuite(alwaysRun = true)
	public void quitBrowser() {
		driver.quit();
	}

	@BeforeMethod(alwaysRun = true)
	public void launchApplication() {
		String currentUrl = driver.getCurrentUrl();
		String url = prop.getProperty("url");
		if (currentUrl == null || currentUrl.equals("about:blank") || !currentUrl.equals(url)) {
			driver.get(url);
		}
		driver.manage().window().maximize();
	}

	@AfterMethod()
	public void logout() throws InterruptedException {
		if(driver!=null) {
			ReusableUtilities resUse = new ReusableUtilities(driver, wait, action);
			resUse.logout();
		}
		
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
	
	@DataProvider(name = "RDVInvoiceData")
	public Object[][] RDVInvoiceData() throws IOException {
		return getData("RDVInvoiceData.json");

	}

}
