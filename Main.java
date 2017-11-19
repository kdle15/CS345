import java.io.IOException;

public class Main {
    public static void main(String args[]){
        SupplierTable s = SupplierTableLinkedList.getInstance();
        try {
            s.insert(new Supplier(10, "ACME",20, "Canton"));
            s.insert(new Supplier(5, "Whites",30, "Postdam"));
            s.insert(new Supplier(20, "REX",30, "Syracuse"));
            s.insert(new Supplier(15, "Aubuchon",30, "Massena"));
            //s.insert(new Supplier(15, "Aubuchon",30, "Massena"))
            //s.update(new Supplier(21, "ACME",10000, "HANOI"));
            s.delete(20);
            s.delete(5);
            s.delete(10);
            s.delete(15);
            s.delete(20);

            s.selectAll();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
