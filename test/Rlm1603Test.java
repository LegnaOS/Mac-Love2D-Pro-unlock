// SPDX-License-Identifier: GPL-3.0-only
import com.reprisesoftware.rlm.*;
public final class Rlm1603Test {
    public static void main(String[] args) throws Exception {
        RlmLicense license=new RlmLicense(null,"fixture-product","1.0",1);
        if(license.status()!=0||license.daysToExpiration()!=9999||!license.getProduct().equals("fixture-product"))
            throw new AssertionError("profile methods");
        try { license.getExpiration(); throw new AssertionError("zero native handle accepted"); }
        catch(IllegalStateException expected) {}
        license.checkin(); license.checkin();
        if(license.daysToExpiration()!=0)throw new AssertionError("checkin state");
        try { license.status(); throw new AssertionError("closed license accepted"); }
        catch(RlmException expected) {}
        if(java.lang.reflect.Modifier.isPublic(RlmLicense.class.getDeclaredMethod("getLicenseHandle").getModifiers()))
            throw new AssertionError("visibility mismatch");
        System.out.println("Rlm1603 profile lifecycle and zero-handle guard: PASS");
    }
}
