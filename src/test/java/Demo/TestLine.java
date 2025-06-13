package Demo;

import org.testng.annotations.Test;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import LocatorsOfWindows.ReceiptDeliveryVerificationLocators;
import TestComponents.BaseClass;

public class TestLine extends BaseClass {

	public static double matchedAmt = 0;
	public static double holdAmt = 0;

	// No Deduction
	public static double noDeductionNetMatchedAmt = 0;
	public static String noDeductionTxrnId = null;
	public static String noDeductionInvoioceId = null;

	boolean matchAllSuccess = false;
	boolean holdSuccess = false;
	boolean submitMessageSuccess = false;
	boolean generateAmarsarafMessageSuccess = false;

	String RDVApprovalType = "Multi";

	@Test(dataProvider = "RDVData")
	public void createRDVWithNoDeduction(HashMap<String, String> data) throws SQLException, InterruptedException {}

}
