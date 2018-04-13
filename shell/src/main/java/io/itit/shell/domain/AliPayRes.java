package io.itit.shell.domain;

public class AliPayRes {

    /**
     * alipay_trade_app_pay_response : {"code":"10000"}
     */

    public AlipayTradeAppPayResponseBean alipay_trade_app_pay_response;

    public static class AlipayTradeAppPayResponseBean {
        /**
         * code : 10000
         */

        public String code;
    }
}
