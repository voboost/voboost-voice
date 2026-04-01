package com.qinggan.canbus.plugs;

import java.util.HashMap;
import java.util.Map;

public interface IXmPayOrderListener {
    public static final int CODE_CANCLE = 2;
    public static final int CODE_NO_LOGIN = 4;
    public static final int CODE_PAY_ALBUM_IS_NOT_EXISTED = 614;
    public static final int CODE_PAY_ALREADY_EXIST_UNPAID_ORDER = 603;
    public static final int CODE_PAY_APP_DISABLE_PAY = 609;
    public static final int CODE_PAY_CHANNEL_FAIL = 433;
    public static final int CODE_PAY_CHECK_FAIL = 100;
    public static final int CODE_PAY_CONTENT_OFF = 600;
    public static final int CODE_PAY_H5_ERROR = 500;
    public static final int CODE_PAY_H5_SUCCESS = 200;
    public static final int CODE_PAY_INFO = 1015;
    public static final int CODE_PAY_IN_HANDLE = 430;
    public static final int CODE_PAY_IS_FREE_UNSUPPORTED_BUY = 619;
    public static final int CODE_PAY_IS_NOT_EXIST = 621;
    public static final int CODE_PAY_JSONERROR = 102;
    public static final int CODE_PAY_LOGIN_NEED = 1014;
    public static final int CODE_PAY_NETERROR = 101;
    public static final int CODE_PAY_ORDER_HAS_CONFIRMED = 615;
    public static final int CODE_PAY_ORDER_HAS_PAY = 618;
    public static final int CODE_PAY_PRICE_INCORRECT = 602;
    public static final int CODE_PAY_SERIAL_FAIL = 432;
    public static final int CODE_PAY_SINGLE_TRACK = 620;
    public static final int CODE_PAY_TIMEOUT = 401;
    public static final int CODE_PAY_TRACKS_FROM_DIFFERENT_ALBUM = 601;
    public static final int CODE_PAY_TYPE_NOT_SUPPORTED = 3;
    public static final int CODE_PAY_UNKNOW_ERROR = 110;
    public static final int CODE_PAY_ZFB_FAIL = 431;
    public static final int CODE_REAL_IP_NOT_INCLUDE_IN_WHITE_LIST = 610;
    public static final Map<Integer, String> ERROR_MAPS = new HashMap<Integer, String>() { // from class: com.ximalaya.ting.android.xmpayordersdk.IXmPayOrderListener.1
        {
            put(2, "取消购买");
            put(4, "用户未登录");
            put(110, "未知错误");
            put(101, "网络错误");
            put(102, "下单接口 json提取失败");
            put(200, "下单成功");
            put(500, "下单失败");
            put(100, "参数常规校验失败");
            put(600, "待购买内容已下架");
            put(601, "待购买声音来自不同专辑");
            put(Integer.valueOf(IXmPayOrderListener.CODE_PAY_PRICE_INCORRECT), "单价校验失败");
            put(Integer.valueOf(IXmPayOrderListener.CODE_PAY_ALREADY_EXIST_UNPAID_ORDER), "已存在未支付订单包含重叠付费音频");
            put(3, "购买类型不支持");
            put(Integer.valueOf(IXmPayOrderListener.CODE_PAY_APP_DISABLE_PAY), "应用没有付费音频接入资格");
            put(Integer.valueOf(IXmPayOrderListener.CODE_PAY_ORDER_HAS_CONFIRMED), "已经购买");
            put(1014, "需要登录");
            put(Integer.valueOf(IXmPayOrderListener.CODE_REAL_IP_NOT_INCLUDE_IN_WHITE_LIST), "应用IP不在白名单中");
            put(Integer.valueOf(IXmPayOrderListener.CODE_PAY_ALBUM_IS_NOT_EXISTED), "查询价格信息的专辑不存在");
            put(1015, "获取价格信息出错");
            put(401, "订单超时");
            put(430, "订单处理中");
            put(431, "支付宝支付失败");
            put(432, "签名错误");
            put(433, "支付渠道错误");
            put(Integer.valueOf(IXmPayOrderListener.CODE_PAY_ORDER_HAS_PAY), "已经购买");
            put(Integer.valueOf(IXmPayOrderListener.CODE_PAY_IS_FREE_UNSUPPORTED_BUY), "声音资源是免费试听的，无需购买");
            put(Integer.valueOf(IXmPayOrderListener.CODE_PAY_SINGLE_TRACK), "购买的资源是单集购买类型，不能当做整张类型购买");
            put(Integer.valueOf(IXmPayOrderListener.CODE_PAY_IS_NOT_EXIST), "购买的商品不存在");
        }
    };
}
