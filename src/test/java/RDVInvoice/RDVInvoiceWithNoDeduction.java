package RDVInvoice;

import org.testng.annotations.Test;
import RDVCreation.RDVCreation;
import TestComponents.BaseClass;
public class RDVInvoiceWithNoDeduction extends BaseClass{

	@Test(dependsOnGroups = "NoDeduction")
	public void InvoiceWithNoDeduction() {
		RDVCreation RDV = new RDVCreation();
		System.out.println(RDV.matchedAmt);
	}
}
