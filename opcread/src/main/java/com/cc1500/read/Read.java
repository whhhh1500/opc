package com.cc1500.read;

import com.cc1500.client.ClientExample;
import com.cc1500.influxDB.Influxdb;
import com.cc1500.read.conf.NodesConfiguration;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static com.cc1500.influxDB.Influxdb.influxDbBuild;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class Read implements ClientExample  {
 public Influxdb inf=new Influxdb();
    //insert opc1,identifier=Counter1 spaceindex=5,type=13,value=1.1
        //static {
        //    if (influxDB==null)
        //    influxDB=InfluxDBFactory.connect("127.0.0.1","root","123456");
        //    influxDB.setDatabase("opch");
        //    Pong pong = influxDB.ping();
        //    if(pong != null){
        //        System.out.println("pong：" + pong + ",连接成功！");
        //    }else{
        //        System.out.println("连接失败");
        //    }
        //}
    private ConcurrentHashMap mapreal=new ConcurrentHashMap();
    private NodesConfiguration configuration;
    public Read(NodesConfiguration configuration){
        this.configuration=configuration;
    }




    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        inf.influxDB= influxDbBuild();
        // synchronous connect

        client.connect().get();

        // 创建一个订阅，轮训时间为1000ms create a subscription @ 1000ms
        UaSubscription subscription = client.getSubscriptionManager()
                .createSubscription(1000.0).get();
        List<String> nodes;
        if ( configuration==null){
            nodes=new ArrayList<String>();
            nodes.add("5,Square1");
        }else {
            nodes= configuration.getNodelist();
        }


        int size=  nodes.size();
        List<MonitoredItemCreateRequest> requests = new ArrayList<>();
        for (int i = 0; i <size ; i++) {
            // 订阅服务器节点的Value属性/////// subscribe to the Value attribute of the server's CurrentTime node
            ReadValueId readValueId = new ReadValueId(
                    new NodeId(Integer.parseInt(nodes.get(i).split(",")[0]),
                            nodes.get(i).split(",")[1]),
                    AttributeId.Value.uid(),
                    null,
                    QualifiedName.NULL_VALUE);

            MonitoringParameters parameters = new MonitoringParameters(
                    uint(i+1),
                    1000.0,     // sampling interval
                    null,       // filter, null means use default
                    uint(10),   // queue size
                    true        // discard oldest
            );
            MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting,parameters);
            requests.add(request);

        }

            BiConsumer<UaMonitoredItem, Integer> onItemCreated =
                (item, id) -> item.setValueConsumer(this::onSubscriptionValue);

        List<UaMonitoredItem> items = subscription.createMonitoredItems(
                TimestampsToReturn.Both,
                requests,
                onItemCreated
        ).get();

        for (UaMonitoredItem item : items) {
            if (item.getStatusCode().isGood()) {
                logger.info("item created for nodeId={}", item.getReadValueId().getNodeId());
            } else {
                logger.warn(
                        "failed to create item for nodeId={} (status={})",
                        item.getReadValueId().getNodeId(), item.getStatusCode());
            }
        }
        //// let the example run for 5 seconds then terminate
        //Thread.sleep(5000);
        //future.complete(client);
    }
    private  String identifier="identifier";
    private  String spaceindex="spaceindex";
    private  String type="type";
    private  String valuere="value";
    private  HashMap<String,String> map1=new HashMap();
    private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
        NodeId ni=item.getReadValueId().getNodeId();
        map1.put(identifier,ni.getIdentifier().toString());
        map1.put(spaceindex,ni.getNamespaceIndex().toString());
        map1.put(type,value.getValue().getDataType().get().getIdentifier().toString());
        map1.put(valuere,value.getValue().getValue().toString());
        System.out.println(value.getValue().getValue().toString());
        inf.insertopc("opc1",map1);
        //map.put(ni.getNamespaceIndex()+","+ni.getIdentifier().toString(),
        //        value.getValue().getDataType().get().getIdentifier()+","+value.getValue().getValue().toString());
        //value.getValue().getDataType().get() 返回一个以type作为Identifier的NodeId
        //logger.info(
        //        " item=【{}】,type=【{}】, value=【{}}",
        //        item.getReadValueId().getNodeId(),
        //        value.getValue().getDataType().get().getIdentifier(),
        //        value.getValue().getValue());
        //System.out.println(map.get(ni.getNamespaceIndex()+","+ni.getIdentifier().toString()));
    }


}
