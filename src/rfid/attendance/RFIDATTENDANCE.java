
package rfid.attendance;

import com.fazecast.jSerialComm.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;

        
public class RFIDATTENDANCE {

    /**
     * @param args the command line arguments
     */
    

    
    public static void main(String[] args) {

        
        
        QueryProcessor qp = new QueryProcessor();
        RFIDConnection rfid = new RFIDConnection();
        rfid.main(args);
        
    }

}
   
    
    
   
