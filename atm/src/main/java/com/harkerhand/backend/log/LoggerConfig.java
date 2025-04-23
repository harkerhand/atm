package com.harkerhand.backend.log;

import java.io.IOException;
import java.util.logging.*;

public class LoggerConfig {
    public static void setup() throws IOException {
        Logger logger = Logger.getLogger("ATMServer");
        FileHandler fileHandler = new FileHandler("atm.log", true);
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
    }
}