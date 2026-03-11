package logSystem; 

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LogSystem {
    private static final Logger logger = Logger.getLogger("AppFlujo");
    
    static {
        try {
        	/*
        	 * To make the logs stay change the false to true
        	 */
            FileHandler fh = new FileHandler("seguimiento.log", false);
            
            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(record.getMillis()));
                    // Format: <Date> <Thread> <Level> >> Message 
                    return String.format("[%s] [Thread-%s] [%s] >> %s%n", 
                            fecha, 
                            record.getLongThreadID(), //getThreadID(), 
                            record.getLevel(), 
                            record.getMessage());
                }
            });

            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
            
            logger.info("================ " + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())) + " ================");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void flujo(String accion, String detalle) {
        String nombreMetodo = StackWalker.getInstance()
                .walk(frames -> frames.skip(1).findFirst())
                .map(StackWalker.StackFrame::getMethodName)
                .orElse("unknown");

        String mensajeDecorado = String.format("METODO: %-20s | ACCION: %-10s | DETALLE: %s", 
                                                nombreMetodo + "()", 
                                                accion.toUpperCase(), 
                                                detalle);
        logger.info(mensajeDecorado);
    }
}