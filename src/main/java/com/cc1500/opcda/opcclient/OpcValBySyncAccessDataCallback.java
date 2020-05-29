package com.cc1500.opcda.opcclient;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;


/**
 *  测试订阅
 * @author cc1500
 * @date 2019/7/23
 */
public class OpcValBySyncAccessDataCallback {

    /**
     * 轮询时间
     */
    private static final int PERIOD = 1000;
    /**
     * 休眠时间
     */
    private static OpcValBySyncAccessDataCallback TestI = new OpcValBySyncAccessDataCallback();
    private static final int SLEEP = 5000;
    private static int add = 0, all = 0;
    private static AccessBase access = null;
    private static Server server =null;
    private static HashMap<String, String> map3 = new HashMap<>();
    //t1 t2 为itemid注册过程计时，大约为25~35秒
 //   private static long t1,t2;

//    public static void main(String[] args) {
//        //设置控制台减少日志输出
//        java.util.logging.Logger.getLogger("org.jinterop").setLevel(java.util.logging.Level.OFF);
//        //链接opc服务器，失败重连重连
//        controllerA();
//        try {
//
//            // itemId[] 筛选字符串
//            String[] ScreenID=new String[]{"Device1.G2","Device1.G1"};
//            //获取map  HashMap<String, Float> map3 = new HashMap<>()  {itemID:0}
//            map3 = GetMapByFlo(ScreenID);
//          //  t1=     System.currentTimeMillis();
//        } catch ( Exception e ) {
//            e.printStackTrace();
//        }
//        String[] itemIdArr = new String[map3.size()];
//        int icon = 0;
//        // itemId[] 筛选字符串
//        for (String key : map3.keySet()) {
//            itemIdArr[icon] = key;
//            icon++;
//        }
//        //注册订阅
//        SyncR(itemIdArr);
//    }
    /**
     * 注册订阅，失败则重新连接OPCServer,重新注册
     */

    public static void SyncR(String[] itemIdArr) {
        try {
            access = new SyncAccess(server, PERIOD);
        } catch ( Exception e ) {
            controllerA();
            SyncR(itemIdArr);
        }
        for (final String str : itemIdArr) {
            add++;
            int Count = add % 500, aim = add;
            try {
                access.addItem(str, new DataCallback() {
                    @Override
                    public void changed(Item item, ItemState state) {
                        try {
                            if (state.getValue().getObjectAsFloat()>0) {
                               // t2 = System.currentTimeMillis();
                                //t1 t2 为itemid注册过程计时
                               // System.out.println("启动：" + (t2 - t1) + "ms");
                                System.out.println( "  " + str+ "  value: " + state.getValue().getObjectAsFloat()
                                   //     + " ]     Quality: "  + Count + "   " + aim
                                        + " ]     Quality: " + state.getQuality()
                                        + "      T:   " + state.getTimestamp().getTime()
                                        + "         T:  " + state.getValue().getType()
                                        + "       value: " + state.getValue().getObjectAsFloat());
                            }
                        } catch ( final JIException e ) {
                            System.out.println("1 重新连接....");
                        }
                    }
                });
            } catch ( JIException e ) {
                e.printStackTrace();
            } catch ( AddFailedException e ) {
                e.printStackTrace();
            }
        }
        add = 0;
        access.bind();
        try {
            Thread.sleep(SLEEP);
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
        Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("  access.isActive()  "+access.isActive());
                }
            }, 5000,3000);
    }
    /**
     * 连接OPCServer  失败则重新连接OPCServer
     */
    public static void controllerA() {
        final ConnectionInformation ci = new ConnectionInformation("cc1500", "1234567");
        ci.setHost("192.168.1.112");
        ci.setDomain("");
        ci.setProgId("KEPware.KEPServerEx.V4");
        ci.setClsid("6E6170F0-FF2D-11D2-8087-00105AA8F840");
        server = new Server(ci, Executors.newSingleThreadScheduledExecutor());
        try {
            JISystem.setJavaCoClassAutoCollection(false);
            AutoReconnectController controller = new AutoReconnectController(server);
            server.connect();
            controller.connect();
        } catch ( Exception e ) {
            all++;
            //我不知道这里是个啥原理，反正是个挺好用的定时器  next row
            synchronized (TestI) {
                try {
                    TestI.wait(6000);
                } catch ( InterruptedException e1 ) {
                    e1.printStackTrace();
                }
            }
            long endTime = System.currentTimeMillis();
            Date nowTime = new Date(endTime);
            SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm ss");
            String retStrFormatNowDate = sdFormatter.format(nowTime);
            //链接次数和当前时间
            System.out.println("[ " + all + " ]正在重新连接。。。。。。"+retStrFormatNowDate);
            //重新连接OPCSERVER
            controllerA();
        }

    }




}

