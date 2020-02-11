import udtf.GenericFor;

public class TempTest {
    public static void main(String[] args) {
        try {
            GenericFor genericFor = new GenericFor();
            Class.forName("udtf.GenericFor");
            System.out.println("test over...");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
