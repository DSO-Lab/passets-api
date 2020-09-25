package com.defvul.passets.api.util;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPUtils {

    public static final String REGEX_SIMPLE_AREA_IPS = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])-(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";

    public static final String REGEX_AREA_IPS = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])-(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";

    public static final String REGEX_MASK_IPS = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\/([1-2]?[0-9]|3[0-2])$";

    public static final String REGEX_IP = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";

    public static final String REGEX_WILDCARD_IP = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\*)$";

    public static final String REGEX_PORT = "^(0|[1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]{1}|6553[0-5])$";

    public static final String REGEX_PORTS = "(?<!-)\\b\\d+-\\d+\\b(?!-)";

    /**
     * 私有IP：
     * A类 10.0.0.0-10.255.255.255
     * B类 172.16.0.0-172.31.255.255
     * C类 192.168.0.0-192.168.255.255
     * 保留地址 169.254.0.0-169.254.255.255
     * 当然，还有127这个网段是环回地址
     **/
    public static int isInnerIP(String ipAddress) {
        boolean isInnerIp;
        long ipNum = getIpNum(ipAddress);
        long aBegin = getIpNum("10.0.0.0");
        long aEnd = getIpNum("10.255.255.255");
        long bBegin = getIpNum("172.16.0.0");
        long bEnd = getIpNum("172.31.255.255");
        long cBegin = getIpNum("192.168.0.0");
        long cEnd = getIpNum("192.168.255.255");
        long nBegin = getIpNum("169.254.0.0");
        long nEnd = getIpNum("169.254.255.255");
        isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd)
                || isInner(ipNum, nBegin, nEnd) ||ipAddress.equals("127.0.0.1");
        return isInnerIp ? 1 : 0;
    }

    private static long getIpNum(String ipAddress) {
        String[] ip = ipAddress.split("\\.");
        long a = Integer.parseInt(ip[0]);
        long b = Integer.parseInt(ip[1]);
        long c = Integer.parseInt(ip[2]);
        long d = Integer.parseInt(ip[3]);
        return a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
    }

    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }

	/*
    public static long getIP(InetAddress ip) {
		byte[] b = ip.getAddress();
		long l = b[0] << 24L & 0xff000000L | b[1] << 16L & 0xff0000L | b[2] << 8L & 0xff00L | b[3] << 0L & 0xffL;
		return l;
	}
	public static InetAddress toIP(long ip) throws UnknownHostException {
		byte[] b = new byte[4];
		int i = (int) ip;// 低３２位
		b[0] = (byte) ((i >> 24) & 0x000000ff);
		b[1] = (byte) ((i >> 16) & 0x000000ff);
		b[2] = (byte) ((i >> 8) & 0x000000ff);
		b[3] = (byte) ((i >> 0) & 0x000000ff);
		return InetAddress.getByAddress(b);
	}
	 */

    public static long toLong(String ip) {
        long l = 0L;
        String[] ips = ip.split("\\.");
        for (String is : ips) {
            int i = Integer.valueOf(is);
            l <<= 8;
            l += i;
        }
        return l >>> 0;
    }

    public static long getLen(String ipfrom, String ipto) {
        long from = toLong(ipfrom);
        long to = toLong(ipto);
        return to - from;
    }

    public static List<String> getIPArr(String ipfrom, String ipto) {
        List<String> ips = new ArrayList<String>();
        if (getLen(ipfrom, ipto) > 7000) {
            return ips;
        }
        String[] ipfromd = ipfrom.split("\\.");
        String[] iptod = ipto.split("\\.");
        int[] int_ipf = new int[4];
        int[] int_ipt = new int[4];
        for (int i = 0; i < 4; i++) {
            int_ipf[i] = Integer.parseInt(ipfromd[i]);
            int_ipt[i] = Integer.parseInt(iptod[i]);
        }
        for (int A = int_ipf[0]; A <= int_ipt[0]; A++) {
            for (int B = (A == int_ipf[0] ? int_ipf[1] : 0); B <= (A == int_ipt[0] ? int_ipt[1]
                    : 255); B++) {
                for (int C = (B == int_ipf[1] ? int_ipf[2] : 0); C <= (B == int_ipt[1] ? int_ipt[2]
                        : 255); C++) {
                    for (int D = (C == int_ipf[2] ? int_ipf[3] : 0); D <= (C == int_ipt[2] ? int_ipt[3]
                            : 255); D++) {
                        ips.add(new String(A + "." + B + "." + C + "." + D));
                    }
                }
            }
        }
        return ips;
    }

    public static List<String> getIPStar(String ipStar) {
        List<String> ips = new ArrayList<String>();
        String strStart = ipStar.substring(0, ipStar.lastIndexOf(".")) + ".";
        for (int i = 1; i <= 254; i++) {
            ips.add(strStart + i);
        }
        return ips;
    }

    public static List<String> getIPList(String IPvalue) {
        List<String> ipList = new ArrayList<>();
        if (IPvalue.contains(",")) {
            String[] ips = IPvalue.split(",");
            for (String ipStr : ips) {
                ipStr = ipStr.trim();
                if (ipStr.isEmpty()) {
                    continue;
                }
                if (ipStr.contains("-")) {
                    String[] ipSub = ipStr.split("-");
                    if (ipSub[1].length() < 4) {
                        ipSub[1] = ipSub[0].substring(0, ipSub[0].lastIndexOf(".")) + "." + ipSub[1];
                    }
                    ipList.addAll(getIPArr(ipSub[0], ipSub[1]));
                } else if (ipStr.contains("/")) {
                    String strStart = ipStr.substring(0, ipStr.indexOf("/"));
                    String mask = ipStr.substring(ipStr.indexOf("/") + 1);
                    ipList.addAll(IpMaskUtils.parseIpMaskRange(strStart, mask));
                } else if (ipStr.contains("*")) {
                    ipList.addAll(getIPStar(ipStr));
                } else {
                    ipList.add(ipStr);
                }
            }
        } else if (IPvalue.contains("-")) {
            String[] ipSub = IPvalue.split("-");
            if (ipSub[1].length() < 4) {
                ipSub[1] = ipSub[0].substring(0, ipSub[0].lastIndexOf(".")) + "." + ipSub[1];
            }
            ipList.addAll(getIPArr(ipSub[0], ipSub[1]));
        } else if (IPvalue.contains("/")) {
            String strStart = IPvalue.substring(0, IPvalue.indexOf("/"));
            String strEnd = IPvalue.substring(IPvalue.indexOf("/") + 1);
            ipList.addAll(IpMaskUtils.parseIpMaskRange(strStart, strEnd));
        } else if (IPvalue.contains("*")) {
            ipList.addAll(getIPStar(IPvalue));
        } else {
            ipList.add(IPvalue);
        }
        return ipList;
    }

    public static Set<String> getIpList(String ipStr) {
        return getIpList(ipStr, false);
    }

    /**
     * 支持所有类型的获取IP列表
     *
     * @param ipStr
     * @return
     */
    public static Set<String> getIpList(String ipStr, boolean once) {
        Set<String> ips = new HashSet<>();
        if (StringUtils.isEmpty(ipStr)) {
            return ips;
        }
        ipStr = ipStr.trim();
        // ip
        if (isIp(ipStr)) {
            ips.add(ipStr);
            return ips;
        }

        // 简易IP段
        if (isSimpleAreaIps(ipStr)) {
            ips.addAll(getIpListBySimpleArea(ipStr));
            return ips;
        }

        // ip段
        if (isAreaIps(ipStr)) {
            ips.addAll(getIpListByArea(ipStr));
            return ips;
        }

        // ip掩码
        if (isMaskIps(ipStr)) {
            ips.addAll(getIpListByMask(ipStr));
            return ips;
        }

        if (once) {
            ips.add(ipStr);
            return ips;
        }

        String[] iparr = ipStr.split("[\\s|,|、]");
        if (iparr.length > 0) {
            if (iparr.length == 1) once = true;
            for (String ip : iparr) {
                Set<String> s = getIpList(ip, once);
                ips.addAll(s);
            }
        }

        return ips;
    }

    /**
     * 获取简易IP段的ip列表
     *
     * @param ipStr
     * @return
     */
    public static List<String> getIpListBySimpleArea(String ipStr) {
        String[] ipArr = ipStr.split("-");
        String first = ipArr[0];
        String last = first.substring(0, first.lastIndexOf(".") + 1) + ipArr[1];
        return getIPArr(first, last);
    }

    /**
     * 获取IP段的IP列表
     *
     * @param ipStr
     * @return
     */
    public static List<String> getIpListByArea(String ipStr) {
        String[] ipArr = ipStr.split("-");
        return getIPArr(ipArr[0], ipArr[1]);
    }

    /**
     * 获取IP掩码的IP列表
     *
     * @param ipStr
     * @return
     */
    public static List<String> getIpListByMask(String ipStr) {
        String[] ipArr = ipStr.split("/");
        return IpMaskUtils.parseIpMaskRange(ipArr[0], ipArr[1]);
    }

    public static List<String> getIpListByWildcard(String ipStr) {
        ipStr = ipStr.replace('*', '/').concat("24");
        return getIpListByMask(ipStr);
    }

    /**
     * 是否内网IP
     *
     * @param ipStr
     * @return
     */
    public static boolean isInnerIp(String ipStr) {
        return isIp(ipStr) && isInnerIP(ipStr) == 1;
    }

    /**
     * 是否外网IP
     *
     * @param ipStr
     * @return
     */
    public static boolean isOuterIp(String ipStr) {
        return isIp(ipStr) && !isInnerIp(ipStr);
    }

    /**
     * 是否子网掩码IP段
     *
     * @param ipStr
     * @return
     */
    public static boolean isMaskIps(String ipStr) {
        return matcher(ipStr, REGEX_MASK_IPS);
    }

    public static boolean isWildcardIps(String ipStr) {
        return matcher(ipStr, REGEX_WILDCARD_IP);
    }

    /**
     * 是否ip段 192.168.1.1-192.168.1.224
     *
     * @param ipStr
     * @return
     */
    public static boolean isAreaIps(String ipStr) {
        return matcher(ipStr, REGEX_AREA_IPS);
    }

    /**
     * 是否简易IP段 192.168.1.1-192
     *
     * @param ipStr
     * @return
     */
    public static boolean isSimpleAreaIps(String ipStr) {
        return matcher(ipStr, REGEX_SIMPLE_AREA_IPS);
    }

    /**
     * 是否内网子网掩码IP段
     *
     * @param ipStr
     * @return
     */
    public static boolean isMaskInnerIps(String ipStr) {
        if (isMaskIps(ipStr)) {
            String[] ipArr = ipStr.split("/");
            return isInnerIp(ipArr[0]);
        }
        return false;
    }

    /**
     * 是否内网IP段
     *
     * @param ipStr
     * @return
     */
    public static boolean isAreaInnerIps(String ipStr) {
        if (isAreaIps(ipStr)) {
            String[] ipArr = ipStr.split("-");
            return isInnerIp(ipArr[0]) && isInnerIp(ipArr[1]);
        }
        return false;
    }

    /**
     * 是否内网简易Ip段
     *
     * @param ipStr
     * @return
     */
    public static boolean isSimpleAreaInnerIps(String ipStr) {
        if (isSimpleAreaIps(ipStr)) {
            String[] ipArr = ipStr.split("-");
            String first = ipArr[0];
            String last = first.substring(0, first.lastIndexOf(".") + 1) + ipArr[1];
            return isInnerIp(first) && isInnerIp(last);
        }
        return false;
    }

    public static boolean contains(String ips, String ip) {
        Set<String> is = getIpList(ips);
        return is.contains(ip);
    }

    /**
     * 是否IP
     *
     * @param ipStr
     * @return
     */
    public static boolean isIp(String ipStr) {
        return matcher(ipStr, REGEX_IP);
    }

//    public static boolean regexURL(String ipStr) {
////        final String regex = "^((https|http|ftp|rtsp|mms)?:\\/\\/)?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?(([0-9]{1,3}\\.){3}[0-9]{1,3}|([0-9a-z_!~*'()-]+\\.)*([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\.[a-z]{2,6})(:[0-9]{2,5})?((\\/?)|(\\/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+\\/?)$";
////
////        final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
////        final Matcher matcher = pattern.matcher(ipStr);
////        return matcher.find();
//        ValidatorOption option = new ValidatorOption();
//        option.setRequireValidProtocol(true);
//        return Validators.isURL(ipStr);
//    }

    private static boolean matcher(String value, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.find();
    }

    public static String removeLastSlash(String url) {
        if (url == null || url.trim().length() == 0) {
            return null;
        }
        url = url.trim();
        if (url.charAt(url.length() - 1) == '/') {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static void main(String[] args) {

//        System.out.println(IPUtils.getIPList("192.168.10.32/28"));
        System.out.println(IPUtils.toLong("1.1.1.1"));
        System.out.println(IPUtils.toLong("1.1.1.2"));
        System.out.println(IPUtils.getLen("1.1.1.1", "255.255.255.255"));
    }

    public static boolean isPort(String portStr) {
        return matcher(portStr, REGEX_PORT);
    }

    public static Boolean isPorts(String ports) {
        return matcher(ports, REGEX_PORTS);
    }
}
