/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package richtextfield.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomLogger {

    /**
     * This method allows to print a string and/or throwable avoiding logger's handlers.
     *
     * @param source
     * @param level
     * @param message
     * @param t
     */
    public static void print(Class source, Level level, String message, Throwable t) {
        String msg = String.format("%s [%s]: %s \n %s",
                level != null && level.getName() != null ? level.getName() : Level.FINEST.getName(),
                source != null && source.getName() != null ? source.getName() : "",
                message != null ? message : "",
                getStackTrace(t)
        );
        Logger logger = Logger.getLogger(source != null ? source.getName() : CustomLogger.class.getName());
        if (isBetween(level, Level.SEVERE, Level.OFF)) {
            logger.severe(msg);
        } else if (isBetween(level, Level.WARNING, Level.SEVERE)) {
            logger.warning(msg);
        } else if (isBetween(level, Level.INFO, Level.WARNING)) {
            logger.info(msg);
        } else if (isBetween(level, Level.CONFIG, Level.INFO)) {
            logger.config(msg);
        } else if (isBetween(level, Level.FINE, Level.CONFIG)) {
            logger.fine(msg);
        } else if (isBetween(level, Level.FINER, Level.FINE)) {
            logger.finer(msg);
        } else {
            logger.finest(msg);
        }
    }

    private static boolean isBetween(Level toEvaluate, Level bottom, Level top) {
        return toEvaluate != null && toEvaluate.intValue() >= bottom.intValue() && toEvaluate.intValue() < top.intValue();
    }

    /**
     * This method transforms a Throwable into a printable string.
     *
     * @param t
     * @return
     */
    public static String getStackTrace(Throwable t) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            return sw.toString();
        } catch (Throwable ignore) {
            return "";
        }
    }
}
