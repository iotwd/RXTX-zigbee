import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
public class Client extends JFrame{
    public static final int WIDTH=800;
    public static final int HEIGHT=600;
    Color color=Color.WHITE;
    Image offScreen=null;
    View view = new View(this);
    public static void main(String[] args) {
        new Client().launchFrame();
    }
    public void launchFrame(){
        this.setBounds(100,100,WIDTH,HEIGHT);
        this.setTitle("串口工具");
        this.setBackground(Color.WHITE);
        this.addWindowListener(new WindowAdapter() {
            //添加对窗口状态的监听
            public void windowClosing(WindowEvent arg0) {
                //当窗口关闭时
                System.exit(0);	//退出程序
            }
        });
        this.addKeyListener(new KeyMonitor());	//添加键盘监听器
        this.setResizable(false);	//窗口大小不可更改
        this.setVisible(true);	//显示窗口

        new Thread(new RepaintThread()).start();	//开启重画线程
    }
    public void paint(Graphics g) {
        Color c = g.getColor();

        g.setFont(new Font("微软雅黑", Font.BOLD, 40));
        g.setColor(Color.black);
        g.drawString("ZigBee实践周串口工具", 45, 190);

        g.setFont(new Font("微软雅黑", Font.ITALIC, 26));
        g.setColor(Color.BLACK);
        g.drawString("Powered By：ZhongLei", 280, 260);

        g.setFont(new Font("微软雅黑", Font.ITALIC, 26));
        g.setColor(Color.BLACK);
        g.drawString("Modified by Wangdong 2019.11", 320, 320);

        g.setFont(new Font("微软雅黑", Font.BOLD, 30));
        g.setColor(color);
        g.drawString("————按回车键进入主界面————", 100, 480);
        //使文字 "————点击Enter键进入主界面————" 黑白闪烁
        if (color == Color.WHITE)	color = Color.black;
        else if (color == color.BLACK)	color = Color.white;
    }
    /**
     * 双缓冲方式重画界面各元素组件
     */
    public void update(Graphics g) {
        if (offScreen == null)	offScreen = this.createImage(WIDTH, HEIGHT);
        Graphics gOffScreen = offScreen.getGraphics();
        Color c = gOffScreen.getColor();
        gOffScreen.setColor(Color.white);
        gOffScreen.fillRect(0, 0, WIDTH, HEIGHT);	//重画背景画布
        this.paint(gOffScreen);	//重画界面元素
        gOffScreen.setColor(c);
        g.drawImage(offScreen, 0, 0, null);	//将新画好的画布“贴”在原画布上
    }
    /*
	 * 内部类形式实现对键盘事件的监听
	 */
    private class KeyMonitor extends KeyAdapter {

        public void keyReleased(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode == KeyEvent.VK_ENTER) {	//当监听到用户敲击键盘enter键后执行下面的操作
                setVisible(false);	//隐去欢迎界面
                view.setVisible(true);	//显示监测界面
                view.viewFrame();	//初始化监测界面
            }
        }
    }
   /*
   * 重画线程（每隔250毫秒重画一次）
   */
    private class RepaintThread implements Runnable {
        public void run() {
            while(true) {
                repaint();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    //重画线程出错抛出异常时创建一个Dialog并显示异常详细信息
                    JOptionPane.showMessageDialog(null, "Error", "错误", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            }
        }
    }
}
