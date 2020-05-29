package com.cc1500.opcda.opcclient;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class CommondUtils {
  /**
     * 传入需要连接的IP，返回是否连接成功
     * @param remoteInetAddr
     * @return
     */
    public static boolean isReachable(String remoteInetAddr) {
        boolean reachable = false;
        try {
            InetAddress address = InetAddress.getByName(remoteInetAddr);
            reachable = address.isReachable(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reachable;
    }

    /**
     * import java.lang.reflect.Field;
     * 判断对象是否为空，且对象的所有属性都为空
     * ps: boolean类型会有默认值false 判断结果不会为null 会影响判断结果
     *     序列化的默认值也会影响判断结果
     * @param object
     * @return
     */

    public static boolean objCheckIsNull(Object object){
        Class clazz = (Class)object.getClass();
        // 得到类对象
        Field fields[] = clazz.getDeclaredFields();
        // 得到所有属性
        boolean flag = true;
        //定义返回结果，默认为true
        for(Field field : fields){
            field.setAccessible(true);
            Object fieldValue = null;
            try {
                fieldValue = field.get(object);
                //得到属性值
                Type fieldType =field.getGenericType();
                //得到属性类型
                String fieldName = field.getName();
                // 得到属性名
                //  System.out.println("属性类型："+fieldType+",属性名："+fieldName+",属性值："+fieldValue);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if(fieldValue != null){
                //只要有一个属性值不为null 就返回false 表示对象不为null
                flag = false;
                break;
            }
        }
        return flag;
    }
    /**
     * 按分隔符截取字符串，返回一个list
     * **/
    public static List<String> mySplit(String str, String delim){
        List<String> stringList = new ArrayList<>();
        while(true) {
            int k = str.indexOf(delim);
            if (k < 0){
                stringList.add(str);
                break;
            }
            String s = str.substring(0, k);
            stringList.add(s);
            str = str.substring(k+1);
        }
        return stringList;
    }


    public static String CurrentTimeyyyyMMddHHmmss(String DateFormat){
        //"yyyy-MM-dd HH:mm ss"
    SimpleDateFormat sdFormatter = new SimpleDateFormat( DateFormat );
     return sdFormatter.format( new Date( System.currentTimeMillis() ) );
    }
    /**
     * 按分隔符截取字符串，返回一个list
     * **/
    public static String [] MapKeyTolist(HashMap map) {
        String[] Liststring = new String[map.size()];
        int icon = 0;
        for (Object key : map.keySet()) {
            Liststring[icon] = key.toString();
            icon++;
        }
        return  Liststring;
    }
    public static List<String> MapKeyTolist1(HashMap map) {
        List<String> Liststring = new ArrayList<>(map.size());
        int icon = 0;
        for (Object key : map.keySet()) {
            Liststring.add( key.toString());
            icon++;
        }
        return  Liststring;
    }



}
