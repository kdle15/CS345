import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class SupplierTableLinkedList implements SupplierTable{

    private RandomAccessFile f = null;
    private static final int FREE_LIST_HEAD_SIZE = Long.SIZE/Byte.SIZE;
    private static final int LIST_HEAD_SIZE = Long.SIZE/Byte.SIZE;
    private static final int FILE_HEADER_SIZE = FREE_LIST_HEAD_SIZE + LIST_HEAD_SIZE;
    private static int LIST_HEAD_LOC = 0;
    private static int FREE_LIST_HEAD_LOC = LIST_HEAD_LOC + LIST_HEAD_SIZE;

    private static final SupplierTableLinkedList instance =
            new SupplierTableLinkedList();

    //getInstance
    public static SupplierTableLinkedList getInstance(){
        return instance;
    }
    //static {instance = new SupplerTableLinkedList();}
    //we should only have one
    //Singleton
    private SupplierTableLinkedList(){
        try{
            f = new RandomAccessFile("Suppler.dat","rw");
        }catch (FileNotFoundException e){

        }
    }

    @Override
    public void insert(Supplier s) {

    }

    @Override
    public void update(Supplier s) {

    }

    @Override
    public void delete(int sno) {

    }

    @Override
    public void selectAll() {

    }

    @Override
    public Supplier get(int sno) {
        return null;
    }
}
