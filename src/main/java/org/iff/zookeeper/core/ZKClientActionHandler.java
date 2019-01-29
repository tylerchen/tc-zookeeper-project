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
import org.iff.infra.util.Exceptions;
import org.iff.netty.server.ProcessContext;
import org.iff.netty.server.handlers.ActionHandler;
import org.iff.netty.server.handlers.BaseActionHandler;
import org.iff.zookeeper.core.service.ZookeeperClientService;
import org.iff.zookeeper.util.ByteHelper;
import org.iff.zookeeper.util.Tuple;

/**
 * ZKClientActionHandler
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 quit'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 redo 1'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 history'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 printwatches'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 connect host:port'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 create [-s] [-e] path data acl'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 delete path [version]'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 rmr path'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 set path data [version]'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 aget path'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 get path [watch]'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 ls path [watch]'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 ls2 path [watch]'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 getAcl path'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 setAcl path acl [aclVersion]'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 stat path [watch]'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 listquota path'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 setquota -n|-b val path'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 delquota [-n|-b] path'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 close'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 sync path'
 * curl -k -X POST -H "Content-Type: text/plain;charset=UTF-8" "https://localhost:8989/zk/client" -d $'127.0.0.1:2181 addauth scheme auth'
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-25
 * auto generate by qdp.
 */
public class ZKClientActionHandler extends BaseActionHandler {
    public static final String uriPrefix = "/zk/client";

    public boolean execute(ProcessContext ctx) {
        String commandLine = StringUtils.defaultString(ByteHelper.utf8(ctx.getContent())).trim();
        int index = commandLine.indexOf(' ');
        String output = "";
        if (index < 0) {
            boolean connect = ZookeeperClientService.connect(commandLine);
            output = connect ? "connected" : "fail to connect!";
        } else {
            Tuple.Two<String, String, String[]> result = ZookeeperClientService.processZKCmdLine(commandLine.substring(0, index), commandLine.substring(index).trim());
            if (result.hasError()) {
                Exceptions.runtime("ZKClientActionHandler execute zookeeper command error!", result.error());
            }
            output = result.result();
        }
        byte[] src = ByteHelper.utf8(output);
        ctx.getOutputBuffer().writeBytes(src);
        if (src.length > 0 && src[src.length - 1] != '\n') {
            ctx.getOutputBuffer().writeByte('\n');
        }
        ctx.getResponse().setStatus(HttpResponseStatus.OK);
        ctx.outputText();
        return true;
    }

    public ActionHandler create() {
        return new ZKClientActionHandler();
    }

    public boolean matchUri(String uri) {
        return uriPrefix.equals(uri) || (uri.startsWith(uriPrefix) && (uri.charAt(uriPrefix.length()) == '/' || uri.charAt(uriPrefix.length()) == '?'));
    }

}
