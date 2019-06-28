package zeng.fanda.com.library;

import android.app.Activity;

import java.lang.reflect.Method;

/**
 * 注解管理器
 *
 * @author 曾凡达
 * @date 2019/6/28
 */
public class Butterknife {

    public static void bind(Activity activity) {
        String className = activity.getClass().getName() + "$ViewBinder";
        try {
            Class<?> viewBinderClass = Class.forName(className);
            Method binder =  viewBinderClass.getMethod("bindView",activity.getClass());
            binder.invoke(viewBinderClass.newInstance(), activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
