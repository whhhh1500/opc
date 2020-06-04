package org.eclipse.milo.examples.client.demo;

import org.eclipse.milo.examples.client.ClientExample;
import org.eclipse.milo.examples.client.ClientExampleRunner;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class Read implements ClientExample {

    public static void main(String[] args) throws Exception {
        Read example = new Read();

        new ClientExampleRunner(example).run();

    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        // synchronous connect
        client.connect().get();

        // 创建一个订阅，轮训时间为1000ms create a subscription @ 1000ms
        UaSubscription subscription = client.getSubscriptionManager()
                .createSubscription(1000.0).get();
        List<String> nodes = new ArrayList<String>();
        nodes.add("Expression1");
        nodes.add("Counter1");
        nodes.add("Square1");
        List<MonitoredItemCreateRequest> requests = new ArrayList<>();
        for (int i = 0; i <3 ; i++) {
            // 订阅服务器节点的Value属性/////// subscribe to the Value attribute of the server's CurrentTime node
            ReadValueId readValueId = new ReadValueId(
                    //Identifiers.Server_ServerStatus_CurrentTime,
                    new NodeId(5, nodes.get(i)),
                    AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE
            );
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

    private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
        logger.info(
                " item=【{}】, value=【{}}",
                item.getReadValueId().getNodeId(), value.getValue());
    }

}
