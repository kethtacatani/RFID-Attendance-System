
package rfid.attendance;

import com.fazecast.jSerialComm.*;
import javax.swing.JOptionPane;
        
public class RFIDATTENDANCE {

    /**
     * @param args the command line arguments
     */
    
     public static SerialPort firstAvailableComPort;
     public static String rfidId;
     public static SerialPort arduinoPort = null;
    
    public static void main(String[] args) {

        QueryProcessor qp = new QueryProcessor();
        HomePanel home = new HomePanel();
        home.setVisible(true);

        // TODO code application logic here

        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();

        for (SerialPort eachComPort : allAvailableComPorts)
            System.out.println("List of all available serial ports: " + eachComPort.getDescriptivePortName());

        arduinoPort = findArduinoPort(allAvailableComPorts);
        if (arduinoPort != null) {
            System.out.println("Arduino is connected on port: " + arduinoPort.getSystemPortName());
            home.arduinoStatus.setText("Scan Status: Connected");
            
            

            // Open the serial port and configure its parameters
            arduinoPort.openPort();
            arduinoPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
            
            // Read data from the serial port
            byte[] buffer = new byte[1024];
            String message = "";
            while (true) {
                int bytesRead = arduinoPort.readBytes(buffer, buffer.length);
                if (bytesRead > 0) {
                    String data = new String(buffer, 0, bytesRead);
                    message += data.trim();
                    if (message.length() == 8) {
                        System.out.println("id is " + message);
                        rfidId = message;
                        home.addRFIDTF.setText(rfidId);
                        System.out.println("why"+rfidId);
                        if(home.enableAttendanceBtn.isSelected()){
                            home.insertStudentAttendance(rfidId);
                        }
                        message = "";
                    } else if (message.length() > 8) {
                        message = "";
                    }
                }
            }
        } else {
            //JOptionPane.showMessageDialog(null, "No RFID Scanner found!");
            System.out.println("Arduino is not connected. Please check the connection.");
            home.arduinoStatus.setText("Scan Status: Disconnected");
        }
    }

    private static SerialPort findArduinoPort(SerialPort[] ports) {
        for (SerialPort port : ports) {
            if (port.getDescriptivePortName().contains("Arduino") || port.getDescriptivePortName().contains("CH340") || port.getDescriptivePortName().contains("CH341")) {
                return port;
            }
        }
        return null;
    }
}
   
    
    
   
