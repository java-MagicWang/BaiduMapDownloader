package com.wxd.download;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.wxd.bean.Position;

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
    
    private List<Position> cityList = new ArrayList<Position>();
    
    public static void main(String[] args) {
        MapDownloader mk = new MapDownloader();
        mk.start("无锡", 3, 19, "D:/map/baidu", "street");
    }
    
    private void start(String cityName, int startLevel, int endLevel, String fileURL, String type) {
        prepareList();
        List<Position> cpList = new ArrayList<Position>();
        for (Position p : cityList) {
            if (p.getNaem().equals(cityName)) {
                cpList.add(p);
            }
        }
        System.out.println("政区域数" + cpList.size());
        for (Position p : cpList) {
            getImgToDownload(p, startLevel, endLevel, fileURL, type);
        }
    }
    
    private void getImgToDownload(Position p, int startLevel, int endLevel, String fileURL, String type) {
        checkFolder(fileURL);
        for (int i = startLevel; i < endLevel; i++) {
            int startBlockX = getBlockNum(p.getSwlng(), i);
            int startBlockY = getBlockNum(p.getSwlat(), i);
            int endBlockX = getBlockNum(p.getNelng(), i);
            int endBlockY = getBlockNum(p.getNelat(), i);
            if (startBlockX == endBlockX && startBlockY == endBlockY) {
                makeImg(startBlockX, endBlockX, i, fileURL, type);
            } else if (startBlockX == endBlockX && startBlockY < endBlockY) {
                for (int j = startBlockY; j < endBlockY; j++) {
                    makeImg(startBlockX, j, i, fileURL, type);
                }
            } else if (startBlockX < endBlockX && startBlockY == endBlockY) {
                for (int j = startBlockX; j < endBlockX; j++) {
                    makeImg(j, startBlockY, i, fileURL, type);
                }
            } else if (startBlockX < endBlockX && startBlockY < endBlockY) {
                for (int j = startBlockY; j < endBlockY; j++) {
                    for (int k = startBlockX; k < endBlockX; k++) {
                        makeImg(k, j, i, fileURL, type);
                    }
                }
            }
        }
        System.out.println("已下载 " + downloadImgNum + " 个文件，失败 " + failedImgNum + " 个");
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
    
    private void prepareList() {
        cityList.add(new Position("北京", 12849780.03, 4758298.57, 13081648.69, 4993877.7));
        cityList.add(new Position("上海", 13454484.1, 3568800.28, 13581796.02, 3723920.55));
        cityList.add(new Position("天津", 12992707.8, 4632155.37, 13144152.71, 4875788.48));
        cityList.add(new Position("重庆", 11721675.29, 3249611.73, 12268313.26, 3767557.48));
        cityList.add(new Position("安徽", 12789537.39, 3405435.24, 13320133.59, 4092816.99));
        cityList.add(new Position("合肥", 12990475.48, 3676579.19, 13123586.88, 3811319.65));
        cityList.add(new Position("安庆", 12887481.9, 3454898.21, 13107862.27, 3646083.88));
        cityList.add(new Position("蚌埠", 12995822.55, 3834584.01, 13144929.84, 3939289.58));
        cityList.add(new Position("亳州", 12863348.24, 3854078.13, 13006628.34, 4014765.32));
        cityList.add(new Position("巢湖", 13028829.69, 3603211.52, 13192759, 3748793.1));
        cityList.add(new Position("池州", 12986120.06, 3426931.13, 13151204.87, 3591348.77));
        cityList.add(new Position("池州", 13053067.1, 3535823.22, 13061768.15, 3545996.32));
        cityList.add(new Position("滁州", 13043184.39, 3721827.62, 13273474.97, 3901278.64));
        cityList.add(new Position("阜阳", 12789537.38, 3794153.77, 12984610.55, 3950009.09));
        cityList.add(new Position("淮北", 12957826.12, 3909503.2, 13026290.91, 4019732.4));
        cityList.add(new Position("淮北", 13020083.78, 4021565.76, 13031750.41, 4038753.9));
        cityList.add(new Position("淮南", 12954547.28, 3791875.17, 13049194.48, 3873101.6));
        cityList.add(new Position("六安", 12843917.55, 3612450.62, 13053613.58, 3827971.98));
        cityList.add(new Position("宿州", 12931647.28, 3908884.14, 13157442.73, 4092816.99));
        cityList.add(new Position("铜陵", 13103319.94, 3578779.17, 13154980.09, 3627862.46));
        cityList.add(new Position("铜陵", 13053028.36, 3535885.51, 13061746.78, 3545874.59));
        cityList.add(new Position("芜湖", 13133024.87, 3564652.97, 13218213.7, 3679856.86));
        cityList.add(new Position("宣城", 13133269.55, 3475589.48, 13320133.59, 3650783.12));
        cityList.add(new Position("福建", 12897454.35, 2683181.03, 13407473.04, 3267816.61));
        cityList.add(new Position("福州", 13179389.05, 2883610.35, 13418530.19, 3059849.59));
        cityList.add(new Position("龙岩", 12897615.32, 2781549.12, 13109090.81, 2985037.64));
        cityList.add(new Position("南平", 13025619.21, 3012367.26, 13278487.07, 3268738.49));
        cityList.add(new Position("宁德", 13197601.45, 3016950.32, 13442670.19, 3188800.6));
        cityList.add(new Position("莆田", 13186983.57, 2850832.02, 13328047.05, 2950379.38));
        cityList.add(new Position("泉州", 13089640.52, 2773176.15, 13255318.23, 2972531.17));
        cityList.add(new Position("三明", 12956588.01, 2916694.44, 13211358.35, 3120762.57));
        cityList.add(new Position("厦门", 13123882.32, 2783184.25, 13187098.46, 2846886.47));
        cityList.add(new Position("漳州", 13014083.93, 2682034.37, 13163863.93, 2882029.41));
        cityList.add(new Position("甘肃", 10278113.51, 3819147.66, 12102965.62, 5252506.82));
        cityList.add(new Position("甘肃", 11788465.15, 4195802.26, 11807156.12, 4206259.08));
        cityList.add(new Position("兰州", 11422342.5, 4217541.74, 11642252.76, 4418836.3));
        cityList.add(new Position("兰州", 11501528.14, 4396344.48, 11533947.96, 4416771.5));
        cityList.add(new Position("白银", 11528560.04, 4195845.91, 11747728.12, 4502894.7));
        cityList.add(new Position("定西", 11518748.23, 4022153.92, 11761521.87, 4282042.76));
        cityList.add(new Position("甘南州", 11217907.82, 3886255.46, 11662869.69, 4220654.41));
        cityList.add(new Position("金昌", 11290711.56, 4557009.66, 11437171.08, 4695275.95));
        cityList.add(new Position("酒泉", 10278113.51, 4568365.95, 11155247.73, 5252506.82));
        cityList.add(new Position("临夏州", 11431805.09, 4132017.83, 11562370.36, 4304956.62));
        cityList.add(new Position("陇南", 11580714.01, 3819147.66, 11865719.16, 4076954.54));
        cityList.add(new Position("平凉", 11727845.47, 4124154.82, 12008123.16, 4241838.57));
        cityList.add(new Position("平凉", 11788465.15, 4195802.26, 11807156.12, 4206259.08));
        cityList.add(new Position("庆阳", 11841053.16, 4172671.15, 12103001.35, 4436112.37));
        cityList.add(new Position("天水", 11642375.88, 4015763.83, 11880236.27, 4164018.38));
        cityList.add(new Position("武威", 11336774.84, 4380067.5, 11601054.41, 4761251.56));
        cityList.add(new Position("张掖", 10842020.78, 4498627.15, 11379407.22, 4822272.6));
        cityList.add(new Position("广东", 12204811.93, 2266035.52, 13065234.46, 2921500.05));
        cityList.add(new Position("广东", 12639267.13, 2508287.37, 12647878.56, 2521703.55));
        cityList.add(new Position("广东", 12954958.72, 2303357.52, 13056457.16, 2380870));
        cityList.add(new Position("广州", 12575735.07, 2557788.85, 12697886.49, 2728067.86));
        cityList.add(new Position("潮州", 12954894.83, 2667280.35, 13048076.49, 2764816.4));
        cityList.add(new Position("东莞", 12638089.8, 2574520.97, 12720118.91, 2633102.76));
        cityList.add(new Position("佛山", 12512172.62, 2573265.14, 12623566.9, 2684829.08));
        cityList.add(new Position("河源", 12718215.32, 2635737.98, 12868594.57, 2831558.65));
        cityList.add(new Position("惠州", 12670519.19, 2540804.77, 12849216.97, 2731644.27));
        cityList.add(new Position("江门", 12469151.17, 2429749.14, 12609270.87, 2598244.64));
        cityList.add(new Position("揭阳", 12870889.44, 2597546.79, 12984731.22, 2709125.25));
        cityList.add(new Position("茂名", 12282216, 2417360.91, 12433414.56, 2580478.42));
        cityList.add(new Position("梅州", 12837695.5, 2660848.7, 13018737.14, 2850589.3));
        cityList.add(new Position("清远", 12460642.6, 2670650.76, 12681969.64, 2882964.54));
        cityList.add(new Position("汕头", 12941785.81, 2620828.27, 13065234.46, 2693730.75));
        cityList.add(new Position("汕尾", 12793512.76, 2569238.56, 12939239.8, 2672747.62));
        cityList.add(new Position("韶关", 12563536.99, 2723059.75, 12774252.39, 2921545.56));
        cityList.add(new Position("深圳", 12655329.26, 2524065.96, 12763882.42, 2598086.93));
        cityList.add(new Position("阳江", 12388729.2, 2432521.57, 12509767.64, 2577300.64));
        cityList.add(new Position("云浮", 12363611.58, 2541024.52, 12526651.32, 2654892.41));
        cityList.add(new Position("湛江", 12204811.93, 2266035.52, 12366346.39, 2486371.49));
        cityList.add(new Position("肇庆", 12397611.15, 2588200.85, 12566766.07, 2785085.04));
        cityList.add(new Position("中山", 12597605.13, 2517097.58, 12657143.12, 2589122.78));
        cityList.add(new Position("珠海", 12586773.18, 2469841.91, 12737069.71, 2550399.13));
        cityList.add(new Position("珠海", 12639267.13, 2508287.37, 12647878.56, 2521703.55));
        cityList.add(new Position("广西", 11628199.97, 2361555.36, 12475525.4, 3028485.6));
        cityList.add(new Position("南宁", 11948723.55, 2520266.1, 12203721.99, 2740475.24));
        cityList.add(new Position("百色", 11628281.68, 2598432.13, 12010475.04, 2870371.72));
        cityList.add(new Position("崇左", 11863081.97, 2447799.58, 12035866.62, 2658625.86));
        cityList.add(new Position("防城港", 11966009.99, 2434092.83, 12090967.59, 2540756.29));
        cityList.add(new Position("桂林", 12203513.59, 2767363.85, 12412145.62, 3028460.26));
        cityList.add(new Position("贵港", 12157883.91, 2574247.99, 12319764.58, 2741746.95));
        cityList.add(new Position("河池", 11864284.79, 2682529.8, 12152497.77, 2933018.53));
        cityList.add(new Position("贺州", 12309542.89, 2694512.71, 12475525.4, 2876770.74));
        cityList.add(new Position("来宾", 12069942.89, 2648285.66, 12297046.92, 2793323.49));
        cityList.add(new Position("柳州", 12088774.01, 2725212.58, 12265781.27, 2987215.34));
        cityList.add(new Position("钦州", 12043831.73, 2436918.66, 12231590.35, 2577726.62));
        cityList.add(new Position("梧州", 12282248.95, 2568690.63, 12431555.05, 2785670.59));
        cityList.add(new Position("玉林", 12194910.29, 2452521.12, 12346063.76, 2630903.18));
        cityList.add(new Position("贵州", 11533824.17, 2811720.2, 12196978.62, 3383484.41));
        cityList.add(new Position("贵州", 12194978.86, 3069800.37, 12200958.78, 3075203.89));
        cityList.add(new Position("贵阳", 11814609.16, 3003588.79, 11943156.07, 3148864.35));
        cityList.add(new Position("安顺", 11717065.62, 2901507.82, 11864290.81, 3057891.29));
        cityList.add(new Position("毕节地区", 11533784.43, 3025022.84, 11881631.57, 3201435.83));
        cityList.add(new Position("毕节地区", 11644468.6, 3071056.9, 11656455.83, 3086864.52));
        cityList.add(new Position("六盘水", 11612424.93, 2897648.89, 11769719.19, 3094721.5));
        cityList.add(new Position("六盘水", 11644468.6, 3071056.9, 11656455.83, 3086864.52));
        cityList.add(new Position("铜仁地区", 11995889.13, 3119682.92, 12187511.24, 3366386.06));
        cityList.add(new Position("遵义", 11757167.01, 3121156.11, 12047262.41, 3383484.41));
        cityList.add(new Position("遵义", 12015598.57, 3122722.29, 12018716.22, 3125413.85));
        cityList.add(new Position("黔西南州", 11637014.34, 2811713.26, 11859899.87, 3002420.55));
        cityList.add(new Position("黔东南州", 11945318.81, 2895734.08, 12196978.62, 3170377.47));
        cityList.add(new Position("黔东南州", 12015502.84, 3122698.53, 12018716.22, 3125331.83));
        cityList.add(new Position("黔东南州", 12194978.86, 3069800.37, 12200958.78, 3075203.89));
        cityList.add(new Position("黔南州", 11824220.14, 2865341.08, 12057171.4, 3164964.42));
        cityList.add(new Position("海南", 12409700.35, 906066.21, 12992596.92, 1317151.37));
        cityList.add(new Position("海南", 13068767.78, 1671999.42, 13143805.06, 1712911.09));
        cityList.add(new Position("海南", 12087085.28, 2038411.14, 12392639.68, 2288276.92));
        cityList.add(new Position("海南", 12641328.9, 1704958.81, 12815778.01, 1837117.15));
        cityList.add(new Position("海南", 12333087, 1743353.48, 12592970.63, 1947438.9));
        cityList.add(new Position("海南", 12449748.09, 397609.14, 12531830.18, 463341.68));
        cityList.add(new Position("海口", 12253739.05, 2202961.89, 12324943.33, 2280172.7));
        cityList.add(new Position("儋州", 12122660.37, 2162581.61, 12219905.46, 2253222.1));
        cityList.add(new Position("澄迈", 12215429.36, 2185324.39, 12275148.24, 2269522.45));
        cityList.add(new Position("东方", 12087085.28, 2108210.16, 12148675.24, 2184688.31));
        cityList.add(new Position("定安", 12259795.67, 2166388.23, 12307463.86, 2227020.45));
        cityList.add(new Position("琼海", 12259376.55, 2138803.45, 12326339.18, 2196983.39));
        cityList.add(new Position("临高", 12189689.57, 2208796.43, 12234541.01, 2263628.19));
        cityList.add(new Position("三亚", 12127107.74, 2038411.14, 12224936.37, 2097570.2));
        cityList.add(new Position("屯昌", 12226179.05, 2156934.88, 12274133.78, 2211123.69));
        cityList.add(new Position("万宁", 12247415.14, 2091636.82, 12314403.93, 2153852.07));
        cityList.add(new Position("文昌", 12297525.49, 2178586.52, 12392639.68, 2288276.92));
        cityList.add(new Position("河北", 12631437.27, 4282909.93, 13343965.35, 5225650.87));
        cityList.add(new Position("河北", 12998148.26, 4784262.48, 13054191.55, 4853107.22));
        cityList.add(new Position("石家庄", 12638208.02, 4475092.94, 12856434.67, 4661831.99));
        cityList.add(new Position("保定", 12665350.79, 4587220.42, 12951186.4, 4832225.52));
        cityList.add(new Position("沧州", 12880793.64, 4479580.82, 13131370.2, 4687632.8));
        cityList.add(new Position("承德", 12903726.26, 4867013.14, 13276059.06, 5225650.87));
        cityList.add(new Position("邯郸", 12631434.94, 4282892.66, 12856439.79, 4417106.37));
        cityList.add(new Position("衡水", 12823909.32, 4422243.11, 12976825.23, 4605899.62));
        cityList.add(new Position("廊坊", 12926947.09, 4620516.76, 13017224.96, 4784803.31));
        cityList.add(new Position("廊坊", 12998148.26, 4784262.48, 13054191.55, 4853107.22));
        cityList.add(new Position("秦皇岛", 13199886.31, 4756823.91, 13343965.35, 4928255.69));
        cityList.add(new Position("唐山", 13082245.15, 4681171.61, 13297827.38, 4906608.95));
        cityList.add(new Position("邢台", 12664646.24, 4379902.73, 12897564.22, 4526506.53));
        cityList.add(new Position("张家口", 12671878.53, 4775095.47, 12965762.95, 5155301.95));
        cityList.add(new Position("河南", 12286071.03, 3660864.94, 12986309.64, 4326464.96));
        cityList.add(new Position("河南", 12856585.84, 4261694.17, 12859146.11, 4264857.81));
        cityList.add(new Position("郑州", 12548946.52, 4040531.51, 12716120.05, 4138411.12));
        cityList.add(new Position("安阳", 12650465.99, 4167705.33, 12801376.56, 4326464.96));
        cityList.add(new Position("鹤壁", 12690831.99, 4200001.14, 12775652.69, 4282718.42));
        cityList.add(new Position("焦作", 12531944.95, 4114483.34, 12651968.71, 4207089.96));
        cityList.add(new Position("开封", 12677610.65, 4031891.71, 12832121, 4142988.38));
        cityList.add(new Position("洛阳", 12372854.09, 3947204.6, 12578234.88, 4149589.96));
        cityList.add(new Position("漯河", 12631364.58, 3926163.84, 12724151.05, 4003454.4));
        cityList.add(new Position("南阳", 12355412.58, 3776444.18, 12670661.23, 3979040.78));
        cityList.add(new Position("平顶山", 12496474.73, 3889600.05, 12656614.68, 4051815.54));
        cityList.add(new Position("濮阳", 12788646.71, 4184766.32, 12926227.74, 4304932.16));
        cityList.add(new Position("濮阳", 12856585.84, 4261694.17, 12859146.11, 4264857.81));
        cityList.add(new Position("三门峡", 12286071.03, 3945519.42, 12472112.08, 4151313.03));
        cityList.add(new Position("商丘", 12783323.68, 3966379.07, 12986309.64, 4121107.78));
        cityList.add(new Position("新乡", 12622490.21, 4123393.18, 12804700.93, 4254081.03));
        cityList.add(new Position("信阳", 12659024.55, 3660864.94, 12907666.35, 3827787.61));
        cityList.add(new Position("许昌", 12587257.09, 3964004.12, 12727459.95, 4059964.81));
        cityList.add(new Position("周口", 12701552.95, 3879639.11, 12875316.22, 4050620.69));
        cityList.add(new Position("驻马店", 12590848.41, 3777478.93, 12827034.2, 3942889.5));
        cityList.add(new Position("黑龙江", 13490905.67, 5348409.52, 15039836.95, 7054232.57));
        cityList.add(new Position("哈尔滨", 13992514.64, 5445872.94, 14499195.16, 5858675.06));
        cityList.add(new Position("大庆", 13777265.82, 5652172.63, 14003866.46, 5989382.85));
        cityList.add(new Position("大兴安岭地区", 13490853.8, 6571823.47, 14141757.63, 7054232.57));
        cityList.add(new Position("鹤岗", 14436077.55, 5921386.98, 14753490.16, 6134632.14));
        cityList.add(new Position("黑河", 13887889.21, 6007483.38, 14420950.4, 6589484.39));
        cityList.add(new Position("鸡西", 14516958.27, 5569834.63, 14912075.52, 5848536.94));
        cityList.add(new Position("佳木斯", 14417610.78, 5741589.43, 15039836.95, 6153248.05));
        cityList.add(new Position("牡丹江", 14275523.93, 5348409.52, 14619689.58, 5748261.41));
        cityList.add(new Position("七台河", 14484081.26, 5684727.64, 14687887.09, 5804083));
        cityList.add(new Position("齐齐哈尔", 13626428.72, 5786139.27, 14102308.03, 6232466.29));
        cityList.add(new Position("绥化", 13901920.74, 5671485.76, 14311629.24, 6091903.49));
        cityList.add(new Position("伊春", 14208755.89, 5827777.76, 14558968.25, 6318269.04));
        cityList.add(new Position("湖北", 12064304.05, 3359266.14, 12929154.47, 3908479.94));
        cityList.add(new Position("武汉", 12657945.07, 3478449.47, 12811591.57, 3658063.61));
        cityList.add(new Position("鄂州", 12747618.35, 3484832.76, 12812749.87, 3561573.36));
        cityList.add(new Position("恩施", 12064304.05, 3369959.41, 12317540.06, 3662709.24));
        cityList.add(new Position("黄冈", 12736044.23, 3447125.6, 12929154.47, 3689644.64));
        cityList.add(new Position("黄石", 12750480.29, 3419830.74, 12859489.09, 3524955.96));
        cityList.add(new Position("荆门", 12453082.36, 3532771.45, 12634540.12, 3690476.09));
        cityList.add(new Position("荆州", 12384453.26, 3409727.23, 12701228.9, 3567254.97));
        cityList.add(new Position("潜江", 12524632.52, 3492647.35, 12583690.15, 3565691.31));
        cityList.add(new Position("神农架林区", 12239344.5, 3649696.82, 12355288.91, 3723091.34));
        cityList.add(new Position("十堰", 12183159.83, 3676978.08, 12422480.63, 3908479.94));
        cityList.add(new Position("十堰", 12367133.32, 3741056.42, 12373714.6, 3747368.35));
        cityList.add(new Position("随州", 12549669.99, 3653567.47, 12706259.03, 3797789.86));
        cityList.add(new Position("天门", 12534612.31, 3531153.72, 12631998, 3598956.38));
        cityList.add(new Position("仙桃", 12573389.21, 3491702.39, 12671806.84, 3551001.39));
        cityList.add(new Position("咸宁", 12639828.07, 3359266.14, 12799671.24, 3520884.76));
        cityList.add(new Position("襄樊", 12331054.95, 3641093.03, 12593184.7, 3823455.32));
        cityList.add(new Position("襄樊", 12367316.11, 3741070.66, 12373714.6, 3747368.35));
        cityList.add(new Position("孝感", 12615222.41, 3531572.81, 12757176.34, 3722128.5));
        cityList.add(new Position("宜昌", 12274457.17, 3475774.83, 12478018.53, 3685686.18));
        cityList.add(new Position("湖南", 12111403.17, 2813884.72, 12720365.37, 3498932.17));
        cityList.add(new Position("湖南", 12194953.92, 3069800.37, 12200958.78, 3075203.89));
        cityList.add(new Position("长沙", 12457143.79, 3210426.01, 12720365.37, 3312691.71));
        cityList.add(new Position("常德", 12300724.95, 3280400.71, 12502694.66, 3498932.17));
        cityList.add(new Position("郴州", 12494256.33, 2845105.02, 12717684.33, 3084766.55));
        cityList.add(new Position("衡阳", 12417843.8, 2995041.87, 12611538.14, 3162763.26));
        cityList.add(new Position("怀化", 12111403.17, 2964391.48, 12369899.08, 3358151.3));
        cityList.add(new Position("怀化", 12194953.92, 3069800.37, 12200958.78, 3075203.89));
        cityList.add(new Position("娄底", 12331934.94, 3130279.23, 12526991.85, 3258868.42));
        cityList.add(new Position("邵阳", 12226234.04, 2976269.45, 12478773.73, 3187238.32));
        cityList.add(new Position("湘潭", 12468613.61, 3146766.51, 12590127.72, 3238741.09));
        cityList.add(new Position("湘西州", 12155458.56, 3194433.71, 12289473.66, 3435962.1));
        cityList.add(new Position("益阳", 12326280.74, 3226294.29, 12574413.25, 3422112.88));
        cityList.add(new Position("永州", 12351927.25, 2813884.72, 12519530.02, 3088178.95));
        cityList.add(new Position("岳阳", 12503731.28, 3282522.94, 12708729.24, 3463189.32));
        cityList.add(new Position("张家界", 12211924.65, 3338800.11, 12394889.47, 3456041.42));
        cityList.add(new Position("株洲", 12576262.28, 2986577.47, 12703067.36, 3233085.72));
        cityList.add(new Position("江苏", 12954229.79, 3580106.02, 13581761.74, 4156572.87));
        cityList.add(new Position("南京", 13176967.94, 3640988.49, 13274663.76, 3821558.1));
        cityList.add(new Position("常州", 13263571.77, 3631026.63, 13381681.92, 3748957.02));
        cityList.add(new Position("淮安", 13161063.21, 3835201.33, 13319697, 4017866.03));
        cityList.add(new Position("连云港", 13182036.81, 4002740.8, 13342598.45, 4156579.51));
        cityList.add(new Position("南通", 13381735.13, 3690921.36, 13581761.74, 3834536.61));
        cityList.add(new Position("苏州", 13350152.45, 3580106.02, 13513616.32, 3745748.53));
        cityList.add(new Position("宿迁", 13129948.41, 3890776.84, 13267507.85, 4062067.28));
        cityList.add(new Position("泰州", 13320019.83, 3731702.32, 13421453.25, 3901551.07));
        cityList.add(new Position("无锡", 13305727.03, 3626875, 13426468.91, 3739356.22));
        cityList.add(new Position("徐州", 12954229.79, 3966916.13, 13211616.5, 4131844.11));
        cityList.add(new Position("盐城", 13299029.64, 3816627.93, 13469968.81, 4078509.68));
        cityList.add(new Position("扬州", 13249831.34, 3769769.55, 13349388.79, 3927706.65));
        cityList.add(new Position("镇江", 13244692.56, 3692354.72, 13357140.83, 3782655.36));
        cityList.add(new Position("江西", 12644109.24, 2795594.61, 13190753.56, 3492343.75));
        cityList.add(new Position("南昌", 12851431.58, 3249211.17, 12976882.33, 3377512.18));
        cityList.add(new Position("抚州", 12868493.37, 3042033.62, 13059017.09, 3291486.29));
        cityList.add(new Position("赣州", 12681670.07, 2795594.61, 12985640.71, 3122470.32));
        cityList.add(new Position("吉安", 12672878.63, 2977517.45, 12906722.57, 3224668.71));
        cityList.add(new Position("景德镇", 13021139.66, 3319860.68, 13104348.55, 3473993.97));
        cityList.add(new Position("九江", 12684843.6, 3315729.07, 13014559.83, 3492315.08));
        cityList.add(new Position("萍乡", 12644109.24, 3099508.61, 12721927.98, 3230152.95));
        cityList.add(new Position("上饶", 12940169.55, 3204487.01, 13190753.56, 3444248.34));
        cityList.add(new Position("新余", 12745423.33, 3170682.15, 12847858.86, 3243010.24));
        cityList.add(new Position("宜春", 12679007.17, 3172639.93, 12930604.09, 3368121.15));
        cityList.add(new Position("鹰潭", 12991006.37, 3210245.04, 13077868.25, 3308488.31));
        cityList.add(new Position("吉林", 13988293.58, 5217424.68, 14251713.32, 5536780.59));
        cityList.add(new Position("长春", 13865002.46, 5323590.1, 14148960.42, 5631617.95));
        cityList.add(new Position("白城", 13542049.63, 5471532.33, 13845684.75, 5799148.39));
        cityList.add(new Position("白山", 14040572.68, 5038501, 14285212.28, 5256385.39));
        cityList.add(new Position("吉林市", 13988293.58, 5217424.68, 14251713.32, 5536780.59));
        cityList.add(new Position("辽源", 13898007.04, 5177774.69, 14009745.42, 5318881.11));
        cityList.add(new Position("四平", 13727238.63, 5256509.4, 14003110.59, 5460508.1));
        cityList.add(new Position("松原", 13705386.94, 5435075.22, 14048737.16, 5677439.9));
        cityList.add(new Position("通化", 13945197.78, 4964607.5, 14111100.13, 5286862.28));
        cityList.add(new Position("延边", 14190662.04, 5132787.01, 14619263.22, 5516016.73));
        cityList.add(new Position("辽宁", 13230751.64, 4649616.17, 14003844.2, 5358417.45));
        cityList.add(new Position("沈阳", 13629240.68, 5014350.49, 13783739.01, 5290030.79));
        cityList.add(new Position("鞍山", 13602737.12, 4839186.88, 13778710.77, 5068827.09));
        cityList.add(new Position("本溪", 13758769.44, 4958008.42, 14003897.3, 5068945.98));
        cityList.add(new Position("朝阳", 13230762.11, 4926403.23, 13503991.65, 5190367.65));
        cityList.add(new Position("大连", 13461674.64, 4649616.17, 13752706.03, 4868678.34));
        cityList.add(new Position("丹东", 13735472.36, 4793832.22, 13995107.19, 5008148.16));
        cityList.add(new Position("抚顺", 13767731.54, 5020112.9, 13970313.73, 5204489.64));
        cityList.add(new Position("阜新", 13472755.95, 5087291.92, 13690558.91, 5259786.15));
        cityList.add(new Position("葫芦岛", 13272022.46, 4832466.51, 13481796.08, 5015274.26));
        cityList.add(new Position("锦州", 13439072.08, 4945280.94, 13648679.73, 5152410.72));
        cityList.add(new Position("辽阳", 13647649.45, 4942125.61, 13769836.06, 5075223.29));
        cityList.add(new Position("盘锦", 13523527.43, 4932035.47, 13628658.48, 5051560.31));
        cityList.add(new Position("铁岭", 13743698.96, 5131040.1, 13928017.54, 5358417.45));
        cityList.add(new Position("营口", 13566201.77, 4827542.52, 13693972.67, 4976093.77));
        cityList.add(new Position("内蒙古", 10818075.47, 4470447.78, 14035610.67, 7011774.18));
        cityList.add(new Position("呼和浩特", 12303730.61, 4779716.47, 12503025.73, 5041774.5));
        cityList.add(new Position("阿拉善盟", 10818075.47, 4470447.78, 11897603.97, 5252506.82));
        cityList.add(new Position("包头", 12163058.54, 4874987.92, 12406921.79, 5243579.34));
        cityList.add(new Position("巴彦淖尔", 11711601.44, 4863791.7, 12235396.29, 5202571.45));
        cityList.add(new Position("赤峰", 12954916.87, 5026938.58, 13469044.85, 5630202.23));
        cityList.add(new Position("鄂尔多斯", 11853705.76, 4500035.24, 12408879.03, 4967758.89));
        cityList.add(new Position("呼伦贝尔", 12861487.29, 5927337.76, 14035610.67, 7011774.18));
        cityList.add(new Position("通辽", 13274418.97, 5169712.46, 13773350.23, 5700327.86));
        cityList.add(new Position("乌海", 11866502.75, 4700672.48, 11932190.2, 4825259.97));
        cityList.add(new Position("乌兰察布", 12283628.32, 4862833.96, 12781569.27, 5341253.52));
        cityList.add(new Position("锡林郭勒盟", 12374482.82, 5069933.32, 13372922.1, 5875332.68));
        cityList.add(new Position("兴安盟", 13301627.53, 5476186.93, 13764825.85, 6019170.77));
        cityList.add(new Position("宁夏", 11610753.23, 4172214.01, 11985178.85, 4751391.87));
        cityList.add(new Position("宁夏", 11788465.15, 4195802.26, 11807246.18, 4206340.75));
        cityList.add(new Position("银川", 11782747.28, 4482018.56, 11897627.01, 4676676.53));
        cityList.add(new Position("固原", 11725584.99, 4172214.01, 11907388.28, 4363021.99));
        cityList.add(new Position("固原", 11788465.15, 4195802.26, 11807246.18, 4206340.75));
        cityList.add(new Position("石嘴山", 11799171.31, 4640913, 11910724.23, 4751391.87));
        cityList.add(new Position("吴忠", 11755581.24, 4354781.37, 11985178.85, 4590714.83));
        cityList.add(new Position("中卫", 11610753.23, 4292753.59, 11819203.69, 4519083.94));
        cityList.add(new Position("青海", 9953867.03, 3689099.52, 11474926.83, 4727046.71));
        cityList.add(new Position("西宁", 11233421.36, 4307806.06, 11345918.61, 4470356.51));
        cityList.add(new Position("果洛州", 10796510.09, 3808461.06, 11330738.92, 4218993.78));
        cityList.add(new Position("海东地区", 11318357.5, 4200793.44, 11474926.83, 4434559.82));
        cityList.add(new Position("海北州", 10920558.31, 4377248.76, 11426912.66, 4708314.46));
        cityList.add(new Position("海南州", 11013940.57, 4091466.6, 11333079.66, 4447004.57));
        cityList.add(new Position("海西州", 10039145.37, 4140832.85, 11108882.99, 4727046.71));
        cityList.add(new Position("海西州", 9979513.54, 3835795.38, 10343970.95, 4132361.76));
        cityList.add(new Position("黄南州", 11209243.28, 4014246.3, 11406266.12, 4299398.15));
        cityList.add(new Position("玉树州", 9953867.03, 3689099.52, 10885226.68, 4314088.37));
        cityList.add(new Position("山东", 12782933.17, 4056085.99, 13662686.76, 4618802.14));
        cityList.add(new Position("山东", 12856585.84, 4261694.17, 12859146.11, 4264857.81));
        cityList.add(new Position("济南", 12938853.51, 4280134.29, 13108451.71, 4489105.12));
        cityList.add(new Position("滨州", 13054690.05, 4370783.44, 13178864.17, 4597271.11));
        cityList.add(new Position("东营", 13148336.36, 4405399.59, 13283347, 4576679.93));
        cityList.add(new Position("德州", 12887067.45, 4332222.43, 13092839.99, 4555071.15));
        cityList.add(new Position("菏泽", 12782933.17, 4079359.18, 12960004.89, 4257385.35));
        cityList.add(new Position("济宁", 12899189.05, 4065886.5, 13091751.72, 4273574.83));
        cityList.add(new Position("莱芜", 13062131.73, 4274781.58, 13133741.44, 4352121.98));
        cityList.add(new Position("聊城", 12833962.58, 4245857.82, 12974662.26, 4417147.88));
        cityList.add(new Position("聊城", 12856585.84, 4261694.17, 12859146.11, 4264857.81));
        cityList.add(new Position("临沂", 13071928.5, 4056085.99, 13270518.63, 4304452.48));
        cityList.add(new Position("青岛", 13304995.87, 4211575.51, 13515784.63, 4434367.2));
        cityList.add(new Position("日照", 13203486.64, 4134294.82, 13353875.46, 4281767.1));
        cityList.add(new Position("泰安", 12919305.6, 4225626.11, 13136851.18, 4341583.6));
        cityList.add(new Position("威海", 13490221.5, 4368831.31, 13662686.76, 4498957.54));
        cityList.add(new Position("潍坊", 13156294.57, 4236695.19, 13360454.73, 4460300.4));
        cityList.add(new Position("烟台", 13309659.43, 4353072.03, 13573707.79, 4618802.14));
        cityList.add(new Position("枣庄", 13004263.44, 4067481.89, 13117857.53, 4183285.71));
        cityList.add(new Position("淄博", 13086032.84, 4265566.89, 13194547.7, 4454232.75));
        cityList.add(new Position("山西", 12270624.5, 4083896.51, 12753907.97, 4947334.76));
        cityList.add(new Position("太原", 12414761.55, 4477543.17, 12597354.77, 4613071.62));
        cityList.add(new Position("长治", 12466586.57, 4251962.38, 12662207.43, 4431272.44));
        cityList.add(new Position("大同", 12533146.99, 4702342.43, 12753907.97, 4947334.76));
        cityList.add(new Position("晋城", 12461903.64, 4167494.97, 12649517.76, 4284844.26));
        cityList.add(new Position("晋中", 12402800.14, 4364687.35, 12706735.93, 4566716.17));
        cityList.add(new Position("临汾", 12287732.27, 4192354.69, 12532521.93, 4404887.45));
        cityList.add(new Position("吕梁", 12287844.15, 4376854.72, 12505293.22, 4657101.81));
        cityList.add(new Position("朔州", 12457309.32, 4708157.61, 12643693.68, 4882446.81));
        cityList.add(new Position("忻州", 12351264.33, 4572394.82, 12687599.9, 4790431.02));
        cityList.add(new Position("阳泉", 12571029.99, 4506299.25, 12696252.86, 4627705.5));
        cityList.add(new Position("运城", 12270624.5, 4083898.8, 12477750.14, 4251682.71));
        cityList.add(new Position("陕西", 11744111.76, 3702746.74, 12384710.75, 4779514.03));
        cityList.add(new Position("西安", 11981884.99, 3964654.15, 12226230.59, 4105288.31));
        cityList.add(new Position("安康", 12026235.15, 3702746.74, 12269009.46, 3984319.77));
        cityList.add(new Position("宝鸡", 11835274.51, 3949399.28, 12029720.49, 4154241.13));
        cityList.add(new Position("汉中", 11744283.86, 3760380.97, 12054988.07, 3989283.7));
        cityList.add(new Position("商洛", 12087463.21, 3885095.93, 12361433.92, 4063447.96));
        cityList.add(new Position("铜川", 12088312.92, 4113604.41, 12189868.68, 4218877.81));
        cityList.add(new Position("渭南", 12130716.64, 4033630.18, 12313489.1, 4256079.36));
        cityList.add(new Position("咸阳", 11984609.78, 4031675.07, 12154517.12, 4213826.27));
        cityList.add(new Position("延安", 11985526.06, 4186651.87, 12307509.95, 4483691.41));
        cityList.add(new Position("榆林", 11941399.2, 4388945.75, 12384710.75, 4779303.54));
        cityList.add(new Position("四川", 10838113.75, 2986533.97, 12084207.75, 4047359.43));
        cityList.add(new Position("成都", 11469367.59, 3494751.29, 11677450.47, 3667639.18));
        cityList.add(new Position("阿坝州", 11191486.63, 3558944.25, 11626760.7, 4047359.43));
        cityList.add(new Position("巴中", 11841716.3, 3643858.17, 11996838.81, 3838156.85));
        cityList.add(new Position("达州", 11874900.11, 3524432.9, 12084207.75, 3785513.7));
        cityList.add(new Position("德阳", 11555332.89, 3549960.81, 11708848.03, 3701927.98));
        cityList.add(new Position("甘孜州", 10838113.75, 3224547.1, 11410483.84, 4031723.71));
        cityList.add(new Position("广安", 11794888.69, 3485015.43, 11946996.41, 3592275.81));
        cityList.add(new Position("广元", 11646675.31, 3679400.3, 11886077.49, 3863720.27));
        cityList.add(new Position("乐山", 11456866.72, 3282255.36, 11606142.44, 3473629.59));
        cityList.add(new Position("凉山州", 11139614.74, 2986533.97, 11564329.49, 3393883.6));
        cityList.add(new Position("泸州", 11705550.71, 3186846.74, 11843096.57, 3396900.43));
        cityList.add(new Position("南充", 11738900.72, 3541461.13, 11908529.65, 3721444.31));
        cityList.add(new Position("眉山", 11450337.21, 3406322.68, 11634170.76, 3529139.28));
        cityList.add(new Position("绵阳", 11550122.97, 3573305.2, 11770233.27, 3878122.45));
        cityList.add(new Position("内江", 11608072.18, 3378899.78, 11739053.9, 3487837.25));
        cityList.add(new Position("攀枝花", 11259548.44, 2990979.4, 11382968.25, 3147334.32));
        cityList.add(new Position("遂宁", 11696014.54, 3505172.11, 11800842.79, 3632125.5));
        cityList.add(new Position("雅安", 11348373.45, 3336428, 11510760.08, 3603260.72));
        cityList.add(new Position("宜宾", 11534873.7, 3209500.13, 11728994.3, 3389231.15));
        cityList.add(new Position("资阳", 11599464.2, 3441209.59, 11773322.2, 3565314.97));
        cityList.add(new Position("自贡", 11583992.85, 3345872.23, 11719370.62, 3436407.2));
        cityList.add(new Position("西藏", 8727843.05, 3086017.6, 11033798.96, 4342740.32));
        cityList.add(new Position("拉萨", 9992141.99, 3385663.74, 10311363.56, 3618513.4));
        cityList.add(new Position("阿里地区", 8727843.05, 3441390.6, 9610897.86, 4257019.66));
        cityList.add(new Position("昌都地区", 10419735.04, 3284156.87, 11033798.96, 3818960.92));
        cityList.add(new Position("林芝地区", 10260423.88, 3174330.56, 10994041.11, 3568908.2));
        cityList.add(new Position("那曲地区", 9468850.53, 3474655.39, 10578770.58, 4342740.32));
        cityList.add(new Position("日喀则地区", 9144835.04, 3130768.42, 10058481, 3716081.04));
        cityList.add(new Position("山南地区", 10027095.13, 3086017.6, 10504892.15, 3460286.95));
        cityList.add(new Position("新疆", 8181798.98, 4050003.25, 10730822.64, 6273729.91));
        cityList.add(new Position("乌鲁木齐", 9662128.42, 5270859.24, 9906000.47, 5592219.04));
        cityList.add(new Position("阿拉尔", 8972185.87, 4891851.52, 9123961.52, 4977007.41));
        cityList.add(new Position("阿克苏地区", 8686910.21, 4762157.81, 9361652.92, 5230038.82));
        cityList.add(new Position("阿克苏地区", 8972185.87, 4891851.52, 9123961.52, 4977007.41));
        cityList.add(new Position("阿勒泰地区", 9521770.94, 5592219.04, 10139318.53, 6273729.91));
        cityList.add(new Position("巴音郭楞", 9181386.86, 4226843.45, 10450142.85, 5368965.84));
        cityList.add(new Position("博尔塔拉州", 8893601.58, 5437124.81, 9329944.22, 5653078.83));
        cityList.add(new Position("昌吉州", 9773086.33, 5345744.78, 10196222.22, 5668843.11));
        cityList.add(new Position("昌吉州", 9527666.26, 5298895.9, 9750123.99, 5644356.17));
        cityList.add(new Position("哈密地区", 10150705.08, 4948635.66, 10730822.64, 5606117.42));
        cityList.add(new Position("和田地区", 8617397.77, 4050003.25, 9454075.26, 4786182.37));
        cityList.add(new Position("喀什地区", 8289730.85, 4201158.73, 8891448.19, 4876496.1));
        cityList.add(new Position("喀什地区", 8756518.04, 4783639.2, 8862513.05, 4857851.76));
        cityList.add(new Position("克拉玛依", 9413099.9, 5541495.71, 9578289.8, 5784360.46));
        cityList.add(new Position("克拉玛依", 9431903.08, 5453177.45, 9475566.84, 5496855.11));
        cityList.add(new Position("克孜勒苏州", 8181798.98, 4504704.65, 8781619.08, 5043837.4));
        cityList.add(new Position("石河子", 9563250.15, 5455441.32, 9593248.41, 5508042.09));
        cityList.add(new Position("塔城地区", 9156886.06, 5356708.34, 9723062.26, 5949778.86));
        cityList.add(new Position("塔城地区", 9413137.41, 5453077.32, 9578314.18, 5784360.46));
        cityList.add(new Position("图木舒克", 8756518.04, 4783639.2, 8862340.94, 4857851.76));
        cityList.add(new Position("吐鲁番地区", 9714556.8, 5012740.03, 10232736.75, 5380227));
        cityList.add(new Position("五家渠", 9726569.75, 5437137.77, 9765472.66, 5528937.44));
        cityList.add(new Position("伊犁州", 8924626.32, 5171696.76, 9456988.42, 5567388.32));
        cityList.add(new Position("伊犁州", 9438627.96, 5489585.39, 9490921.97, 5564219.65));
        cityList.add(new Position("云南", 10857721.56, 2393774.36, 11822981.47, 3386968.96));
        cityList.add(new Position("昆明", 11374215.89, 2783319.89, 11541734.51, 3047940.43));
        cityList.add(new Position("保山", 10920144.87, 2751373.9, 11137987.9, 2963199.62));
        cityList.add(new Position("楚雄州", 11213131.16, 2763346.98, 11409116.6, 3042427.04));
        cityList.add(new Position("大理州", 11006373.44, 2816846.8, 11249273.2, 3066933.44));
        cityList.add(new Position("德宏州", 10857885.87, 2717112.8, 10991086.43, 2899331.83));
        cityList.add(new Position("迪庆州", 10976289.68, 3088082.75, 11167166.62, 3387037.37));
        cityList.add(new Position("红河州", 11333385.68, 2548820.52, 11609514.34, 2829457.53));
        cityList.add(new Position("丽江", 11063995.37, 2979088.98, 11301319.42, 3220020.52));
        cityList.add(new Position("怒江州", 10924864.31, 2925486.15, 11093103.62, 3279081.08));
        cityList.add(new Position("曲靖", 11472606.58, 2778803.18, 11670343.2, 3111043.37));
        cityList.add(new Position("昭通", 11452492.49, 3046917.77, 11724598.46, 3313477.27));
        cityList.add(new Position("文山", 11530657.54, 2578150.93, 11822981.47, 2792390.19));
        cityList.add(new Position("西双版纳", 11126674.27, 2393805.45, 11338093.77, 2566720.27));
        cityList.add(new Position("玉溪", 11274923.61, 2653633.78, 11483803.55, 2852475.44));
        cityList.add(new Position("浙江", 13139130.14, 3124220.58, 13597165.96, 3633652.49));
        cityList.add(new Position("浙江", 13617818.74, 3460251.46, 13629588.12, 3475911.6));
        cityList.add(new Position("浙江", 13612463.87, 3534593.33, 13630004.12, 3542768.3));
        cityList.add(new Position("浙江", 13563307.1, 3477623.32, 13573530.57, 3492890.68));
        cityList.add(new Position("浙江", 13590726.61, 3511620.64, 13608413.47, 3525120.35));
        cityList.add(new Position("浙江", 13567657.52, 3361593.28, 13579552.91, 3377746.21));
        cityList.add(new Position("浙江", 13586593.39, 3437493.7, 13604177.05, 3455095.63));
        cityList.add(new Position("浙江", 13574435.5, 3474037.55, 13619386.13, 3505312.02));
        cityList.add(new Position("杭州", 13174965.5, 3379425.23, 13439566.79, 3555379.53));
        cityList.add(new Position("湖州", 13274316.78, 3530637.21, 13413803.29, 3634464.94));
        cityList.add(new Position("嘉兴", 13392163.88, 3514264.36, 13530798.9, 3615258.63));
        cityList.add(new Position("金华", 13273358.08, 3295182.09, 13446447.08, 3442148.74));
        cityList.add(new Position("丽水", 13214392.18, 3156523.05, 13408063.48, 3349194.76));
        cityList.add(new Position("宁波", 13456881.95, 3335309.93, 13616290.3, 3540487.02));
        cityList.add(new Position("衢州", 13139724.48, 3259521.17, 13286427.8, 3418618.76));
        cityList.add(new Position("绍兴", 13346849.9, 3383986.06, 13496304.62, 3519472.14));
        cityList.add(new Position("台州", 13391487.61, 3229487.33, 13579594.55, 3398618.42));
        cityList.add(new Position("温州", 13317494.85, 3106318.51, 13512020.1, 3306358.35));
        cityList.add(new Position("舟山", 13528611.11, 3424814.02, 13692301.19, 3598734.46));
        cityList.add(new Position("香港", 12671487.79, 2512035.14, 12747437.34, 2563724.73));
        cityList.add(new Position("澳门", 12639267.13, 2508287.37, 12647856.41, 2521696.25));
    }
    
}
