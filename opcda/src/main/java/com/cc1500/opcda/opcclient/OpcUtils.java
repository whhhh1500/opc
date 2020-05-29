package com.cc1500.opcda.opcclient;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.*;
import org.openscada.opc.dcom.common.KeyedResult;
import org.openscada.opc.dcom.common.KeyedResultSet;
import org.openscada.opc.dcom.da.OPCBROWSEDIRECTION;
import org.openscada.opc.dcom.da.OPCBROWSETYPE;
import org.openscada.opc.dcom.da.PropertyDescription;
import org.openscada.opc.dcom.da.impl.OPCBrowseServerAddressSpace;
import org.openscada.opc.dcom.da.impl.OPCItemProperties;
import org.openscada.opc.dcom.da.impl.OPCServer;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.ItemState;
import org.openscada.opc.lib.da.Server;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OPC工具类
 * Created by cc1500 on 2019/8/9.
 */

public class OpcUtils {
    private static int reF;
   private static OPCServer Propertiesserver;
    public static List<String> map1 =  new ArrayList<>(  );
    private static OPCItemProperties itemProperties;

    public static  List<String> GetMapByFlo(List<String> ScreenID) throws Exception {
       // Logger.getLogger("org.jinterop").setLevel(Level.OFF);
        long startTime = System.currentTimeMillis();
        JISystem.setAutoRegisteration(true);
        OPCServer server = CreaterOpcServer();
        OPCBrowseServerAddressSpace serverBrowser = server.getBrowser();

        browseFlat1(serverBrowser, ScreenID);
        long endTime = System.currentTimeMillis();
        System.out.println("启动：" + (endTime - startTime) + "ms");
        return map1;

    }





    private static void browseFlat1(OPCBrowseServerAddressSpace browser, List ScreenID) throws JIException, IllegalArgumentException, UnknownHostException {
        System.out.println(String.format("Organization: %s", new Object[]{browser.queryOrganization()}));
        browser.changePosition((String)null, OPCBROWSEDIRECTION.OPC_BROWSE_TO);
        System.out.println("Showing flat address space");
        Iterator var2 = browser.browse(OPCBROWSETYPE.OPC_FLAT, "", 0, 0).asCollection().iterator();

        while(var2.hasNext()) {
            String id = (String)var2.next();

            for(int i = 0; i < ScreenID.size(); ++i) {
                if(id.contains(ScreenID.get( i ).toString())) {
                    map1.add(id);
                }
            }
        }

    }
    public static  HashMap<String, String> GetMap( Iterator var2,String[] ScreenID) {
        HashMap<String, String> map1=null;
        while(var2.hasNext()) {
        String id = (String)var2.next();
        for(int i = 0; i < ScreenID.length; ++i) {
            System.out.println("  map1.put(id, \"\") "+ id);
            if(id.contains(ScreenID[i])) {

                map1.put(id, "");
            }
        }
    }
    return map1;
    }
    /**
     * 获取指定Item的Properties
     *
     * @author Freud
     *
     */
    public static  void   GetPropertiesserver( )throws Exception {

        Logger.getLogger("org.jinterop").setLevel( Level.OFF);
        Propertiesserver=    CreaterOpcServer();
//        OPCGroupStateMgt group = server.addGroup("OPC_ENUM_ALL", true, 1000, 1234, 60,
//                0.0f, 1033);
        itemProperties = Propertiesserver
                .getItemPropertiesService();

    }
    public static HashMap<String, Map>  GetProperties(String... itemids )throws Exception {
            HashMap<String, Map> returnobj=new HashMap<>(  );
        int i=0;
      List<String> map1 =  new ArrayList<>(  );

        OPCServer Propertiesserver=    CreaterOpcServer();
        OPCItemProperties itemProperties = Propertiesserver
                .getItemPropertiesService();
        for(String j:itemids){
            if("".equals( j )||j==null) {
                continue;
            }

          //  System.out.println(i+"  " +j+" "+dateTimeNow());
            returnobj.put( j, dumpItemProperties(itemProperties, j))  ;
            i++;
        }
      return returnobj;
    }
    /**
     * show create OpcServer
     *
     * @param
     * @return Server
     */
    public  static OPCServer CreaterOpcServer ()throws Exception
    {
        JISystem.setAutoRegisteration(true);

        JISession _session = JISession.createSession( BaseConfiguration.getEntryValue("domain"), BaseConfiguration.getEntryValue("username"), BaseConfiguration.getEntryValue("password"));
        JIComServer comServer = new JIComServer( JIClsid.valueOf(BaseConfiguration.getEntryValue("clsid")), BaseConfiguration.getEntryValue("host"), _session);
        IJIComObject serverObject = comServer.createInstance();

        OPCServer server = new OPCServer(serverObject);
        return  server;
    }
    /**
     * show create Server
     * @param
     * @return Server
     */
    public static Server CreaterServer() {
        return new Server(
                BaseConfiguration.getCLSIDConnectionInfomation(),
                Executors.newSingleThreadScheduledExecutor());
    }




    /**
     * show create Sever by BaseConfiguration .
     *
     * @param
     * @return Server
     */
    public static Item write(String tg, String val, Server ser) throws Exception {
        JIVariant value = new JIVariant(val);
        final Item item;
        item = ser.addGroup().addItem(tg);
        reF = item.write(value);
        Thread.sleep(200);
        return item;
    }

    /**
     * 获取value
     *
     * @param
     * @return Server
     */
    public static String readvalue(Item item) throws Exception {
        int typeid;
        ItemState ItemState = item.read(true);
        typeid = ItemState.getValue().getType();
        return    getvalueByOPCUtils(typeid,ItemState);
    }


    public static String getvalueByOPCUtils(int var1, ItemState item) throws Exception {
        String value="" ;

        switch (var1) {
            case 3:
                value = String.valueOf(item.getValue().getObjectAsInt());
                break;
            case 4:
                value = "" + item.getValue().getObjectAsFloat();
                break;
                case 8:
                value = "" + item.getValue().getObjectAsString2();
                break;
            case 11:
                value = "" + item.getValue().getObjectAsBoolean();
                break;
            case 2:
                value = "" + item.getValue().getObjectAsShort();
                break;
//            case 18:
//                value = "" + item.getValue().getObjectAsShort();
//                break;
            default:
                value = item. getValue().toString();
                break;
        }
      //  System.out.println( var1+"   "+ value);
        return value;
    }


    private static HashMap<Integer, String> dumpItemProperties(
            final OPCItemProperties itemProperties, final String itemID) throws JIException {

        final Collection<PropertyDescription> properties = itemProperties
                .queryAvailableProperties(itemID);
        final int[] ids = new int[properties.size()];
//        System.out.println(String.format("Item Properties for '%s' (count:%d)",
//               itemID, properties.size()));
        int i = 0;
        for (final PropertyDescription pd : properties) {
            ids[i] = pd.getId();
            i++;
        }
//        System.out.println("Lookup");
//        dumpItemPropertiesLookup(itemProperties, itemID, ids);
        System.out.println("Query:"+ (reF++) +" "+itemID);
     return  dumpItemProperties2(itemProperties, itemID, ids);
    }

    private static HashMap<Integer, String> dumpItemProperties2(
            final OPCItemProperties itemProperties, final String itemID,
            final int... ids) throws JIException {
        final KeyedResultSet<Integer, JIVariant> values = itemProperties
                .getItemProperties(itemID, ids);
        HashMap<Integer, String> map1 =new HashMap<Integer, String> () ;

        map1.put( 1, values.get( 6 ).getValue().getObjectAsString().getString().toString());
        map1.put( 2, values.get( 8 ).getValue().getObjectAsString().getString().toString());
//            System.out.println(String.format(
//                    "ID: %d, Value: %s, Error Code: %08x", entry.getKey(),
//                    entry.getValue().toString(), entry.getErrorCode()));
            return map1 ;
    }

    private static void dumpItemPropertiesLookup(
            final OPCItemProperties itemProperties, final String itemID,
            final int... ids) throws JIException {
        final KeyedResultSet<Integer, String> values = itemProperties
                .lookupItemIDs( itemID, ids );
        for (final KeyedResult<Integer, String> entry : values) {
            System.out.println( String.format(
                    "ID: %d, Item ID: %s, Error Code: %08x", entry.getKey(),
                    entry.getValue(), entry.getErrorCode() ) );
        }
    }
//    /**
//     * 根据limit 获取Alarm Type
//     * 已弃用
//     * **/
//    public static String getalmType(int type, String value, AlarmLimit Limit1) throws Exception {
//        String alm = "9";
//        /**非空*/
//        if( objCheckIsNull(Limit1) ) {return  alm;}
//        boolean flg = false,fval = false;
//        float max = 0,min = 0,val=0;
//        if (type==11) {
//            flg= Boolean.parseBoolean(value);
//            fval=Limit1.isflg();
//        }
//        else {
//            max=Limit1.getUpperAlm();
//            min=Limit1.getLowerAlm();
//            val=Float.parseFloat(value);
//        }
//
//        switch (type) {
//            case 4:
//                alm = val<min ?"3": val>max ?"4":"1";
//                break;
//            case 11:
//                alm = (flg== fval) ? "2": "1";
//                break;
//            case 2:
//                alm = val<min ?"3": val>max ?"4":"1";
//                break;
//            default:
//                break;
//        }
//        return alm;
//    }

//    private static void dumpItem(SelfOpcItem selfitem, Item item) throws JIException {
//
//        //1.解析传入对象的tag名称 和值
//        //String groupName=item.getGroup().getName();
//        String tagName=item.getId();
//        Object value;
//        switch (selfitem.getTagType()) {
//            case 0:
//                //该Float需要转换成double
//                float objectAsFloat = item.read(true).getValue().getObjectAsFloat();
//                value = Double.parseDouble(String.valueOf(objectAsFloat));
//
//            case 1:
//                value= item.read(true).getValue().getObjectAsInt();
//            case 2:
//                value= (item.read(true).getValue().getObjectAsBoolean()?1:0);
//
//            case 3:
//                value= item.read(true).getValue().getObjectAsString2();
//            default:// string
//                value= item.read(true).getValue().toString();
//        }
}
