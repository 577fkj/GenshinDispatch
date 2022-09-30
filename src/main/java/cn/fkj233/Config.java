package cn.fkj233;

public class Config {
    public String bindAddress = "0.0.0.0";
    public int bindPort = 443;
    public String accessAddress = "127.0.0.1";
    public int accessPort = 0;
    public boolean useEncryption = true;
    public String keystore = "./keystore.p12";
    public String keystorePassword = "123456";

    public Region[] regions = {new Region()};

    public String defaultName = "Grasscutter";

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
    }
}
