package ru.sds.plugialo.absenter.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogService {
    static Logger log = LoggerFactory.getLogger(LogService.class);

    public static void error(String msg) {
        log.error("UserAbsencePlanner - Error - :" + msg);
    }

    public static void info(String msg) {
        log.info("UserAbsencePlanner - Info - :" + msg);
    }

    public static void debug(String msg) {
        if (log.isDebugEnabled())
            log.debug("UserAbsencePlanner - Error - :" + msg);
    }
}
