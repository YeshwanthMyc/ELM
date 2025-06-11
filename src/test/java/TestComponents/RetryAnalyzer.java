package TestComponents;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int maxRetryCount = 1;

    @Override
    public boolean retry(ITestResult result) {
    	
    	Throwable throwable = result.getThrowable();
    	if(throwable instanceof AssertionError) {
    		return false;
    	}
        if (retryCount < maxRetryCount) {
            retryCount++;
            System.out.println("Retry Attempt:"+retryCount);
            return true;
        }
        return false;
    }
}

