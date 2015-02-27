package utils;

/**
 * 与服务器建立连接的各种URL
 */
public class URLConstants {

    /**
     * 服务器IP
     */
    public static final String SERVER_IP = "http://comet-01.chinacloudsites.cn";


    public static final String LOGIN = SERVER_IP + "/auth/login";
    public static final String REGISTER = SERVER_IP + "/auth/register";

    /**
     * 得到用户信息
     */
    public static final String USER_INFO = SERVER_IP + "/users/profile";


    // 聚合数据网 api constant
    public static final String SCENERY_LIST = "http://web.juhe.cn:8080/travel/scenery/sceneryList.json" + "?key=" + APIConstants.APPKEY + "&pname=" + APIConstants.PACKAGE_NAME + "&v=1";
}
