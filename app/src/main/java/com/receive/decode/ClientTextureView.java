package com.receive.decode;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

import nc.NCUtils;

public class ClientTextureView extends TextureView implements  TextureView.SurfaceTextureListener{
    private static  final String MIME_TYPE = "video/avc";
    private static final String TAG = "ClientTextureView" ;
    private MediaCodec decode;
    private byte[] h264Buffer = new byte[60000];
    private int h264Len = 0;
    private byte[] rtpData =new byte[1500];
    private byte[] h264=new byte[60000];
    private byte[] fuben=new byte[50000];
    private byte[] resr=new byte[3000];
    private byte[] res=new byte[1500];
    private byte[] res2=new byte[1500];
    private byte[] ren=new byte[200];
    long presentationTimeUs = 0;
    private int flagn=101;
    private int c=0;
    private boolean flag=false;
//    long starttime = 0;
//    long sumt = 0;
    DatagramSocket socket;

    private DatagramSocket mSocket;
    private String ipa="192.168.43.255";
    private InetAddress mInetAddress;
    private int mPort=5004;

    int cc=0;
    int ccz=0;
    int count1=0;
    int k=0;
    int n=-1;
    int rank=0;
    int fu_header_len = 12;         // FU-Header长度为12字节
    byte[] temp1=new byte[2000];
    byte[] temp2=new byte[2000];
    private byte[] result=new byte[60000];
    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    ExecutorService fixThreadPool = Executors.newFixedThreadPool(30 );
    ExecutorService cacheThreadPool = Executors.newCachedThreadPool();

//    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testD.h264";
//    private BufferedOutputStream outputStream;

    Runnable task =new Runnable(){
        public void run(){
            if (h264Buffer != null) {
                offerDecoder(h264Buffer,h264Len);
                h264Buffer = null;
            }
        }
    };
    Runnable task1 =new Runnable(){
        public void run(){
//            try {
//                outputStream.write(result,0,h264Len);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            offerDecoder(result,h264Len);
        }
    };
    Runnable task2 =new Runnable(){
        public void run(){
            try {
                sendPacket(res,0,1400+k+16);//原封不动的发送出去
                sendPacket(res2,0,1400+k+16);//原封不动的发送出去
//                Log.e( "广播", "线性混合后的数据！");
            }catch (Exception e){
            }

        }
    };
    public ClientTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this);
//        createfile();
        Random random = new Random();
        random.nextBytes(ren);
        try {
            socket = new DatagramSocket(5004);//端口号
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            mInetAddress = InetAddress.getByName(ipa);
            mSocket = new DatagramSocket();
            mSocket.setBroadcast(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        new PreviewThread(new Surface(surface),800,480);//手机的分辨率
    }
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (socket != null){
            socket.close();
            socket = null;
        }
//        try {
//            outputStream.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            outputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return false;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
    private  class  PreviewThread extends  Thread {
        DatagramPacket datagramPacket = null;
        public PreviewThread(Surface surface, int width , int height){
            decode = MediaCodec.createDecoderByType(MIME_TYPE);
            final MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE,width,height);
            byte[] header_sps = {0,0,1,103,66,(byte) 0x80,30, (byte) 0xda,3,32, (byte)0xf6, (byte)0x80,109,10,19,80};
            byte[] header_pps = {0, 0, 0, 1, 104, (byte) 0xce, 6, (byte)0xe2};
            format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
//            try {
//                outputStream.write(header_sps,0,header_sps.length);
//                outputStream.write(header_pps,0,header_pps.length);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            decode.configure(format,surface,null,0);
            decode.start();
            start();
        }
        @Override
        public void run() {
            while (true) {
                byte[] data = new byte[1500];
                if (socket != null) {
                    try {
                        datagramPacket = new DatagramPacket(data,data.length);
                        socket.receive(datagramPacket);//接收数据
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//                cc++;
                rtpData = datagramPacket.getData();
//                starttime=System.currentTimeMillis();
                //不能进行再编码使用rtp2h2644(rtpData); 使用再编码使用rtp2h2645(rtpData);
                rtp2h2644(rtpData);
//                rtp2h2645(rtpData);
//                sumt+=System.currentTimeMillis()-starttime;
//                Log.e( "解码时间", sumt+"ms");
//                h264Buffer=rtp2h2642(rtpData,data.length);
//                fixThreadPool.execute(task);
//                singleThreadExecutor.execute(task);
//                cacheThreadPool.execute(task1);
            }
        }
    }
    //解码h264数据
    private void offerDecoder(byte[] input, int length) {
         Log.d(TAG, "offerDecoder: ");
        try {
            ByteBuffer[] inputBuffers = decode.getInputBuffers();
            int inputBufferIndex = decode.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                try{
                    inputBuffer.put(input, 0, length);
                }catch (Exception e){
                    e.printStackTrace();
                }
                long pts = computePresentationTime(presentationTimeUs);
                decode.queueInputBuffer(inputBufferIndex, 0, length,pts,0);
                presentationTimeUs++;
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = decode.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                decode.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = decode.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    public void rtp2h2645(byte[] rtpData) {
        // 解析FU-indicator
        byte indicatorType = (byte) (byteToInt(rtpData[fu_header_len]) & 0x1f); // 取出low 5 bit 则为FU-indicator type
        if (indicatorType == 28||indicatorType == 29) {  // FU-A
            if (n == -1) {
                System.arraycopy(rtpData, 0, res, 0, 14);
                k = (int) rtpData[15];
                flag=true;
                if(k<=0||k>30||k==flagn)return;
                n = (int) rtpData[14];
                count1 = 1;
                h264Len = k * 1400;
                System.arraycopy(rtpData, 16, temp1, 0, k);
                System.arraycopy(rtpData, 16 + k, h264, 0, 1400); // 负载数据
                System.arraycopy(rtpData, 16, fuben, 0, k + 1400);//将数据复制 进入副本
                if (count1 == k) {
                    temp2 = NCUtils.InverseMatrix(temp1, k);
                    if (temp2 != null) {
                        NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                        ccz++;
                        MainActivity.handle1(ccz, cc);
//                           offerDecoder(result,h264Len);
//                            cacheThreadPool.execute(task1);
//                            fixThreadPool.execute(task1);
                        singleThreadExecutor.execute(task1);
                    }
                }
                return;
            } else {
                 c =(int) rtpData[14];
                 if(c<n&&n!=99)return;
                int f =(int)rtpData[15];
                if(f==flagn){flag=false;}
                if (n == c) {
                    if (count1 >=k) {
                        return;
                    }
                    else {
                        count1++;
                        System.arraycopy(rtpData, 16, temp1,(count1-1)*k,k);
                        System.arraycopy(rtpData, 16 + k, h264, (count1 - 1) * 1400, 1400); // 负载数据
                        System.arraycopy(rtpData, 16, fuben, (count1 - 1) * (k + 1400), k + 1400);//将数据复制 进入副本
                        if(count1==k) {
                            if (flag == true) {
                                NCUtils.Multiply2(ren, 2, count1, fuben, count1, 1400 + k, resr);
                                res[14] = (byte) n;
                                res[15] = (byte) flagn;
                                System.arraycopy(res, 0, res2, 0, 16);
                                System.arraycopy(resr, 0, res, 16, 1400 + k);
                                System.arraycopy(resr, 1400 + k, res2, 16, 1400 + k);
                                singleThreadExecutor.execute(task2);
//                                fixThreadPool.execute(task2);
//                                cacheThreadPool.execute(task2);
                            }
                            temp2 = NCUtils.InverseMatrix(temp1, k);
                            if (temp2 != null) {
                                NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                                ccz++;
                                MainActivity.handle1(ccz, cc);
//                                   offerDecoder(result,h264Len);
//                                   cacheThreadPool.execute(task1);
//                                     fixThreadPool.execute(task1);
                                singleThreadExecutor.execute(task1);
                            }
                        }
                        return;
                    }
                }
                else {
                    k = (int) rtpData[15];
                    flag=true;
                    if(k<=0||k>30||k==flagn)
                    {
                        n=-1;
                        return;
                    }
                    n = c;
                    count1 = 1;
                    h264Len = k * 1400;
                    System.arraycopy(rtpData, 16, temp1, 0, k);
                    System.arraycopy(rtpData, 16 + k, h264, 0, 1400); // 负载数据
                    System.arraycopy(rtpData, 16, fuben, 0, k + 1400);//将数据复制 进入副本
                    if (count1 == k) {
                        temp2 = NCUtils.InverseMatrix(temp1, k);
                        if (temp2 != null) {
                            NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                            ccz++;
                            MainActivity.handle1(ccz, cc);
//                            offerDecoder(result,h264Len);
//                            cacheThreadPool.execute(task1);
//                             fixThreadPool.execute(task1);
                            singleThreadExecutor.execute(task1);
                        }
                    }
                    return;
                }
            }
        }
        return;
    }
    public void rtp2h2644(byte[] rtpData) {//不进行再编码操作时使用这个
        // 解析FU-indicator
        byte indicatorType = (byte) (byteToInt(rtpData[fu_header_len]) & 0x1f); // 取出low 5 bit 则为FU-indicator type
//        Log.e("李玉龙", indicatorType+"成功接收//");
        if (indicatorType == 28||indicatorType == 29) {  // FU-A
            if (n == -1) {
                n = (int) rtpData[14];
                k = (int) rtpData[15];
                if(k<=0||k>30)return;
                count1 = 1;
                h264Len = k * 1400;
                System.arraycopy(rtpData, 16, temp1, 0, k);
                System.arraycopy(rtpData, 16 + k, h264, 0, 1400); // 负载数据
                if (count1==k) {
                    temp2 = NCUtils.InverseMatrix(temp1, k);
                    if (temp2 != null) {
                        NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                        ccz++;
                        MainActivity.handle1(ccz, cc);
//                        offerDecoder(result,h264Len);
//                        cacheThreadPool.execute(task1);
                        singleThreadExecutor.execute(task1);
//                        fixThreadPool.execute(task1);
                    }
                }
            } else {
                int c =(int) rtpData[14];
                if(c<n&&n!=99)return;
                if (n == c) {
                    if (count1 >=k) {
                        return;
                    }
                    else {
                        count1++;
                        System.arraycopy(rtpData, 16, temp1,(count1-1)*k,k);
                        System.arraycopy(rtpData, 16 + k, h264, (count1 - 1) * 1400, 1400); // 负载数据
                        if(count1==k) {
                            temp2 = NCUtils.InverseMatrix(temp1, k);
                            if (temp2 != null) {
                                NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                                ccz++;
                                MainActivity.handle1(ccz, cc);
//                                offerDecoder(result, h264Len);
//                                cacheThreadPool.execute(task1);
                                singleThreadExecutor.execute(task1);
//                                fixThreadPool.execute(task1);
                            }
                        }
                    }
                }
                else {
                    n = c;
                    k = (int) rtpData[15];
                    if(k<=0||k>30)
                    {
                        n=-1;
                        return;
                    }
                    count1 = 1;
                    h264Len = k * 1400;
                    System.arraycopy(rtpData, 16, temp1, 0, k);
                    System.arraycopy(rtpData, 16 + k, h264, 0, 1400); // 负载数据
                    if (count1==k) {
                        temp2 = NCUtils.InverseMatrix(temp1, k);
                        if (temp2 != null) {
                            NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                            ccz++;
                            MainActivity.handle1(ccz, cc);
//                          offerDecoder(result,h264Len);
//                            cacheThreadPool.execute(task1);
                            singleThreadExecutor.execute(task1);
//                            fixThreadPool.execute(task1);
                            }
                        }
                    }
                }
            }

        }
    public void rtp2h2643(byte[] rtpData) {
        // 解析FU-indicator
        byte indicatorType = (byte) (byteToInt(rtpData[fu_header_len]) & 0x1f); // 取出low 5 bit 则为FU-indicator type
//        Log.e("李玉龙", indicatorType+"成功接收//");
        if (indicatorType == 28||indicatorType == 29) {  // FU-A
            if (n == -1) {
                System.arraycopy(rtpData, 0, res, 0, 14);
                k = (int) rtpData[15];
                if(k<0||k>50)return;
                    n = (int) rtpData[14];
                    count1 = 1;
                    h264Len = k * 1400;
                    System.arraycopy(rtpData, 16, temp1, 0, k);
                    System.arraycopy(rtpData, 16 + k, h264, 0, 1400); // 负载数据
                    System.arraycopy(rtpData, 16, fuben, 0, k + 1400);//将数据复制 进入副本
                    if (count1 == k) {
                        temp2 = NCUtils.InverseMatrix(temp1, k);
                        if (temp2 != null) {
                            NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                            ccz++;
                            MainActivity.handle1(ccz, cc);
//                           offerDecoder(result,h264Len);
//                            cacheThreadPool.execute(task1);
//                            fixThreadPool.execute(task1);
                        singleThreadExecutor.execute(task1);
                        }
                    }
                    return;
            } else {
                int c =(int) rtpData[14];
                if (n == c) {
                    if (count1 >=k) {
                        return;
                    }
                    else {
                        count1++;
                        System.arraycopy(rtpData, 16, temp1,(count1-1)*k,k);
                        rank=NCUtils.getRank1(temp1,count1,k);
                            if(count1==rank) {
                            System.arraycopy(rtpData, 16 + k, h264, (count1 - 1) * 1400, 1400); // 负载数据
                            System.arraycopy(rtpData, 16, fuben, (count1 - 1) * (k + 1400), k + 1400);//将数据复制 进入副本
                            if(count1==k)
                            {
                                    NCUtils.Multiply2(ren, 1, count1, fuben, count1, 1400 + k, resr);
                                    res[14] = (byte) n;
                                    res[15] = (byte) k;
                                    System.arraycopy(resr, 0, res, 16, 1400 + k);
//                                  sendPacket(res,0,1400+k+16);//原封不动的发送出去
                                    singleThreadExecutor.execute(task2);

//                                fixThreadPool.execute(task2);
//                                cacheThreadPool.execute(task2);
                                // 还是在线程中发送 好些
                                // 主要优化这里  应该试下更多数的数据在编码  效果会强一些？
                                //或者去掉线程 同时发送端需要进行优化处理
                                //fixThreadPool.execute(task2);这个固定线程池更好一些
                                temp2 = NCUtils.InverseMatrix(temp1, k);
                                if (temp2 != null) {
                                    NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                                    ccz++;
                                    MainActivity.handle1(ccz, cc);
//                                   offerDecoder(result,h264Len);
//                                   cacheThreadPool.execute(task1);
//                                     fixThreadPool.execute(task1);
                                  singleThreadExecutor.execute(task1);
                                }
                            }
                        }
                        else {
                            count1--;
                            return;
                        }
                        return;
                    }
                }
                else {
                    n = c;
                    k = (int) rtpData[15];
                    if(k<0||k>50)
                    {
                        n=-1;
                        return;
                    }
                        count1 = 1;
                        h264Len = k * 1400;
                        System.arraycopy(rtpData, 16, temp1, 0, k);
                        System.arraycopy(rtpData, 16 + k, h264, 0, 1400); // 负载数据
                        System.arraycopy(rtpData, 16, fuben, 0, k + 1400);//将数据复制 进入副本
                        if (count1 == k) {
                            temp2 = NCUtils.InverseMatrix(temp1, k);
                            if (temp2 != null) {
                                NCUtils.Multiply2(temp2, k, k, h264, k, 1400, result);
                                ccz++;
                                MainActivity.handle1(ccz, cc);
//                            offerDecoder(result,h264Len);
//                            cacheThreadPool.execute(task1);
//                             fixThreadPool.execute(task1);
                            singleThreadExecutor.execute(task1);
                            }
                        }
                    return;
                }
            }
        }
        return;
    }

    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 /25;
    }

    public void sendPacket(final byte[] data,final int offset, final int size) {
        try{
            DatagramPacket p;
            p = new DatagramPacket(data, offset, size, mInetAddress, mPort);
            mSocket.send(p);
//            Log.e("李玉龙","成功发送//");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    private void createfile(){
//        File file = new File(path);
//        if(file.exists()){
//            file.delete();
//        }
//        try {
//            outputStream = new BufferedOutputStream(new FileOutputStream(file));
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }

}
