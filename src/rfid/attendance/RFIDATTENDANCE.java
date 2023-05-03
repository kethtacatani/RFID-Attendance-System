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
        
        firstAvailableComPort = allAvailableComPorts[2];
        
        firstAvailableComPort.openPort();

        System.out.println("Opened the first available serial port: " + firstAvailableComPort.getDescriptivePortName()
        + firstAvailableComPort.getPortDescription() + "port is "+firstAvailableComPort.getSystemPortName());



        SerialPort arduinoPort = null;
        for (SerialPort port : allAvailableComPorts) {
            if (port.getSystemPortName().equals("COM7")) {
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
                String data = new String(buffer, 0, bytesRead);
                 message += data.trim();
                 //System.out.println("id is "+message);
                 
                 
                if (message.length() == 8){
                System.out.println("id is "+message);
                message="";
                
                String messagee = "Hello, Arduino! TIYO JAVA";
                    System.out.println(messagee.getBytes()+" length is"+ messagee.length());
                arduinoPort.writeBytes(messagee.getBytes(), messagee.length());
                }
                else if(message.length() > 8){
                    message="";
                }

            }
        }

    }
    
}
