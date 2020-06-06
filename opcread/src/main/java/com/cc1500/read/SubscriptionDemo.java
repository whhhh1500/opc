package com.cc1500.read;

import com.cc1500.client.ClientExample;
import com.cc1500.client.ClientExampleRunner;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class SubscriptionDemo implements ClientExample {

    public static void main(String[] args) throws Exception {
        SubscriptionDemo createSubscription = new SubscriptionDemo();

        new ClientExampleRunner(createSubscription).run();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public void run( OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception  {

        //创建连接
        client.connect().get();

        //创建发布间隔1000ms的订阅对象
        UaSubscription subscription = client.getSubscriptionManager()
                .createSubscription(1000.0).get();

        //创建订阅的变量
        NodeId nodeId = new NodeId(5,"Expression1");
        ReadValueId readValueId = new ReadValueId(
                nodeId, AttributeId.Value.uid(),null,null);

        //创建监控的参数
        MonitoringParameters parameters = new MonitoringParameters(
                uint(1),
                1000.0,     // sampling interval
                null,       // filter, null means use default
                uint(10),   // queue size
                true        // discard oldest
        );

        //创建监控项请求
        //该请求最后用于创建订阅。
        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);

        List<MonitoredItemCreateRequest> requests = new ArrayList<>();
        requests.add(request);

        //创建监控项，并且注册变量值改变时候的回调函数。
        List<UaMonitoredItem> items = subscription.createMonitoredItems(
                TimestampsToReturn.Both,
                requests,
                (item,id)->{
                    item.setValueConsumer(( value)->{
                        System.out.println("nodeid :"+item.getReadValueId().getNodeId()
                        +"  value :【"+value.getValue().getValue()+"】");

                    });
                }
        ).get();
    }
}
