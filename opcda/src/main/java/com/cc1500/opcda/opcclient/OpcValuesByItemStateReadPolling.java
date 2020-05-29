package com.cc1500.opcda.opcclient;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.ItemState;
import org.openscada.opc.lib.da.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static com.cc1500.opcda.opcclient.OpcUtils.GetMapByFlo;


/**
 * 测试轮询
 * 同步读取某个点位的值
 * @author cc1500
 *
 */
public class OpcValuesByItemStateReadPolling {

    private static List<String> map3= new ArrayList<String>(  );
    private static  OpcValuesByItemStateReadPolling test  =new OpcValuesByItemStateReadPolling();;
    private static ItemState itemall;
    private static Server server;
    private static Group group ;
    private static int Quality ,count,countarr;
    private static  int wai=5000;
    private static  float val=0;



//    public static void main(String[] args) throws Exception {
//        //设置控制台减少日志输出
//        java.util.logging.Logger.getLogger("org.jinterop").setLevel(java.util.logging.Level.OFF);
//        String[] ScreenID=new String[]{"Device1.G2","Device1.G1"};
//        ScreenAndDropValue(  ScreenID);
//    }


    public static Server CreOpcConn( ) throws InterruptedException {
        java.util.logging.Logger.getLogger("org.jinterop").setLevel(java.util.logging.Level.OFF);
        try {
            server = new Server(
                    BaseConfiguration.getCLSIDConnectionInfomation(),
                    Executors.newSingleThreadScheduledExecutor());
            JISystem.setJavaCoClassAutoCollection(false);
           server.connect();

            group = server.addGroup();
        }
        catch (  Exception ee ) {
            System.out.println ( "1 重新连接....1" );
            synchronized(test) {
                test.wait(6000);
            }
            CreOpcConn( );
        }
        return  server;
    }
    public static void ScreenAndDropValue( List ScreenID) throws Exception {

        CreOpcConn( );

        try {
            map3= GetMapByFlo(ScreenID);
            String[] list = new String[map3.size()];
            for (int i=0;i<=map3.size();i++) {
                list[i]=map3.get( i );
            }
            Map<String, Item> items =   group.addItems(list);
            StringBuilder cut1 =new StringBuilder("") ;
//            while (true) {
//                synchronized(test) {
//                test.wait(wai);
//                }
                long startTime = System.currentTimeMillis();
                countarr=1;

                for (Map.Entry<String, Item> temp : items.entrySet()) {
                  dumpItem(temp.getValue(),ScreenID);
                    count++;
                }

                long endTime = System.currentTimeMillis();
                cut1.append(" "+(endTime - startTime));
              System.out.println((endTime - startTime) + " + "+wai+")ms "+cut1);
//         }
        }
        catch ( final JIException e ) {
            wai=10000;
            System.out.println("2 重新连接 2");
            System.out.println ( String.format ( "%08X: %s", e.getErrorCode (), server.getErrorMessage ( e.getErrorCode () ) ) );
        }
    }

    private static void dumpItem(Item sa , List ScreenID)  {
        try {

            itemall= sa.read(false);
            Quality =itemall.getQuality();
            val=0;
            wai=1000;
//          Inf inf=new Inf();
//            inf.setId(1);
//            inf.setTagname(sa.getId());
//            inf.setAlias(sa.getId());
//            inf.setTagtype(""+itemall.getValue().getType());
//            inf.setGroupname(sa.getId());
//            inf.setAddr(sa.getId())   ;
//            inf.setGroupid(count);
//            inf.setDescreption(sa.getId());
//            inf.setNote(sa.getId());
//            System.out.println(sa.getId()+"");

            if(Quality>0){
                val= itemall.getValue().getObjectAsFloat();
            }
            else{
                val=0;
            }

        }  catch ( final JIException e ) {
           wai=10000;
            try {

                ScreenAndDropValue( ScreenID);
            }
            catch (  Exception ee ) {
                System.out.println ( "2 数据采集异常" );
            }
            System.out.println ( "1 重新连接...." );
        }
    }

}
