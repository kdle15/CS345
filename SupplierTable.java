import java.io.IOException;

public interface SupplierTable{
    void insert(Supplier s) throws IOException;
    void update(Supplier s) throws IOException;
    void delete(int sno) throws IOException;

    //print table to the console
    void selectAll() throws IOException;

    Supplier get(int sno);

}
