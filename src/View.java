import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import gnu.io.*;
import exception.*;
import java.awt.event.*;
import socket.SendToWeb;
import exception.serialPortManager;


/**
 * 实现画图类和数据的操作读取等
 * 在299行修改解析数据的代码，自定义正则表达式的规则，在try语句内设处理发送服务器的内容
 */
public class View extends JFrame {
    View view = null;
    //保存可用端口号
    private List<String> commList = null;
    //保存端口对象
    private SerialPort serialPort = null;
//    private Font font = new Font("微软雅黑", Font.BOLD, 25);
    private JLabel label = new JLabel("数据：");
    private TextArea textArea = new TextArea();
    private Choice commChoice = new Choice();//串口选择
    private Choice bpsChoice = new Choice();//波特率选择
    private JButton openSerialButton = new JButton("打开串口");
    Image offScreen = null;    //重画时的画布

    /**
     * 类的构造方法
     *
     * @param client
     */
    public View(Client client) {
        this.view = view;
        commList = serialPortManager.findPort();//程序初始化时就扫描一次有效串口
    }

    /*
    主菜单显示，添加界面与相关事件监听
     */
    public void viewFrame() {
        this.setBounds(100, 200, 800, 600);
        this.setTitle("串口程序");
        this.setBackground(Color.WHITE);
        this.setLayout(null);
        //添加事件监听器
        this.addWindowListener(new WindowAdapter() {
            public void WindowClosing(WindowEvent arg0) {
                if (serialPort != null) {
                    serialPortManager.closePort(serialPort);
                }
                System.exit(0);
            }
        });
        label.setBounds(70, 103,50,20);
        add(label);
        textArea.setBounds(270, 103, 325, 200);
        add(textArea);
        //添加串口选择选项
        commChoice.setBounds(160, 397, 200, 200);
        //检查是否有可用串口，有则加入选项中
        if (commList == null || commList.size() < 1) {
            JOptionPane.showMessageDialog(null, "没有搜索到有效串口！", "错误", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (String s : commList) {
                commChoice.add(s);
            }
        }
        add(commChoice);

        //添加波特率选项
        bpsChoice.setBounds(526, 396, 200, 200);
        bpsChoice.add("1200");
        bpsChoice.add("2400");
        bpsChoice.add("4800");
        bpsChoice.add("9600");
        bpsChoice.add("14400");
        bpsChoice.add("19200");
        bpsChoice.add("115200");
        add(bpsChoice);
        //添加打开串口按钮
        openSerialButton.setBounds(250, 490, 300, 50);
        openSerialButton.setBackground(Color.lightGray);
        openSerialButton.setFont(new Font("微软雅黑", Font.BOLD, 20));
        openSerialButton.setForeground(Color.darkGray);
        add(openSerialButton);
        //添加打开串口按钮的事件监听
        openSerialButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                //获取串口名称
                String commName = commChoice.getSelectedItem();
                //获取波特率
                String bpsStr = bpsChoice.getSelectedItem();

                //检查串口名称是否获取正确
                if (commName == null || commName.equals("")) {
                    JOptionPane.showMessageDialog(null, "没有搜索到有效串口！", "错误", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    //检查波特率是否获取正确
                    if (bpsStr == null || bpsStr.equals("")) {
                        JOptionPane.showMessageDialog(null, "波特率获取错误！", "错误", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        //串口名、波特率均获取正确时
                        int bps = Integer.parseInt(bpsStr);
                        try {

                            //获取指定端口名及波特率的串口对象
                            serialPort = serialPortManager.openPort(commName, bps);
                            //在该串口对象上添加监听器
                            serialPortManager.addListener(serialPort, new SerialListener());
                            //监听成功进行提示
                            JOptionPane.showMessageDialog(null, "监听成功，稍后将显示监测数据！", "提示", JOptionPane.INFORMATION_MESSAGE);

                        } catch (SerialPortParameterFailure | NotASerialPort | NotASuchPort | PortInUse | TooManyListeners e1) {
                            //发生错误时使用一个Dialog提示具体的错误信息
                            JOptionPane.showMessageDialog(null, e1, "错误", JOptionPane.INFORMATION_MESSAGE);
                        } catch (NoSuchPortException e1) {
                            e1.printStackTrace();
                        } catch (PortInUseException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
        this.setResizable(false);
        new Thread(new RepaintThread()).start();    //启动重画线程
    }
    /**
     * 画出主界面组件元素
     */
    public void paint(Graphics g) {
        Color c = g.getColor();

        g.setColor(Color.black);
        g.setFont(new Font("微软雅黑", Font.BOLD, 25));
        g.drawString(" 数据： ", 45, 130);


        g.setColor(Color.gray);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString(" 串口选择： ", 45, 410);

        g.setColor(Color.gray);
        g.setFont(new Font("微软雅黑", Font.BOLD, 20));
        g.drawString(" 波特率： ", 425, 410);
    }

    /**
     * 双缓冲方式重画界面各元素组件
     */
    public void update(Graphics g) {
        if (offScreen == null) offScreen = this.createImage(Client.WIDTH, Client.HEIGHT);
        Graphics gOffScreen = offScreen.getGraphics();
        Color c = gOffScreen.getColor();
        gOffScreen.setColor(Color.white);
        gOffScreen.fillRect(0, 0, Client.WIDTH, Client.HEIGHT);    //重画背景画布
        this.paint(gOffScreen);    //重画界面元素
        gOffScreen.setColor(c);
        g.drawImage(offScreen, 0, 0, null);    //将新画好的画布“贴”在原画布上
    }

    /*
     * 重画线程（每隔30毫秒重画一次）
	 */
    private class RepaintThread implements Runnable {
        public void run() {
            while (true) {
                //调用重画方法
                repaint();
                //扫描可用串口
                commList = serialPortManager.findPort();
                if (commList != null && commList.size() > 0) {
                    //添加新扫描到的可用串口
                    for (String s : commList) {
                        //该串口名是否已存在，初始默认为不存在（在commList里存在但在commChoice里不存在，则新添加）
                        boolean commExist = false;
                        for (int i = 0; i < commChoice.getItemCount(); i++) {
                            if (s.equals(commChoice.getItem(i))) {
                                //当前扫描到的串口名已经在初始扫描时存在
                                commExist = true;
                                break;
                            }
                        }
                        if (commExist) {
                            //当前扫描到的串口名已经在初始扫描时存在，直接进入下一次循环
                            continue;
                        } else {
                            //若不存在则添加新串口名至可用串口下拉列表
                            commChoice.add(s);
                        }
                    }
                    //移除已经不可用的串口
                    for (int i = 0; i < commChoice.getItemCount(); i++) {

                        //该串口是否已失效，初始默认为已经失效（在commChoice里存在但在commList里不存在，则已经失效）
                        boolean commNotExist = true;

                        for (String s : commList) {
                            if (s.equals(commChoice.getItem(i))) {
                                commNotExist = false;
                                break;
                            }
                        }
                        if (commNotExist) {
                            //System.out.println("remove" + commChoice.getItem(i));
                            commChoice.remove(i);
                        } else {
                            continue;
                        }
                    }
                } else {
                    //如果扫描到的commList为空，则移除所有已有串口
                    commChoice.removeAll();
                }
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    JOptionPane.showMessageDialog(null, "ERROR", "错误", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            }
        }

    }

    /**
     * 处理监控到的串口事件
     */
    private class SerialListener implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            switch (serialPortEvent.getEventType()) {
                case SerialPortEvent.BI://10通讯中断
                    JOptionPane.showMessageDialog(null, "与串口通讯中断", "错误", JOptionPane.INFORMATION_MESSAGE);
                    break;

                case SerialPortEvent.OE: // 7 溢位（溢出）错误

                case SerialPortEvent.FE: // 9 帧错误

                case SerialPortEvent.PE: // 8 奇偶校验错误

                case SerialPortEvent.CD: // 6 载波检测

                case SerialPortEvent.CTS: // 3 清除待发送数据

                case SerialPortEvent.DSR: // 4 待发送数据准备好了

                case SerialPortEvent.RI: // 5 振铃指示

                case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
                    break;
                case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
                    System.out.println("找到数据");
                    byte[] data = null;
                    try {
                        if (serialPort == null) {
                            JOptionPane.showMessageDialog(null, "串口对象为空！监听失败！", "错误", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            data = serialPortManager.readFromPort(serialPort);    //读取数据，存入字节数组
                          //  System.out.println("1111111111111111111111111111111111111");
                            System.out.println(new String(data));
                            //自定义解析过程
                            if (data == null || data.length < 1) {
                                JOptionPane.showMessageDialog(null, "读取数据过程中未获取到有效数据！请检查设备或程序！", "错误", JOptionPane.INFORMATION_MESSAGE);
                                System.exit(0);
                            } else {
                                //初始数据是data
                                String dataOriginal = new String(data);
                             //   System.out.println("22222222222222222222222222222222222");
                                System.out.println("原始数据：" + dataOriginal);

                                //把原始数据写入到D盘的txt文件中
                                try {
                                    File file = new File("D:/OriginalResult.txt");
                                    PrintStream ps = new PrintStream(new FileOutputStream(file, true));;
                                    ps.append(dataOriginal + "\r\n");
                                    ps.flush();
                                    ps.close();
                                } catch (FileNotFoundException eg) {
                                    eg.printStackTrace();
                                }
                                String dataValid = "";//有效数据 保存原始数据字符串去除最开头*号以后的字符串
                                String[] elements = null;//保存按空格拆分原始字符串后得到的字符串数组
                                    //            执行到了这里
                                    //解析数据

                                if (dataOriginal.charAt(0) =='S') {//当数据的第一个字符是#号时表示数据接收完成，开始解析
                                    dataValid = dataOriginal.substring(0);
//                                    String regex="\\D+";
                                    String regex="\\D+(\\.\\D+)?";//解析字符串里的整数或者小数
                                    elements = dataValid.split(regex);//按空格解析数据
                                     //String regex="\\D+";
                                    // elements = dataValid.split(regex);//按空格解析数据
                                    for(String string:elements){
                                        System.out.println(string);
                                    }
                                    if (elements == null || elements.length < 1) {//检查数据是否解析正确
                                        JOptionPane.showMessageDialog(null, "数据解析过程出错，请检查设备或程序！", "错误", JOptionPane.INFORMATION_MESSAGE);
                                        System.exit(0);
                                    } else {
                                        try {
                                            //定义发送到数据库的对象
                                           SendToWeb sendToWeb = new SendToWeb();
                                           String message="";
//                                           处理心率
                                           Integer heartRate = new Integer(elements[2]);
                                           if(heartRate<120&&heartRate>40){
                                               message=elements[2];
                                           }
                                           //处理血氧
                                            Integer bloodOxygen = new Integer(elements[3]);
                                            if(bloodOxygen<120&&bloodOxygen>40){
                                                message=elements[2];
                                            }
                                            //处理温度
                                            Float temperature = Float.parseFloat(elements[0]);
                                            if(temperature<41&&heartRate>35) {
                                                message = elements[2];
                                            }
                                            //处理时间
                                            Date nowTime=new Date();
                                            String t1=String.format("%tY-%<tm-%<td-%<tH:%<tM:%<tS",nowTime);
                                            if(elements[0]!=null && elements[1]!=null&&elements[2]!=null){
                                                //创建发送的信息
                                                message=elements[0] + "#" + elements[1] + "#" + elements[2] + "#"+ t1;
                                                //发送数据到数据库
                                                //sendToWeb.sendMessage(message);
                                                //显示到文本区/测试
                                                textArea.setText(message);
                                            }

                                            message=elements[1] + "#" + elements[2] +"#"+elements[3] +  "#"+ t1;
                                            Thread.currentThread().sleep(10000);
                                            //发送数据到数据库
                                           sendToWeb.sendMessage(message);
                                            //显示到文本区/测试
                                            System.out.println(message);
                                            textArea.setText(message);


                                        } catch (ArrayIndexOutOfBoundsException e) {
                                            JOptionPane.showMessageDialog(null, "数据解析过程出错，更新界面数据失败！请检查设备或程序！", "错误", JOptionPane.INFORMATION_MESSAGE);
                                            System.exit(0);
                                        }
                                        catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (ReadDataFromSerialPortFailure | SerialPortInputStreamCloseFailure e) {
                        JOptionPane.showMessageDialog(null, e, "错误", JOptionPane.INFORMATION_MESSAGE);
                        System.exit(0);    //发生读取错误时显示错误信息后退出系统
                    }
                    break;
            }
        }
    }
}
