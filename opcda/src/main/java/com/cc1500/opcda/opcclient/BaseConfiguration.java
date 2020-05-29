//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.cc1500.opcda.opcclient;

import org.openscada.opc.lib.common.ConnectionInformation;

import java.io.IOException;
import java.util.Properties;

/**
 *获取opcserver 信息的工具类
 * @author cc1500
 * @date 2019/8/13
 */
public final class BaseConfiguration {
    private static final ConnectionInformation ci = new ConnectionInformation();
    private static final Properties prop = new Properties();
    public static final String CONFIG_USERNAME = "username";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_DOMAIN = "domain";
    public static final String CONFIG_CLSID = "clsid";
    public static final String CONFIG_PROGID = "progid";
    private static final String CONFIG_FILE_NAME = "config.properties";

    public BaseConfiguration() {
    }

    public static String getEntryValue(String name) {
        return prop.getProperty(name);
    }

    public static ConnectionInformation getCLSIDConnectionInfomation() {
        ci.setProgId((String)null);
        getConnectionInfomation();
        ci.setClsid(prop.getProperty("clsid"));
        return ci;
    }

    public static ConnectionInformation getPROGIDConnectionInfomation() {
        ci.setClsid((String)null);
        getConnectionInfomation();
        ci.setProgId(prop.getProperty("progid"));
        return ci;
    }

    private static void getConnectionInfomation() {
        ci.setHost(prop.getProperty("host"));
        ci.setDomain(prop.getProperty("domain"));
        ci.setUser(prop.getProperty("username"));
        ci.setPassword(prop.getProperty("password"));
    }

    static {
        try {
            prop.load(BaseConfiguration.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException var1) {
            var1.printStackTrace();
        }

    }
}
