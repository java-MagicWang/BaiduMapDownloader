import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class MapDownloader {
    
    private static int downloadImgNum = 0;
    
    private static int failedImgNum = 0;
    
    private void init(BaiduPoint startPoint, BaiduPoint endPoint, int startLevel, int endLevel, String fileURL,
            String type) {
        checkFolder(fileURL);
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
    
    private void checkFolder(String fileURL) {
        Path path = Paths.get(fileURL);
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            System.out.println("文件夹存在");
        } else {
            System.out.println("创建文件夹");
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                int server = new Random().nextInt(3) + 1;
                String url = null;
                
                if (type == "street" || type.equals("street")) {
                    // old url = "http://q" + server + ".baidu.com/it/u=x=" + x + ";y=" + y + ";z=" + level +
                    // ";v=014;type=web&fm=44";
                    url = "http://or" + server + ".map.bdimg.com:8080/tile/?qt=tile&x=" + x + "&y=" + y + "&z=" + level
                            + "&styles=pl&udt=20130822";
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
            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(fileURL);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        BaiduPoint p1 = new BaiduPoint(9724758.79, 5450608.44);
        BaiduPoint p2 = new BaiduPoint(9770070.88, 5381232.41);
        MapDownloader mk = new MapDownloader();
        mk.init(p1, p2, 3, 19, "D:/map/baidu", "street");
    }
}
