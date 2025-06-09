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
     * This method allows to print a string and/or throwable avoiding logger's
     * handlers.
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
        switch (level) {
            case Level l when isBetween(l, Level.SEVERE, Level.OFF) ->
                logger.severe(msg);
            case Level l when isBetween(l, Level.WARNING, Level.SEVERE) ->
                logger.warning(msg);
            case Level l when isBetween(l, Level.INFO, Level.WARNING) ->
                logger.info(msg);
            case Level l when isBetween(l, Level.CONFIG, Level.INFO) ->
                logger.config(msg);
            case Level l when isBetween(l, Level.FINE, Level.CONFIG) ->
                logger.fine(msg);
            case Level l when isBetween(l, Level.FINER, Level.FINE) ->
                logger.finer(msg);
            case null, default ->
                logger.finest(msg);
        }
    }

    private static boolean isBetween(Level toEvaluate, Level bottom, Level top) {
        return toEvaluate.intValue() >= bottom.intValue() && toEvaluate.intValue() < top.intValue();
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
