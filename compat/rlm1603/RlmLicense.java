package com.reprisesoftware.rlm;

/* SPDX-License-Identifier: GPL-3.0-only
 * Adapted from pinned LoveLive2D upstream for the exact Cubism 5.3.04 profile.
 * Public descriptors match the inspected 1603 class. Only this class is replaced;
 * RlmHandle, native rlm1603 and all other original classes remain unchanged.
 */
public class RlmLicense implements RlmConstants {
    private long licHandle;
    private boolean valid;
    private String requestedProduct = "";

    // Unexercised native attributes must not dereference a synthetic zero handle.
    private long nativeHandle() {
        if (licHandle == 0) throw new IllegalStateException("Native attribute unavailable for this compatibility profile");
        return licHandle;
    }

    private native long rlmCheckout(long j, String str, String str2, int i);

    private native long rlmCheckoutProduct(long j, long j2, String str, int i);

    private native int rlmLicenseStat(long j);

    private native int rlmCheckin(long j);

    private native int rlmGetAttrHealth(long j);

    private native int rlmGoodOnce(long j);

    private native String rlmLicenseAkey(long j);

    private native String rlmLicenseContract(long j);

    private native String rlmLicenseCustomer(long j);

    private native int rlmLicenseCount(long j);

    private native int rlmLicenseDetachedDemo(long j);

    private native String rlmLicenseExp(long j);

    private native String rlmLicenseExpTime(long j);

    private native int rlmLicenseHold(long j);

    private native int rlmLicenseHostBased(long j);

    private native String rlmLicenseHostid(long j);

    private native int rlmLicenseIsMetered(long j);

    private native String rlmLicenseIssued(long j);

    private native String rlmLicenseIssuer(long j);

    private native String rlmLicenseLineItem(long j);

    private native int rlmLicenseMaxRoam(long j);

    private native int rlmLicenseMaxRoamCount(long j);

    private native int rlmLicenseMeterCounter(long j);

    private native int rlmLicenseMinRemove(long j);

    private native int rlmLicenseMinTimeout(long j);

    private native int rlmLicenseMinCheckout(long j);

    private native String rlmLicensePlatforms(long j);

    private native String rlmLicenseProduct(long j);

    private native String rlmLicenseServer(long j);

    private native int rlmLicenseSingle(long j);

    private native int rlmLicenseShare(long j);

    private native int rlmLicenseSoftLimit(long j);

    private native String rlmLicenseStart(long j);

    private native String rlmLicenseTeams(long j);

    private native int rlmLicenseType(long j);

    private native int rlmLicenseTimezone(long j);

    private native int rlmLicenseUncounted(long j);

    private native int rlmLicenseUserBased(long j);

    private native String rlmLicenseVersion(long j);

    private native int rlmLicenseNamedUserCount(long j);

    private native int rlmLicenseNamedUserMinHours(long j);

    private native String rlmLicenseGetLFPath(long j);

    private native int rlmLicenseExpDays(long j);

    private native int rlmLicenseRoaming(long j);

    private native int rlmLicenseMaxShare(long j);

    private native String rlmLicenseOptions(long j);

    private native int rlmLicenseClientCache(long j);

    private native int rlmLicenseIsCached(long j);

    private native int rlmAuthCheck(long j, String str);

    public RlmLicense(RlmHandle handle, String product, String version, int count) throws RlmException {
        this.valid = true;
        this.requestedProduct = product;

        /*
        * ORIGINAL CODE
        */
        // this.licHandle = rlmCheckout(handle.getHandle(), product, version, count);
        // int stat = rlmLicenseStat(nativeHandle());
        // if (stat != 0 && stat != -25 && stat != -39) {
        //     throw new RlmException(stat, handle, this);
        // }
        // this.valid = true;
    }

    public RlmLicense(RlmHandle handle, RlmAvailableProduct product, String version, int count) throws RlmException {
        this.valid = true;
        this.requestedProduct = "";

        /*
        * ORIGINAL CODE
        */
        // long prodHandle = product.getProdHandle();
        // handle.positionProdHandle(prodHandle, product.getIndex());
        // this.licHandle = rlmCheckoutProduct(handle.getHandle(), prodHandle, version, count);
        // int stat = rlmLicenseStat(nativeHandle());
        // if (stat != 0 && stat != -25 && stat != -39) {
        //     throw new RlmException(stat, handle, this);
        // }
        // this.valid = true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    protected long getLicenseHandle() {
        return this.licHandle;
    }

    public int status() throws RlmException {
        if (!valid) throw new RlmException(-19);
        return 0;

        /*
        * ORIGINAL CODE
        */
        // if (this.valid) {
        //     int stat = rlmLicenseStat(nativeHandle());
        //     return stat;
        // }
        // throw new RlmException(-19);
    }

    public void checkin() {
        // No native checkout was performed, so never check in a zero handle.
        this.valid = false;
    }

    public int getAttrHealth() throws RlmException {
        if (this.valid) {
            int stat = rlmGetAttrHealth(nativeHandle());
            return stat;
        }
        throw new RlmException(-19);
    }

    public String getActKey() {
        return rlmLicenseAkey(nativeHandle());
    }

    public boolean goodOnce() {
        boolean ret = false;
        int stat = rlmGoodOnce(nativeHandle());
        if (stat != 0) {
            ret = true;
        }
        return ret;
    }

    public int getCLientCache() {
        return rlmLicenseClientCache(nativeHandle());
    }

    public String getContract() {
        return rlmLicenseContract(nativeHandle());
    }

    public int getDetachedDemo() {
        return rlmLicenseDetachedDemo(nativeHandle());
    }

    public String getCustomer() {
        return rlmLicenseCustomer(nativeHandle());
    }

    public int getCount() {
        return rlmLicenseCount(nativeHandle());
    }

    public String getExpiration() {
        return rlmLicenseExp(nativeHandle());
    }

    public String getExpTime() {
        return rlmLicenseExpTime(nativeHandle());
    }

    public int getHold() {
        return rlmLicenseHold(nativeHandle());
    }

    public int hostBased() {
        return rlmLicenseHostBased(nativeHandle());
    }

    public String getHostID() {
        return rlmLicenseHostid(nativeHandle());
    }

    public String getIssued() {
        return rlmLicenseIssued(nativeHandle());
    }

    public boolean isMetered() {
        int metered = rlmLicenseIsMetered(nativeHandle());
        return metered != 0;
    }

    public String getIssuer() {
        return rlmLicenseIssuer(nativeHandle());
    }

    public boolean isCached() {
        int cached = rlmLicenseIsCached(nativeHandle());
        return cached != 0;
    }

    public String getLineItem() {
        return rlmLicenseLineItem(nativeHandle());
    }

    public int getMaxRoam() {
        return rlmLicenseMaxRoam(nativeHandle());
    }

    public int getMaxRoamCount() {
        return rlmLicenseMaxRoamCount(nativeHandle());
    }

    public int getMeterCounter() {
        return rlmLicenseMeterCounter(nativeHandle());
    }

    public int getMinRemove() {
        return rlmLicenseMinRemove(nativeHandle());
    }

    public int getMinTimeout() {
        return rlmLicenseMinTimeout(nativeHandle());
    }

    public String getPlatforms() {
        return rlmLicensePlatforms(nativeHandle());
    }

    public String getProduct() {
        return this.requestedProduct;
    }

    public String getServer() {
        return rlmLicenseServer(nativeHandle());
    }

    public int getShare() {
        return rlmLicenseShare(nativeHandle());
    }

    public int getSoftLimit() {
        return rlmLicenseSoftLimit(nativeHandle());
    }

    public String getStart() {
        return rlmLicenseStart(nativeHandle());
    }

    public int getType() {
        return rlmLicenseType(nativeHandle());
    }

    public String getTeamsURL() {
        return rlmLicenseTeams(nativeHandle());
    }

    public int getTimezone() {
        return rlmLicenseTimezone(nativeHandle());
    }

    public int userBased() {
        return rlmLicenseUserBased(nativeHandle());
    }

    public String getVersion() {
        return rlmLicenseVersion(nativeHandle());
    }

    public String getLFPath() {
        return rlmLicenseGetLFPath(nativeHandle());
    }

    public int daysToExpiration() {
        return this.valid ? 9999 : 0;
    }

    public boolean isRoaming() {
        boolean roaming = false;
        if (rlmLicenseRoaming(nativeHandle()) != 0) {
            roaming = true;
        }
        return roaming;
    }

    public int getMaxShare() {
        return rlmLicenseMaxShare(nativeHandle());
    }

    public int getNamedUserCount() {
        return rlmLicenseNamedUserCount(nativeHandle());
    }

    public int getNamedUserMinHours() {
        return rlmLicenseNamedUserMinHours(nativeHandle());
    }

    public String getOptions() {
        return rlmLicenseOptions(nativeHandle());
    }

    public int getMinCheckout() {
        return rlmLicenseMinCheckout(nativeHandle());
    }

    public boolean authCheck(String license) throws RlmException {
        System.out.println("[Love Live2D] Bypass license authentication....");
        return true;

        /*
        * ORIGINAL CODE
        */
        // int stat = rlmAuthCheck(nativeHandle(), license);
        // if (stat != 0) {
        //     throw new RlmException(stat);
        // }
        // return true;
    }

    public boolean isSingle() {
        boolean single = false;
        if (rlmLicenseSingle(nativeHandle()) != 0) {
            single = true;
        }
        return single;
    }

    public boolean isUncounted() {
        boolean uncounted = false;
        if (rlmLicenseUncounted(nativeHandle()) != 0) {
            uncounted = true;
        }
        return uncounted;
    }
}
