package com.cc1500.opcda.opcclient;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.*;
import org.openscada.opc.dcom.common.KeyedResult;
import org.openscada.opc.dcom.common.KeyedResultSet;
import org.openscada.opc.dcom.da.PropertyDescription;
import org.openscada.opc.dcom.da.impl.OPCGroupStateMgt;
import org.openscada.opc.dcom.da.impl.OPCItemProperties;
import org.openscada.opc.dcom.da.impl.OPCServer;

import java.util.Collection;

import static com.cc1500.opcda.opcclient.BaseConfiguration.*;

/**
 * 获取指定Item的Properties
 * Get the specified  item`s Properties
 *
 * @author Freud
 *
 */
public class TagInfo {
	public static void main(String[] args) throws Exception {
		java.util.logging.Logger.getLogger( "org.jinterop" ).setLevel( java.util.logging.Level.OFF );
		long begin=System.currentTimeMillis();

		JISystem.setAutoRegisteration(true);

		/**
		 * Session获取
		 */
		JISession _session = JISession.createSession(
				getEntryValue(CONFIG_DOMAIN), getEntryValue(CONFIG_USERNAME),
				getEntryValue(CONFIG_PASSWORD));

		final JIComServer comServer = new JIComServer(
				JIClsid.valueOf(getEntryValue(CONFIG_CLSID)),
				getEntryValue(CONFIG_HOST), _session);

		final IJIComObject serverObject = comServer.createInstance();

		OPCServer server = new OPCServer(serverObject);

		/**
		 * 添加一个Group的信息
		 */
		OPCGroupStateMgt group = server.addGroup("test", true, 1000, 1234, 60,
				0.0f, 1033);

		final OPCItemProperties itemProperties = server
				.getItemPropertiesService();
		for(int i=1;i<=10000;i++) {
			dumpItemProperties(itemProperties, "Channel1.Device1.G1.HH"+i);
		}
		long end=System.currentTimeMillis();
		System.out.println( "....耗时为 " +String.format("%.3f",(double)(end-begin)/1000)+" S ");

		server.removeGroup(group, true);
	}

	public static void dumpItemProperties(
            final OPCItemProperties itemProperties, final String itemID)
			throws JIException {
		final Collection<PropertyDescription> properties = itemProperties
				.queryAvailableProperties(itemID);
		final int[] ids = new int[properties.size()];

		int i = 0;
		for (final PropertyDescription pd : properties) {
			ids[i] = pd.getId();
			i++;
		}
		dumpItemProperties2(itemProperties, itemID, ids);
	}

	public static void dumpItemProperties2(
            final OPCItemProperties itemProperties, final String itemID,
            final int... ids) throws JIException {
		final KeyedResultSet<Integer, JIVariant> values = itemProperties
				.getItemProperties(itemID, ids);
			System.out.println(itemID);

	}

	public static void dumpItemPropertiesLookup(
            final OPCItemProperties itemProperties, final String itemID,
            final int... ids) throws JIException {
		final KeyedResultSet<Integer, String> values = itemProperties
				.lookupItemIDs(itemID, ids);
		for (final KeyedResult<Integer, String> entry : values) {
			System.out.println(String.format(
					"ID: %d, Item ID: %s, Error Code: %08x", entry.getKey(),
					entry.getValue(), entry.getErrorCode()));
		}
	}
}
