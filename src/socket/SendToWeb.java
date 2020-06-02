package socket;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SendToWeb {
    //将读到的信息以字符串格式，通过tcp协议发给服务器。将IP：127.0.0.1和端口12345改成自己的ip和端口
    public void sendMessage(String message) {
        try {
            Socket s = new Socket("119.23.181.223",12345);

            //构建IO输入输出流为socket传送的数据
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();
            //将输出流新建为写入流
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            //向服务器端发送一条消息
            bw.write(message);
            bw.flush();

            //读取服务器返回的消息
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//            String mess = br.readLine();
//            System.out.println("服务器："+mess);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

