/*******************************************************************************
 * Copyright (c) 2019-01-28 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.zookeeper.core.service;

import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.iff.infra.util.Exceptions;
import org.iff.infra.util.Logger;
import org.iff.infra.util.SocketHelper;
import org.iff.zookeeper.core.CustomQuorumPeerMain;
import org.iff.zookeeper.core.CustomZooKeeperServerMain;

import java.io.Closeable;
import java.io.StringReader;
import java.util.*;

/**
 * ZookeeperServerService
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-28
 * auto generate by qdp.
 */
public class ZookeeperServerService {

    public static final Map<String, Closeable> map = new HashMap<>();

    public static String createZkServer(String configContent) {
        String id = "";
        try {
            QuorumPeerConfig config = new QuorumPeerConfig();
            {
                Properties zkProp = new Properties();
                zkProp.load(new StringReader(configContent));
                config.parseProperties(zkProp);
            }
            id = config.getClientPortAddress().getAddress().getHostAddress() + ":" + config.getClientPortAddress().getPort();
            if (config.getServers().size() > 0) {
                CustomQuorumPeerMain main = CustomQuorumPeerMain.start(config);
                if (main.getStartupError() != null) {
                    Exceptions.runtime("ZKServerActionHandler startup zookeeper QuorumPeer server fail!", main.getStartupError());
                }
                map.put(id, main);
            } else {
                Logger.warn("ZKServerActionHandler Either no config or no quorum defined in config, running in standalone mode");
                // there is only server in the quorum -- run as standalone
                String[] args = new String[]{
                        String.valueOf(config.getClientPortAddress().getPort()),//port
                        config.getDataDir(),//dataDir
                        String.valueOf(config.getTickTime()),//tickTime
                        String.valueOf(config.getMaxClientCnxns())//maxClientCnxns
                };
                CustomZooKeeperServerMain main = CustomZooKeeperServerMain.start(args);
                if (main.getStartupError() != null) {
                    Exceptions.runtime("ZKServerActionHandler startup zookeeper standalone server fail!", main.getStartupError());
                }
                map.put(id, main);
            }
        } catch (Exception e) {
            Exceptions.runtime("ZookeeperServerService startup zookeeper fail!", e);
        }
        return id;
    }

    public static List<String> servers() {
        return new ArrayList<>(map.keySet());
    }

    public static void shutdown(String id) {
        if (map.containsKey(id)) {
            SocketHelper.closeWithoutError(map.remove(id));
        }
    }
}
