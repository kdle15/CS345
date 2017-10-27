public interface SupplierTable{
    void insert(Supplier s);
    void update(Supplier s);
    void delete(int sno);

    //print table to the console
    void selectAll();

    Supplier get(int sno);

}
