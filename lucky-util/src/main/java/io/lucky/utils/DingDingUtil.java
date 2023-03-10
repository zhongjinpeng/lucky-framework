//package io.lucky.utils;
//
//import org.apache.commons.codec.binary.Base64;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.util.List;
//
//public class DingDingUtil {
//    //发送超时时间10s
//    private static final int TIME_OUT = 10000;
//
//    /**
//     * 钉钉机器人文档地址https://ding-doc.dingtalk.com/doc#/serverapi2/qf2nxq
//     *
//     * @param webhook
//     * @param secret     安全设置 3选1【方式一，自定义关键词 】 【方式二，加签 ，创建机器人时选择加签 secret以SE开头】【方式三，IP地址（段）】
//     * @param content    发送内容
//     * @param mobileList 通知具体人的手机号码列表 （可选）
//     * @return
//     */
//    public static String sendMsg(String webhook, String secret, String content, List<String> mobileList) {
//        try {
//            //钉钉机器人地址（配置机器人的webhook）
//            if (null != secret) {
//                Long timestamp = System.currentTimeMillis();
//                String sign = getSign(timestamp, secret);
//                webhook = new StringBuilder(webhook)
//                        .append("&timestamp=")
//                        .append(timestamp)
//                        .append("&sign=")
//                        .append(sign)
//                        .toString();
//            }
//            //是否通知所有人
//            boolean isAtAll = false;
//            //组装请求内容
//            String reqStr = buildReqStr(content, isAtAll, mobileList);
//            //推送消息（http请求）
//            String result = postJson(webhook, reqStr);
//            return result;
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    /**
//     * 组装请求报文
//     * 发送消息类型 text
//     *
//     * @param content
//     * @return
//     */
//    private static String buildReqStr(String content, boolean isAtAll, List<String> mobileList) {
//        DingDingSendMsgDto dingDingMsg = new DingDingSendMsgDto();
//        DingDingMsgContentDto markDown = new DingDingMsgContentDto();
//        markDown.setTitle("巡检报告");
//        markDown.setText(content);
//        dingDingMsg.setMarkdown(markDown);
//        dingDingMsg.setMsgtype("markdown");
//        JSONObject json = (JSONObject) JSON.toJSON(dingDingMsg);
//        return json.toJSONString();
//    }
//
//
//    private static String postJson(String url, String reqStr) {
//        String body = null;
//        try {
//            body = HttpRequest.post(url).body(reqStr).timeout(TIME_OUT).execute().body();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return body;
//    }
//
//    /**
//     * 自定义机器人获取签名
//     * 创建机器人时选择加签获取secret以SE开头
//     *
//     * @param timestamp
//     * @return
//     * @throws NoSuchAlgorithmException
//     * @throws UnsupportedEncodingException
//     * @throws InvalidKeyException
//     */
//    private static String getSign(Long timestamp, String secret) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
//        String stringToSign = timestamp + "\n" + secret;
//        Mac mac = Mac.getInstance("HmacSHA256");
//        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
//        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
//        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
//        return sign;
//    }
//
//
//    public static void main(String[] args) {
//        String webhook = "https://oapi.dingtalk.com/robot/send?access_token=4ed60efb06e3ccc4028a03fbe1fac36895ee79ba0360ed7c3ee844703103fe4a";
//        String secret = "SECc513a16c161d7a80d6aa25329905fe8f42f107b351b45401875492b92d44fb16";
//        DingDingUtil.sendMsg(webhook, secret, "123", null);
//    }
//}