package kr.mmv.mjusugangsincheonghelper.global.api.support;

import kr.mmv.mjusugangsincheonghelper.global.api.meta.ClientInfo;

/**
 * ThreadLocal 기반 메타데이터 저장소
 */
public class CustomResponseMetaContextHolder {
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> API_VERSION = new ThreadLocal<>();
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();
    private static final ThreadLocal<ClientInfo> CLIENT_INFO = new ThreadLocal<>();

    public static void setRequestId(String id) {
        REQUEST_ID.set(id);
    }

    public static String getRequestId() {
        return REQUEST_ID.get();
    }

    public static void setApiVersion(String version) {
        API_VERSION.set(version);
    }

    public static String getApiVersion() {
        return API_VERSION.get();
    }

    public static void setStartTime(Long time) {
        START_TIME.set(time);
    }

    public static Long getStartTime() {
        return START_TIME.get();
    }

    public static void setClientInfo(ClientInfo clientInfo) {
        CLIENT_INFO.set(clientInfo);
    }

    public static ClientInfo getClientInfo() {
        return CLIENT_INFO.get();
    }

    /**
     * 메모리 누수 방지를 위해 반드시 호출
     */
    public static void clear() {
        REQUEST_ID.remove();
        API_VERSION.remove();
        START_TIME.remove();
        CLIENT_INFO.remove();
    }
}
