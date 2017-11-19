public class Supplier {
    private int sno;
    private String name;
    private int status;
    private String location;

    public static final int NAMELEN = 10;
    public static final int LOCATIONLEN = 10;

    public Supplier(int sno, String name, int status, String location){
        this.sno = sno;
        StringBuilder sb = new StringBuilder(name);
        sb.setLength(NAMELEN);
        this.name = sb.toString();
        this.status = status;
        sb = new StringBuilder(location);
        sb.setLength(LOCATIONLEN);
        this.location = sb.toString();
    }

    public int getSno() {
        return sno;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }
}
