package utils;

/**
 * 与服务器建立连接的各种URL
 */
public class URLConstants {

    /**
     * 服务器IP
     */
    public static final String SERVER_IP = "http://123.57.142.203:9000";

    /**
     * 获取Info列表
     * GET方法
     */
    public static final String FETCH_INFO_LIST = SERVER_IP + "/ground/fetch";

    /**
     * post Info
     * POST方法
     */
    public static final String POST_INFO = SERVER_IP + "/ground/post";

    /**
     * 评论info
     * POST方法
     */
    public static final String POST_INFO_COMMENT = SERVER_IP + "/ground/comment";

    /**
     * 上传文件
     * POST方法
     */
    public static final String UPLOAD_FILE = SERVER_IP + "/upload";

    public static final String LOGIN = SERVER_IP + "/user/login";
    public static final String CHECK_IS_OTHER_PLATFORMS_REGISTERED = SERVER_IP + "/user/check";
    public static final String REGISTER = SERVER_IP + "/user/register";
    public static final String USER_INFO = SERVER_IP + "/user/info";
}
