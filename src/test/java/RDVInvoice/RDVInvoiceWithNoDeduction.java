package RDVInvoice;

import org.testng.annotations.Test;
import RDVCreation.RDVCreation;
import TestComponents.BaseClass;
public class RDVInvoiceWithNoDeduction extends BaseClass{

	@Test(dependsOnGroups = "NoDeduction",groups = {"NoDeduction"})
	public void InvoiceWithNoDeduction() {
		
	}
}
