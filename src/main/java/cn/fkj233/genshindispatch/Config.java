package cn.fkj233.genshindispatch;

public class Config {
    public String bindAddress = "0.0.0.0";
    public int bindPort = 443;
    public String accessAddress = "127.0.0.1";
    public int accessPort = 0;
    public boolean useEncryption = true;
    public String keystore = "./keystore.p12";
    public String keystorePassword = "123456";

    public Region[] regions = {new Region()};

    public static class Region {
        public Region() { }

        public Region(
                String name, String title,
                String address, int port, boolean run
        ) {
            this.Name = name;
            this.Title = title;
            this.Ip = address;
            this.Port  = port;
            this.Run = run;
        }

        public String Name = "os_usa";
        public String Title = "Grasscutter";
        public String Ip = "127.0.0.1";
        public int Port = 22102;
        public boolean Run = true;
        public StopServer StopServer = new StopServer();
        public VersionCheck VersionCheck = new VersionCheck();
    }

    public static class StopServer {
        public String Title = "停服维护";
        public String StartTime = "2022-10-01 00:00:00";
        public String StopTime = "2022-10-07 00:00:00";
        public String Msg = "更新";
        public String Url = "https://www.baidu.com";
    }

    public static class VersionCheck {
        public String Version = "3.1.0";
        public String Msg = "发现新版本，请点击下方按钮跳转下载最新客户端。 完成本次更新后登录即可获得300原石。";
        public String Url = "https://www.baidu.com";
    }
}
