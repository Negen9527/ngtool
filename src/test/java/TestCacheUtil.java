import com.negen.CacheHandleSchedule;
import com.negen.CacheUtil;

public class TestCacheUtil {
    public static void main(String[] args) {
        CacheHandleSchedule cacheHandleSchedule = new CacheHandleSchedule();
        cacheHandleSchedule.start();
        CacheUtil cacheUtil = CacheUtil.getInstance();
        cacheUtil.put("zhangsan", "zhangsan", 100);
        cacheUtil.put("lisi", "lisi", 1200);
        cacheUtil.put("w5", "w5", 1300);
        Object zhangsan = cacheUtil.get("zhangsan");
        if (null != zhangsan)
            System.out.printf((String)zhangsan);
    }
}
