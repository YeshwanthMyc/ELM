package Base;

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
import org.testng.annotations.DataProvider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BaseClass {
	public WebDriver driver;
	public WebDriverWait wait;
	public Actions action;
	public Properties prop;

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

	public String approvalType = System.getProperty("approvalType", "Single");

	public BaseClass() {
		try {
			prop = new Properties();
			FileInputStream fis = new FileInputStream(
					System.getProperty("user.dir") + "/src/main/java/GlobalProperties/GlobalData.properties");
			prop.load(fis);
		} catch (Exception e) {

		}

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

		this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
		this.action = new Actions(driver);
	}

	public void launchApplication() {
		String url = prop.getProperty("url");
		driver.get(url);
		driver.manage().window().maximize();
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

	@DataProvider(name = "poData")
	public Object[][] getData() throws IOException {
		List<HashMap<String, String>> data = getJSONData(
				System.getProperty("user.dir") + "\\src\\test\\java\\TestData\\purchaseOrder.json");
		Object[][] dataProvider = new Object[data.size()][];

		for (int i = 0; i < data.size(); i++) {
			HashMap<String, String> originalData = data.get(i);
			HashMap<String, String> finalData = new HashMap<>();

			for (String key : originalData.keySet()) {
				String jenkinsValue = System.getProperty(key);
				if (jenkinsValue != null && !jenkinsValue.isEmpty()) {
					try {
						String decodedValue = new String(jenkinsValue.getBytes("ISO-8859-1"), "UTF-8");
						finalData.put(key, decodedValue);
					} catch (Exception e) {

						finalData.put(key, jenkinsValue);
					}
				} else {
					finalData.put(key, originalData.get(key));
				}
			}

			dataProvider[i] = new Object[] { finalData };
		}

		return dataProvider;
	}

}
