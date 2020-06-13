package com.cc1500.influxDB;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Influxdb {
    /**连接时序数据库，获取influxDB**/
    public static InfluxDB influxDB=influxDbBuild();
    public static InfluxDB influxDbBuild() {
        if(influxDB!=null)
            return influxDB;
        influxDB = InfluxDBFactory.connect("http://192.168.1.109:8086", "root", "123456");
        Pong pong = influxDB.ping();
        if(pong != null){
            System.out.println("pong：" + pong + ",连接成功！");
        }else{
            System.out.println("连接失败");
            return null;
        }
        return influxDB;
    }

    public QueryResult query(String command) {
        Query query = new Query(command, "opch");
        QueryResult a = influxDB.query(query);
        return a;
    }
    public void insert(String measurement,Map<String, String> tags, Map<String, Object> fields) {
        Point.Builder builder = Point.measurement(measurement);//源码讲表名赋值到类中
        builder.tag(tags);//源码对标签进行验证，不得有空
        builder.fields(fields);//源码将数据值赋值到类中

        influxDB.write("opch", "", builder.build());//向数据库写数据，builder.build()：验证表名不得为空，field数据长度大于0。
    }
    //   //insert opc1,identifier=Counter1 spaceindex=5,type=13,value=1.1
    private  String identifier="identifier";
    private  String spaceindex="spaceindex";
    private  String type="type";
    private  String valuere="value";

    public void insertopc(String measurement,Map<String, String> tags) {
        Point.Builder builder = Point.measurement(measurement);//源码讲表名赋值到类中
        builder.time(System.currentTimeMillis(), TimeUnit.MICROSECONDS);
        builder.addField(spaceindex,tags.get(spaceindex));
        builder.addField(type,tags.get(type));
        builder.addField(valuere,tags.get(valuere));
        builder.tag(identifier,tags.get(identifier));
        Point point = builder.build();
        //可传入map参数
       // builder.tag(tags);//源码对标签进行验证，不得有空
      //  builder.fields(fields);//源码将数据值赋值到类中
        influxDB.setDatabase("opch").write(point);
        System.out.println("-----------");
       //向数据库写数据，builder.build()：验证表名不得为空，field数据长度大于0。
    }

}
