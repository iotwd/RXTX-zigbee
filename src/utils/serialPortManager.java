package utils;
import exception.*;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 * 串口工具类，实现查找端口、关闭端口、读数据、发送数据、添加事件监听器等
 */

public class serialPortManager {
    /**
     * 查找串口
     */
    @SuppressWarnings("unchecked")
    public static final ArrayList<String> findPort() {
        // 获得当前所有可用串口
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        ArrayList<String> portNameList = new ArrayList<String>();
        //将可用端口添加到list并返回该list
        while (portList.hasMoreElements()) {
            String portName = portList.nextElement().getName();
            portNameList.add(portName);
        }
        return portNameList;
    }

    /**
     * 打开串口
     *
     * @param portName 端口名称
     * @param baudrate 波特率
     * @return 串口对象
     * @throws SerialPortParameterFailure 设置串口参数失败
     * @throws NotASerialPort             端口指向设备不是串口类型
     * @throws NoSuchPortException        没有该端口对应的串口设备
     * @throws PortInUseException
     */
    public static final SerialPort openPort(String portName, int baudrate)
            throws SerialPortParameterFailure, NotASerialPort, NoSuchPortException, PortInUseException, NotASuchPort, PortInUse {
        try{
            //通过端口名识别端口
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            //打开端口，设置端口名和timeout打开操作的超时时间
            CommPort commPort=portIdentifier.open(portName,2000);
            //判断是不是串口
            if(commPort instanceof SerialPort){
                SerialPort serialPort=(SerialPort)commPort;
                try{
                    //设置串口的波特率等参数
                    serialPort.setSerialPortParams(baudrate,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                }catch (UnsupportedCommOperationException e){
                    throw  new SerialPortParameterFailure();
                }
                return serialPort;
            }else{
                throw  new NotASerialPort();
            }
        }catch (NoSuchPortException e1){
            throw new NotASuchPort();
        }catch (PortInUseException e2){
            throw new PortInUse();
        }
    }
    /**
     * 关闭串口
     * @param serialPort
     */
    public static void closePort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
            serialPort = null;
        }
    }
    /**
     *发送数据
     */
    public static void sendToPort(SerialPort serialPort,byte[] order)
        throws SendDataToSerialPortFailure,SerialPortOutputStreamCloseFailure{
        OutputStream out = null;
        try {
            out = serialPort.getOutputStream();
            out.write(order);
            out.flush();
        } catch (IOException e) {
            throw new SendDataToSerialPortFailure();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                throw new SerialPortOutputStreamCloseFailure();
            }
        }
    }

    /**
     * 从串口读数据
     */
    public static byte[] readFromPort(SerialPort serialPort)
            throws ReadDataFromSerialPortFailure,
            SerialPortInputStreamCloseFailure{
        InputStream in=null;
        byte[] bytes=null;
        try {
            in = serialPort.getInputStream();
            //获取buffer里的数据长度
            int bufflenth = in.available();
            while (bufflenth!=0){
                //初始化byte数组为buffer中数据的长度
                bytes = new byte[bufflenth];
                in.read(bytes);
                bufflenth=in.available();
            }
            return bytes;
        }catch (IOException e){
            throw new ReadDataFromSerialPortFailure();
        }finally {
            try{
                if(in!=null){
                    in.close();
                    in=null;
                }
            }catch (IOException e){
                throw new SerialPortInputStreamCloseFailure();
            }
        }
    }

    /**
     * 添加监听器
     */
    public static void addListener(SerialPort port,SerialPortEventListener listener) throws TooManyListeners{
        try {
            port.addEventListener(listener);
            port.notifyOnDataAvailable(true);
            port.notifyOnBreakInterrupt(true);
        }
        catch (TooManyListenersException e){
            throw new TooManyListeners();
        }
    }
}

