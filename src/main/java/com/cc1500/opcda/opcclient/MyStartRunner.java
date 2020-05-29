package com.cc1500.opcda.opcclient;


import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.openscada.opc.lib.da.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.*;
import static com.cc1500.opcda.opcclient.BaseConfiguration.CONFIG_HOST;
import static com.cc1500.opcda.opcclient.BaseConfiguration.getEntryValue;
import static com.cc1500.opcda.opcclient.CommondUtils.CurrentTimeyyyyMMddHHmmss;
import static com.cc1500.opcda.opcclient.CommondUtils.isReachable;
import static com.cc1500.opcda.opcclient.OpcUtils.*;


/**
 * app启动后运行 订阅
 * @author cc1500
 * @date 2019/8/2
 */

@Component
public class MyStartRunner implements CommandLineRunner{

    private final String screen ="ModBus.Device1.T";
    private  final int QUALITY = 10;//数据质量
    private  final int PERIOD = 1000, SLEEP = 5000;
    private  int add = 0, all = 0;
    private  AccessBase access = null;
    private static MyStartRunner TestI=new MyStartRunner();
    private  Server server = CreaterServer();
    private  List<String> ItemIdMap = new ArrayList<>();
    private  Map<String, String> tags = new HashMap<String, String>();
    private  Map<String, Object> fields = new HashMap<String, Object>();
    long begin = System.currentTimeMillis();
    long tagcount = 0;
    private  boolean cbflag = false;

    /**
     * itemid注册过程，大约为25~35秒
     */
    @Override
    public void run(String... args) {
        /**减少日志**/
        java.util.logging.Logger.getLogger("org.jinterop").setLevel(java.util.logging.Level.OFF);
        getItemIdMap();//获取所有itemid(tagid)放入全局变量ItemIdMap

        System.out.println(getInfo());//获取所有itemid的Properties和address()
        System.out.println(" ******************" + ItemIdMap.size());
        callback();
    }


    private String callback() {
        String callbackflag = "失败";
        String host = getEntryValue(CONFIG_HOST);
        try {

            System.out.println(CurrentTimeyyyyMMddHHmmss("yyyy-MM-dd HH:mm ss") + "     server.getServerState()  " + server.getServerState().getServerState().toString());
            callbackflag = server.getServerState().getServerState().toString();
        } catch (Exception e) {
            callbackflag = "null";
        }
        if (!callbackflag.equals("null")) {
            return CurrentTimeyyyyMMddHHmmss("yyyy-MM-dd HH:mm ss") + "    Server : " + callbackflag;
        }
        if (isReachable(host)) {
            ConnByConnectionInfo( server );
            //TODO 重连/Reconnection
        } else {
            return CurrentTimeyyyyMMddHHmmss("yyyy-MM-dd HH:mm ss") + "    无法连接到 " + host;
        }
        /**开始链接OPCserver**/
        System.out.println(CurrentTimeyyyyMMddHHmmss("yyyy-MM-dd HH:mm ss") + "    开始注册到： " + host);


        List<String> ItemIdList = ItemIdMap;

        try {        /**   注册订阅 */
            SyncR(ItemIdList);
        } catch (Exception e) {

        }
        return "";
    }

    private  void getItemIdMap() {
        try {
            /**    itemId[] 筛选字符串  */
            List<String> ScreenID = new ArrayList<>();
            ScreenID.add(screen);//queryAlltarget ();//new ArrayList<>();  ScreenID.add( "G1" );
            System.out.println(ScreenID);
            ItemIdMap = GetMapByFlo(ScreenID);
        } catch (
                Exception e)

        {
            e.printStackTrace();
        }

    }

    private  String getInfo() {
        Map map = new HashMap();
        long begin = System.currentTimeMillis();
        String[] list = new String[ItemIdMap.size()];
        for (int i = 0; i < ItemIdMap.size(); i++) {
            list[i] = ItemIdMap.get(i);
        }
        try {
            map = GetProperties(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();

            System.out.println(key + "  " + val);
        }
        long end = System.currentTimeMillis();
        return "size="+map.size() + "___耗时为 " + String.format("%.3f", (double) (end - begin) / 1000) + " S ";

    }


    /**
     * 注册订阅
     */
    private void SyncR(List<String> itemIdArr) throws JIException, AddFailedException {
        try {
            access = new SyncAccess(server, PERIOD);
        } catch (Exception e) {
            SyncR(itemIdArr);
        }
        for (final String str : itemIdArr) {
            access.addItem(str, new DataCallback() {
                @Override
                public void changed(Item tag, ItemState state) {

                    if (state.getQuality() > 10) {
                        if ("ModBus.Device1.T2".equals(tag.getId())) {
                            tagcount++;
                            long flg = System.currentTimeMillis();
                            System.out.println(tagcount + "  " + (flg - begin) + "mm");
                            begin = flg;
                        }
                        int type = 0;
                        tags.put("item_id", tag.getId());
                        try {
                            type = state.getValue().getType();
                            tags.put("item_value", getvalueByOPCUtils(type, state));
                            System.out.println("=================tyoe:" + type + " ||||||||||||  " + tags.get("item_value"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        tags.put("value_type", String.valueOf(type));
                        tags.put("quality", String.valueOf(state.getQuality()));
                        tags.put("timestamp", state.getTimestamp().toString());
                    }
                }
            });
        }
        System.out.println("正在订阅");
        add = 0;
        access.bind();
        try {
            Thread.sleep(SLEEP);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                /**轮询开始 PERIOD*3 秒后开始刷新实时数据*/
                cbflag = true;
                add++;
                // TODO do something
            }
        }, PERIOD * 3, 600000);
    }


    private void RealValueBySyncAccess(ItemState state, String str) {
        try {
            if (state.getQuality() > QUALITY) {
                state.toString();

                //var1=   FillingRightItemVal( var1, state, Alias, str, id);

                /**报警已弃用***/       // String sta="1";// getalmType( Integer.parseInt( var1.getType() ), var1.getValue(), almLimit.get( str ) );
            } else {
                //   String obj = RedisUtilre.get( str );
//                var1=   FillItemVal(var1 , Alias, obj, str, id);
            }

            // System.out.println("状态"+ state.getQuality() +" ,值  :"+ state.getValue().toString()   );
            /**计时：每轮实际时间***/

            /**     写入Redis实时库 */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 连接OPCServer  失败则重新连接OPCServer
     */
    private  void ConnByConnectionInfo( Server server) {


        try {
            JISystem.setJavaCoClassAutoCollection( false );
            AutoReconnectController controller = new AutoReconnectController( server );
            server.connect();
            controller.connect();
        } catch (Exception e) {
            all++;
            synchronized (TestI) {
                try {
                    TestI.wait( 6000 );
                } catch (InterruptedException e1) {
                    //    e1.printStackTrace();
                }
            }
            String retStrFormatNowDate = CurrentTimeyyyyMMddHHmmss("yyyy-MM-dd HH:mm ss");
            System.out.println( "[  " + all + " ]正在重新连接。。。。。。" + retStrFormatNowDate );

        }
    }
}






