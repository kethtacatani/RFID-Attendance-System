/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package rfid.attendance;

import com.fazecast.jSerialComm.*;
        
public class RFIDATTENDANCE {

    /**
     * @param args the command line arguments
     */
    
    public static SerialPort firstAvailableComPort;
    
    public static void main(String[] args) {
        // TODO code application logic here
        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();
        
        for(SerialPort eachComPort:allAvailableComPorts)
            System.out.println("List of all available serial ports: " + eachComPort.getDescriptivePortName());
        
        firstAvailableComPort = allAvailableComPorts[0];
        
        firstAvailableComPort.openPort();

        System.out.println("Opened the first available serial port: " + firstAvailableComPort.getDescriptivePortName()
        + firstAvailableComPort.getPortDescription() + "port is "+firstAvailableComPort.getSystemPortName());

//        MyComPortListener listenerObject = new MyComPortListener();
//        
//        firstAvailableComPort.addDataListener(listenerObject); 

        SerialPort arduinoPort = null;
        for (SerialPort port : allAvailableComPorts) {
            if (port.getSystemPortName().equals("COM3")) {
                arduinoPort = port;
                System.out.println("Connected");
                break;
            }
        }
        
        // Open the serial port and configure its parameters
        // Open the serial port and configure the settings
        arduinoPort.openPort();
        arduinoPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);

        // Read data from the serial port
        byte[] buffer = new byte[1024];
        String message ="";
        while (true) {
            //System.out.println("looping");
            int bytesRead = arduinoPort.readBytes(buffer, buffer.length);
            if (bytesRead > 0) {
                message = new String(buffer, 0, bytesRead);
                if (message.trim().length() == 8){
                System.out.println("id is "+message);
                }
//                else{
//                    System.out.println(" else id is "+message+" "+message.length());
//                }
            }
            String newID = message.trim();
            if (newID.length() == 8 && newID.equals("bb6f3125")) {
            System.out.println("Welcome Keth Tacatani");
            message = "";
        }
        }
        
        
        
        // Close the serial port
        //serialPort.closePort();


        
    }
    
}
