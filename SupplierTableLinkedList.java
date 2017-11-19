import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SupplierTableLinkedList implements SupplierTable{

    private RandomAccessFile f = null;
    private static final int FREE_LIST_HEAD_SIZE = Long.SIZE/Byte.SIZE;
    private static final int LIST_HEAD_SIZE = Long.SIZE/Byte.SIZE;
    private static final int FILE_HEADER_SIZE = FREE_LIST_HEAD_SIZE + LIST_HEAD_SIZE;
    private static int LIST_HEAD_LOC = 0;
    private static int FREE_LIST_HEAD_LOC = LIST_HEAD_LOC + LIST_HEAD_SIZE;
    private static final int RECORD_SIZE = 56;
    private static final int RECORD_SIZE_WITHOUT_NEXT = RECORD_SIZE - Long.SIZE/Byte.SIZE;
    private static final int MAXCHAR = 15;
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
            f = new RandomAccessFile("Supplier.dat","rw");
        }catch (FileNotFoundException e){
            System.exit(1);
        }

        // file point is at 0
        try {
            f.writeLong(-1); // list head
            f.writeLong(-1); // free list head
        }
        catch (IOException e) {
            // print some nice error message
            // catastrophic
            System.exit(1);
        }

        // TODO Do I want to close the file?
    }

    @Override
    public void insert(Supplier s) throws IOException {
        long freepos = popFree();
        //freepos contains a valid position where s will get written
        f.seek(LIST_HEAD_LOC);
        long curr = f.readLong();

        //Speacial Case:
        //if the linked list is empty
        if(curr == -1){
            writerecord(s, freepos, -1);
            f.seek(LIST_HEAD_LOC);
            System.out.println("Byte address used to insert " + s.getSno() + " : " + freepos);
            // where we wrote the record
            f.writeLong(freepos);
            return;
        }

        //cur is along and it refers to the location of the head node in the list
        // free pos still refers to a valid free location
        f.seek(curr);
        int currsno = f.readInt();
        //if the add sno has smaller than head sno
        //add to the front of the linked list
        if (s.getSno() < currsno){
            //no worry about duplicate keys
            writerecord(s, freepos, curr);
            f.seek(LIST_HEAD_LOC);
            System.out.println("Byte address used to insert suppluere number" +
                    s.getSno() + " : " + freepos);
            f.writeLong(freepos);
            return;
        }

        //insert at the middele
        //add sno is bigger than head sno.
        long prev = -1;
        //curr refers to location of head and currsno reers to sno of head.
        while(curr != -1 && s.getSno() > currsno){
            //advance the file pointer to the next field
            f.seek(curr + RECORD_SIZE_WITHOUT_NEXT);
            prev = curr;
            curr = f.readLong();
            //avoid case when add to the last
            if (curr != -1) {
                f.seek(curr);
                currsno = f.readInt();
            }
        }

        if(s.getSno() == currsno){
            System.err.println("Insert supplier number " +
                    s.getSno() + " :Duplicate sno existed");
            return;
        }
        //write at freepos and set at curr
        writerecord(s, freepos, curr);
        f.seek(prev + RECORD_SIZE_WITHOUT_NEXT);
        System.out.println("Byte address used to insert supplier number " +
                s.getSno() + " : " + freepos);
        f.writeLong(freepos);
        return;
    }

    /**
     * Write the object s to the file at location pos
     * patching up the next field
     * @param s - the Supplier object to be written
     *          s != null
     * @param pos - the location where s is being written
     *            pos >= FILE_HEADER_SIZE
     *            (pos - FILE_HEADER_SIZE) % RECORD_SIZE == 0
     *            pos cant be beyond of the file
     * @param next - the location of the next record in the linked list
     *             next >= FILE_HEADER_SIZE
     *             (next - FILE_HEADER_SIZE) % RECORD_SIZE == 0 or
     *             next == -1
     *
     *             Location where next is written
     *             pos + (RECORD_SIZE - long.SIZE)
     *
     * post condition: file pointer is left in its orifinal postion
     */
    private void writerecord(Supplier s, long pos, long next) throws IOException{
        //assert precondition
        assert(s != null);
        assert(pos >= FILE_HEADER_SIZE);
        assert((pos - FILE_HEADER_SIZE)% RECORD_SIZE == 0);
        assert(((next - FILE_HEADER_SIZE) % RECORD_SIZE == 0)
                || next == -1);

        long tmp = f.getFilePointer();
        f.seek(pos);
        f.writeInt(s.getSno());
        f.writeChars(s.getName());
        f.writeInt(s.getStatus());
        f.writeChars(s.getLocation());
        f.writeLong(next);
        //pointer after the record
        f.seek(tmp);
    }

    public long popFree() throws IOException{
        f.seek(FREE_LIST_HEAD_LOC);
        long pos = f.readLong();
        //free list is empty -> free pos = -1 Last guys in the list
        if(pos == -1){
            return f.length();
        }
        else{
            f.seek(pos);
            long v = f.readLong();
            //about to use the free space so modify the free list
            f.seek(FREE_LIST_HEAD_LOC);
            f.writeLong(v);
            return pos;
        }
    }

    @Override
    public void update(Supplier s) throws IOException {
        //look for supplier
        f.seek(LIST_HEAD_LOC);
        long curr = f.readLong();
        //if the file is empty
        if(curr == -1){
            System.err.println("Update supplier number " + s.getSno() +
                    " :List is empty");
            return;
        }

        f.seek(curr);
        long firstcurr = f.getFilePointer();
        int currsno = f.readInt();
        long prev = -1;
        //long temp = curr;
        while(curr != -1 && s.getSno() != currsno){
            //advance the file pointer to the next field
            f.seek(curr + RECORD_SIZE_WITHOUT_NEXT);
            prev = curr;
            curr = f.readLong();
            if (curr != -1) {
                f.seek(curr);
                currsno = f.readInt();
            }
        }
        f.seek(curr+ RECORD_SIZE_WITHOUT_NEXT);
        long next = f.readLong();
        //if the curr advance to the last location but cant find matching sno
        if(curr == -1 && currsno != s.getSno()){
            System.err.println("Update supplier number " + s.getSno() +
                    " :No such sno existed to update");
            return;
        }
        //if we are going to update the first
        else if(curr == firstcurr && currsno == s.getSno()) {
            f.seek(LIST_HEAD_LOC);
        }
        //otherwise
        else{
            f.seek(prev + RECORD_SIZE_WITHOUT_NEXT);
        }
        writerecord(s, f.readLong(),next);
        return;
        //else
        //this.delete(s.getSno());
        //this.insert(s);

    }

    @Override
    public void delete(int sno) throws IOException {
        //List is empty
        f.seek(LIST_HEAD_LOC);
        long curr = f.readLong();
        if(curr == -1){
            System.err.println("Delete suppplier number " +
                    sno + " :List is empty");
            return;
        }

        //Linked List is not empty
        f.seek(curr);
        long firstcurr = f.getFilePointer();
        int currsno = f.readInt();
        long prev = -1;
        //long temp = curr;
        while(curr != -1 && sno != currsno){
            //advance the file pointer to the next field
            f.seek(curr + RECORD_SIZE_WITHOUT_NEXT);
            prev = curr;
            curr = f.readLong();
            if (curr != -1) {
                f.seek(curr);
                currsno = f.readInt();
            }
        }

        //record the next position after the delete position
        f.seek(curr+ RECORD_SIZE_WITHOUT_NEXT);
        long next = f.readLong();

        //delete first
        if (curr == firstcurr && currsno == sno){
            f.seek(LIST_HEAD_LOC);
        }
        else if(curr == -1 && currsno != sno){
            System.err.println("Delete supplier number " + sno + " :No such sno existed");
            return;
        }
        else{
            //delete node between 2 node
            f.seek(prev + RECORD_SIZE_WITHOUT_NEXT);
        }
        f.writeLong(next);
        f.seek(FREE_LIST_HEAD_LOC);
        System.out.println("Bytes address is free after delete " + sno + " : " + curr);
        //push it in the top of the free list
        long v = f.readLong();
        f.seek(curr);
        f.writeLong(v);
        f.seek(FREE_LIST_HEAD_LOC);
        f.writeLong(curr);
        return;
    }

    @Override
    public void selectAll() throws IOException{
        f.seek(LIST_HEAD_LOC);
        long curr = f.readLong();
        StringBuilder l = new StringBuilder();
        String ch = "Table is presented below: \n";
        String chi = "      SNO      |      Name      |     Status     |      Location      \n";
        String chii = "---------------|----------------|----------------|------------------\n";
        l.append(ch);
        if(curr == -1){
            l.append("Table is empty \n");
        }
        l.append(chi);
        l.append(chii);
        while(curr != -1){
            f.seek(curr);
            String k = Integer.toString(f.readInt());
            l.append(k);
            printString(l,k.length());
            //print Name
            StringBuilder t = new StringBuilder();
            for(int i = 0; i < Supplier.NAMELEN; i++){
                char q = f.readChar();
                if((int) q != 0){
                    t.append(q);
                }
            }
            l.append(t);
            printString(l,t.length());
            //print status
            String z = Integer.toString(f.readInt());
            l.append(z);
            printString(l,z.length());
            for(int i = 0; i < Supplier.LOCATIONLEN; i++){
                l.append(f.readChar());
            }
            l.append("\n");
            f.seek(curr + RECORD_SIZE_WITHOUT_NEXT);
            curr = f.readLong();
        }
        System.out.println(l.toString());
    }

    @Override
    public Supplier get(int sno) {
        return null;
    }

    private static void printString(StringBuilder l, int k){
        for (int i = 0; i < MAXCHAR - k; i++) {
            l.append(" ");
        }
        l.append("| ");
    }
}
