/*******************************************************************************
 * Copyright (c) 2019-01-25 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.iff.zookeeper.core;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.iff.infra.util.Logger;
import org.iff.infra.util.PreRequiredHelper;
import org.iff.netty.server.ProcessContext;
import org.iff.netty.server.handlers.ActionHandler;
import org.iff.netty.server.handlers.BaseActionHandler;
import org.iff.zookeeper.core.service.ZookeeperServerService;
import org.iff.zookeeper.util.ByteHelper;

import java.util.List;

/**
 * ZookeeperActionHandler
 *
 * <pre>
 * Start Server:
 * curl -k -X POST "https://localhost:8989/zk/server/create" -H "Content-Type: text/plain;charset=UTF-8" -d $'clientPort=2181\ndataDir=/Users/zhaochen/Desktop/temppath/zk'
 * curl -k "https://localhost:8989/zk/server/list"
 * curl -k "https://localhost:8989/zk/server/shutdown/serverId"
 * </pre>
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-25
 * auto generate by qdp.
 */
public class ZKServerActionHandler extends BaseActionHandler {
    public static final String uriPrefix = "/zk/server";

    public boolean execute(ProcessContext ctx) throws Exception {
        String result = "";
        String[] paths = PreRequiredHelper.trimAndRemoveBlank(ctx.getRequestPath().split("/"));
        if ("create".equals(paths[2])) {
            String contents = ByteHelper.utf8(ctx.getContent());
            Logger.debug("ZKServerActionHandler Zookeeper Server Config:\n" + contents);
            result = ZookeeperServerService.createZkServer(contents);
        } else if ("list".equals(paths[2])) {
            List<String> servers = ZookeeperServerService.servers();
            result = StringUtils.join(servers, ", ");
        } else if ("shutdown".equals(paths[2])) {
            String serverId = paths[3];
            PreRequiredHelper.requireNotBlank(serverId, "ZKServerActionHandler zookeeper server id is required!");
            ZookeeperServerService.shutdown(serverId);
            result = "OK";
        }
        ctx.getOutputBuffer().writeBytes(ByteHelper.utf8(result));
        ctx.getResponse().setStatus(HttpResponseStatus.OK);
        ctx.outputHtml();
        return true;
    }

    public ActionHandler create() {
        return new ZKServerActionHandler();
    }

    public boolean matchUri(String uri) {
        return uriPrefix.equals(uri) || (uri.startsWith(uriPrefix) && (uri.charAt(uriPrefix.length()) == '/' || uri.charAt(uriPrefix.length()) == '?'));
    }

}
