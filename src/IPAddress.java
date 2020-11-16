import java.io.Serializable;
import java.util.Objects;

//Done!
public class IPAddress implements Serializable {

    private Short bytes[];
    private String string;

    public IPAddress(String string) {
        bytes = new Short[4];
        this.string = string;
        String[] temp = string.split("\\.");
        for (int i = 0; i < 4; i++) {
            bytes[i] = Short.parseShort(temp[i]);
        }

    }

    public Short[] getBytes()
    {
        return bytes;
    }

    public String getString()
    {
        return string;
    }

    @Override
    public String toString() { return string; }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null || (this.getClass()!= obj.getClass()))
            return false;
        IPAddress ipAddress = (IPAddress)obj;
        return (obj instanceof IPAddress && ((IPAddress)obj).getString().equals(this.getString()));



    }

    @Override
    public int hashCode() {
//        return super.hashCode();
//        return Objects.hash(string);
//        return new HashCodeBuilder(17, 37)
//                .append(string)
//                .toHashCode();
        int result = 17;
        result = 31 * result + string.hashCode();
        return result;
    }
}
