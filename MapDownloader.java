package org.qiuqiu.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Random;

import org.qiuqiu.vo.BaiduPoint;

/**
 * @author Administrator
 * <ul>
 * <li>comment:</li>
 * <li>param type: street, satellite</li>
 * <ul>
 */
public class MapDownloader {
    
    private static int downloadImgNum = 0;
    
    private static int failedImgNum = 0;
    
    private void init(BaiduPoint startPoint, BaiduPoint endPoint, int startLevel, int endLevel, String fileURL,
            String type) {
        for (int i = startLevel; i < endLevel; i++) {
            int startBlockX = getBlockNum(startPoint.getLng(), i);
            int startBlockY = getBlockNum(startPoint.getLat(), i);
            int endBlockX = getBlockNum(endPoint.getLng(), i);
            int endBlockY = getBlockNum(endPoint.getLat(), i);
            if (startBlockX == endBlockX && startBlockY == endBlockY) {
                makeImg(startBlockX, endBlockX, i, fileURL, type);
            } else if (startBlockX == endBlockX && startBlockY > endBlockY) {
                for (int j = endBlockY; j < startBlockY; j++) {
                    makeImg(startBlockX, j, i, fileURL, type);
                }
            } else if (startBlockX < endBlockX && startBlockY == endBlockY) {
                for (int j = startBlockX; j < endBlockX; j++) {
                    makeImg(j, startBlockY, i, fileURL, type);
                }
            } else if (startBlockX < endBlockX && startBlockY > endBlockY) {
                for (int j = startBlockY; j > endBlockY; j--) {
                    for (int k = startBlockX; k < endBlockX; k++) {
                        makeImg(k, j, i, fileURL, type);
                    }
                }
            }
        }
        System.out.println("已下载 " + downloadImgNum + " 个文件，失败 " + failedImgNum + " 个");
        // for (int i = startLevel; i < endLevel; i++) {
        // for (double j = startPoint.getLng(); j <= endPoint.getLng(); j += 5) {
        // for (double k = endPoint.getLat(); k <= startPoint.getLat(); k += 5) {
        // int y = mercatorProjection(j, i);
        // int x = mercatorProjection(k, i);
        // System.out.println(j + ", " + k);
        // System.out.println(x + ", " + y + ", " + i);
        // startPoint.setLng(startPoint.getLng() + 1);
        // makeImg(x, y, i, fileURL);
        // }
        // startPoint.setLat(startPoint.getLat() + 1);
        // }
        // }
    }
    
    private int getBlockNum(double num, int level) {
        int mercator = (int) ((num / Math.pow(2, Math.abs(level - 18))) / 256);
        return (int) Math.ceil(mercator);
    }
    
    // 生成图片函数
    private void makeImg(int x, int y, int level, String fileURL, String type) {
        try {
            testFileURL(fileURL);
            fileURL = fileURL + "/" + type;
            testFileURL(fileURL);
            fileURL = fileURL + "/" + level;
            testFileURL(fileURL);
            fileURL = fileURL + "/" + x;
            testFileURL(fileURL);
            // 生成图片路径
            fileURL = fileURL + "/" + y + ".png";
            if (new File(fileURL).exists()) {
                System.out.println("图片已存在");
                return;
            } else {
                int server = new Random().nextInt(8) + 1;
                String url = null;
                if (type == "street" || type.equals("street")) {
                    url = "http://q" + server + ".baidu.com/it/u=x=" + x + ";y=" + y + ";z=" + level
                            + ";v=014;type=web&fm=44";
                } else if (type == "satellite" || type.equals("satellite")) {
                    url = "http://q" + server + ".baidu.com/it/u=x=" + x + ";y=" + y + ";z=" + level
                            + ";v=009;type=sate&fm=46";
                }
                System.out.println(url);
                download(url, fileURL);
                if (new File(fileURL).exists()) {
                    System.out.println("图片下载完成");
                    downloadImgNum++;
                } else {
                    System.out.println("下载失败");
                    failedImgNum++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void testFileURL(String url) {
        if (!(new File(url).isDirectory())) {
            new File(url).mkdir();
        }
    }
    
    public void download(String url, String fileURL) {
        try {
            // 创建流
            BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
            // 存放地址
            File img = new File(fileURL);
            // 生成图片
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(img));
            byte[] buf = new byte[2048];
            int length = in.read(buf);
            while (length != -1) {
                out.write(buf, 0, length);
                length = in.read(buf);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        BaiduPoint p1 = new BaiduPoint(9724758.79, 5450608.44);
        BaiduPoint p2 = new BaiduPoint(9770070.88, 5381232.41);
        MapDownloader mk = new MapDownloader();
        mk.init(p1, p2, 3, 19, "D:/map/baidu", "satellite");
    }
}