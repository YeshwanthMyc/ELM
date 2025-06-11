package TestComponents;

import org.testng.annotations.Test;

public class TriggerSuiteSetup extends BaseClass{
	@Test(enabled = false)
	public void trigger() {
		// This test doesn't run, it just makes TestNG load this class
	}
}
