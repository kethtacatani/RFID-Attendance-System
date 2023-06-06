/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rfid.attendance;

import com.fazecast.jSerialComm.SerialPort;

/**
 *
 * @author Keth Dominic
 */
public class RFIDConnection {
        public static SerialPort firstAvailableComPort;
        public static String rfidId;
        public static String host;
        public static SerialPort arduinoPort = null;
        static HomePanel home = new HomePanel();
        
        
    
    public static void main(String[] args) {
        
        home.setVisible(true);
        rfidConnect();
        
        
    }
    
    
    public static void rfidConnect(){
        
        
        
        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();

        for (SerialPort eachComPort : allAvailableComPorts)
            System.out.println("List of all available serial ports: " + eachComPort.getDescriptivePortName());

        arduinoPort = findArduinoPort(allAvailableComPorts);
        if (arduinoPort != null) {
            System.out.println("Arduino is connected on port: " + arduinoPort.getSystemPortName());
            home.arduinoStatus.setText("<html>RFID Scanner: <font color='green'>Connected</font></html>");

            
            
            

            // Open the serial port and configure its parameters
            arduinoPort.openPort();
            arduinoPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
            
            // Read data from the serial port
            byte[] buffer = new byte[1024];
            String message = "";
            while (true) {
           
                //System.out.println("rip");
                int bytesRead = arduinoPort.readBytes(buffer, buffer.length);
                if (bytesRead > 0) {
                    String data = new String(buffer, 0, bytesRead);
                    message += data.trim();
                    
                    if (message.length() == 8) {
                        System.out.println("id is " + message);
                        rfidId = message;
                        if(home.addRFIDStatus.isSelected()){
                            home.addRFIDTF.setText(rfidId);
                        }
                        //System.out.println("why"+rfidId);
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
            home.arduinoStatus.setText("<html>RFID Scanner: <font color='red'>Disconnected</font></html>");

        }
    }

    public static SerialPort findArduinoPort(SerialPort[] ports) {
        for (SerialPort port : ports) {
            if (port.getDescriptivePortName().contains("Arduino") || port.getDescriptivePortName().contains("CH340") || port.getDescriptivePortName().contains("CH341")) {
                return port;
            }
        }
        return null;
    }
}