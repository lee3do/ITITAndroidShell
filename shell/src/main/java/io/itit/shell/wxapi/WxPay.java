package io.itit.shell.wxapi;

/**
 * Created by Lee_3do on 16/7/23.
 */

public class WxPay  {

    /**
     * retCode : 0
     * req : {"appid":"wxd370997aa5c4f2b1","noncestr":"456733fc78294a58a900e1ab5b281723",
     * "packageStr":"Sign=WXPay","partnerid":"1284157601",
     * "prepayid":"wx20160723220751e58f9076b00690352285",
     * "sign":"8CF484AE160F54CF684F0F354227D475","timestamp":"1469282871"}
     */

    private int retCode;
    /**
     * appid : wxd370997aa5c4f2b1
     * noncestr : 456733fc78294a58a900e1ab5b281723
     * packageStr : Sign=WXPay
     * partnerid : 1284157601
     * prepayid : wx20160723220751e58f9076b00690352285
     * sign : 8CF484AE160F54CF684F0F354227D475
     * timestamp : 1469282871
     */

    private ReqBean req;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public ReqBean getReq() {
        return req;
    }

    public void setReq(ReqBean req) {
        this.req = req;
    }

    public static class ReqBean {
        private String appid;
        private String noncestr;
        private String packageStr;
        private String partnerid;
        private String prepayid;
        private String sign;
        private String timestamp;

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getNoncestr() {
            return noncestr;
        }

        public void setNoncestr(String noncestr) {
            this.noncestr = noncestr;
        }

        public String getPackageStr() {
            return packageStr;
        }

        public void setPackageStr(String packageStr) {
            this.packageStr = packageStr;
        }

        public String getPartnerid() {
            return partnerid;
        }

        public void setPartnerid(String partnerid) {
            this.partnerid = partnerid;
        }

        public String getPrepayid() {
            return prepayid;
        }

        public void setPrepayid(String prepayid) {
            this.prepayid = prepayid;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}
