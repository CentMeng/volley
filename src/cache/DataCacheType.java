package cache;

/**
 * @author CentMeng csdn@vip.163.com on 16/3/14.
 */
public enum DataCacheType {

    NO_CACHE,
    /**
     * 使用临时缓存
     */
    TEMP_CACHE,
    CACHE,
    //进入页面使用老数据，请求回来的数据进行更改，下回进入页面使用上次请求回来的数据
    USE_OLD_CACHE;

}
