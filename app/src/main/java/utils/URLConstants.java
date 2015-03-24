package utils;

/**
 * 与服务器建立连接的各种URL
 */
public class URLConstants {

    /**
     * 服务器IP
     */
    public static final String SERVER_IP = "http://comet.chinacloudsites.cn";


    public static final String LOGIN = SERVER_IP + "/auth/login";
    public static final String REGISTER = SERVER_IP + "/auth/register";

    public static final String SUBMIT_ROUTE = SERVER_IP + "/users/profile/routes";

    /**
     * 得到用户信息
     */
    public static final String USER_INFO = SERVER_IP + "/users/profile";


    // 聚合数据网 api constant
//    public static final String SCENERY_LIST = "http://web.juhe.cn:8080/travel/scenery/sceneryList.json" + "?key=" + APIConstants.APPKEY + "&pname=" + APIConstants.PACKAGE_NAME + "&v=1";

    /**
     * routemaker API constants
     */
    public static final String USER_COLECTION_SPOT = SERVER_IP + "/users/profile/collections";

    public static final String SPOT_VISIT_TIME = SERVER_IP + "/spots/profile";

    public static final String ROUTE_SUBMIT = SERVER_IP + "/users/profile/routes";


}
