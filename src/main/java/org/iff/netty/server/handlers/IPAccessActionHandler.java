/*******************************************************************************
 * Copyright (c) Nov 17, 2016 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation
 ******************************************************************************/
package org.iff.netty.server.handlers;

import org.apache.commons.lang3.StringUtils;
import org.iff.infra.util.FCS;
import org.iff.infra.util.Logger;
import org.iff.infra.util.StringHelper;
import org.iff.netty.server.ProcessContext;

/**
 * ip access handler.
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since Nov 17, 2016
 */
public class IPAccessActionHandler extends BaseActionHandler {

    public ActionHandler create() {
        return new IPAccessActionHandler();
    }

    public boolean execute(ProcessContext ctx) {
        String clientIp = ctx.getHeaders().get("client_ip");
        Object obj = ctx.getConfig().get("access.ip");
        if (obj == null) {
            Logger.debug(FCS.get("IPAccessActionHandler Config access.ip is not set, block {0} access!", clientIp));
            /*return false will process next RestHandler*/
            return false;
        }
        if (StringUtils.isBlank(clientIp)) {
            Logger.debug(FCS.get("IPAccessActionHandler No client ip found, block {0} access!", clientIp));
            /*return false will process next RestHandler*/
            return false;
        }
        String accessIp = obj.toString();
        if (StringUtils.isBlank(accessIp)) {
            return true;
        }
        String[] ips = StringUtils.split(accessIp, ',');
        for (String aip : ips) {
            boolean match = StringHelper.wildCardMatch(clientIp, aip.trim());
            if (match) {
                Logger.debug(FCS.get("IPAccessActionHandler Accept {0} access!", clientIp));
                /*return false will process next RestHandler*/
                return false;
            }
        }
        Logger.debug(FCS.get("IPAccessActionHandler Block {0} access!", clientIp));
        /*return true will block next RestHandler*/
        return true;
    }

    public boolean matchUri(String uri) {
        return true;
    }

    public int getOrder() {
        return -100;
    }
}
