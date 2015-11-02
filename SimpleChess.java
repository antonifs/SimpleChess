
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SimpleChess {

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        Castling castling = new Castling();

        /*
         * get the input and fill the board
         * */
        for (int i = 0; i < 8; i++) {
            String temp[] = in.readLine().split("(?!^)"); //split character tanpa spasi 
            for (int j = 0; j < 8; j++) {
                char c = temp[j].charAt(0);
                castling.fillBoard(i, j, c);	// mengisi board dengan data inputan
            }
        }
        
        String act = ""; // inisiasi langkah
        while ((act = in.readLine()) != null) {
            String[] cmd = act.split(" ");
            
            // jika mendapatkan perintah move di-handle disini
            if (cmd[0].equalsIgnoreCase("move")) {
                boolean status = castling.move(cmd[1], cmd[2]);
                if (!status) { // jika status false 
                    System.out.println("Invalid move");
                }
            } 
            
            // jika mendapatkan perintah mencetak log di-handle disini
            else if (cmd[0].equalsIgnoreCase("log")) {
                int no = 0;
                if (cmd.length > 1) {
                    no = Integer.parseInt(cmd[1]);
                }
                castling.cetakLog(no);
            } 
            
            // jika mendapatkan perintah mencetak path di-handle disini
            else if (cmd[0].equalsIgnoreCase("path")) {
                castling.printPath(cmd[1]);
            } 
            
            // jika mendapatkan perintah check/skak di-handle disini
            else if (cmd[0].equalsIgnoreCase("check")) {
                if (cmd.length > 1) {
                    Posisi pos = new Posisi(cmd[1]);
                    char bidak = castling.board[pos.xb][pos.yb];
                    
                    // jika bidak tidak yang diharapkan melakukan skak ternyata adalah "." atau bukan bidak sebenarnya
                    String tempBidak = bidak + "";
                    if (tempBidak.equals(tempBidak.toLowerCase()) || tempBidak.equalsIgnoreCase(".")) {
                        System.out.println("Position invalid");
                    } 
                    
                    else {              
                    	
                    	// jika tidak ada langkah yang dapat dilakukan
                        int langkah = 0;
                        if (langkah == 0) {
                            System.out.println("Unable to check");
                        } 
                        
                        /* langkah yang benar ada disini
                         * lakukan check dengan rekursif
                         */
                        else {
                            langkah = langkah-1;
                            System.out.println(bidak+" "+cmd[1]+" "+langkah);
                        }
                    }


                } 
                
                // Jika perintah tidak sesuai
                else {
                    System.out.println("Unable to check");
                }
            }

        }

    }
}

/*
 * Castling
 * */
class Castling {
	
    public Posisi kingPosition = null; 
    public char emptyMark = '.'; 
    static ArrayList<LogData> log = new ArrayList<LogData>(); 
    public char[][] board;
    
    
    public int recursifCheckStep(char bidak, String pos) {
        ArrayList<String> steps = this.getAvailableStep(bidak, pos);
        boolean isCheck = this.checkByBidak(bidak, pos);
        int jmlLangkah = 0;
        if (isCheck) {
            jmlLangkah = 1;
        } 
        
        else if (steps.size() == 0) {
            jmlLangkah = 0;
        } 
        
        else {
            int min = 1111111111;
            for (int i = 0; i < steps.size(); i++) {
                Posisi p = new Posisi(steps.get(i));
                char temp = this.board[p.xb][p.yb];
                this.board[p.xb][p.yb] = 'x';
                int ret = this.recursifCheckStep(bidak, p.pos);
                if(ret!=0){
                    ret++;
                }
                
                if (ret < min && ret>0) {
                    min = ret;
                }
                this.board[p.xb][p.yb] = temp;
            }
            
            if(min==1111111111)
                min=0;
            jmlLangkah = min;
        }
        return jmlLangkah;
    }

    // Inisiasi papan catur
    public Castling() {
        // Buat papan catur
        board = new char[8][8];

    }

    /* 
     * Fill the board
     *  
     * */
    public void fillBoard(int i, int j, char c) {
        board[i][j] = c;
        if(c=='k'){
            Posisi p = new Posisi(i,j);
            this.kingPosition = p;
        }
    }

    /*
     * Print Baord
     * */
    public void printBoard() {
        for (int i = 0; i < 8; i++) {

            for (int j = 0; j < 8; j++) {
                System.out.print(board[i][j]);
            }

            System.out.println();

        }
    }    

    /* 
     * Move Pawn 
     * 		Posisi[x][y+1] maju satu langkah ke depan 
     * 		karena pion hanya bisa maju ke depan maka tidak perlu diinisiasi seperti kuda, raja dll
     * */
    public ArrayList<String> generatePawn1Step(String pos) {
        Posisi p = new Posisi(pos);
        String nextStep = p.move(0, 1);
        ArrayList<String> steps = new ArrayList<String>();
        if (this.isEmptyPos(nextStep) && !p.isOutOfBoard) {
            steps.add(nextStep);
        }
        return steps;
    }

    // method untuk meng-handle skak yang dilakukan oleh bidak
    public boolean checkByBidak(char bidak, String pos) {
        char temp = this.emptyMark;
        this.emptyMark = 'k';
        boolean status = false;
        ArrayList<String> posKing = this.getAvailableStep(bidak, pos);
        //System.out.println(posKing);
        status = posKing.contains(this.kingPosition.pos);
        System.out.println(posKing+"=>"+this.kingPosition.pos);
        this.emptyMark = temp;
        return status;
    }

    /* 
     * Move Rook 
     * Dapat pindah ke 
     * 		Posisi[x][y++] ke atas. Batasan: y <=8 atau bersentuhan dg bidak lain/bidak musuh  
     * 		Posisi[x++][x] ke kanan. Batasan: x <=8 (h) atau bersentuhan dg bidak lain/bidak musuh  
     *  	Posisi[x][y--] ke bawah. Batasan: y >=8 atau bersentuhan dg bidak lain/bidak musuh 
     *  	Posisi[x--][x] ke kiri. Batasan: x >=1 (a) atau bersentuhan dg bidak lain/bidak musuh
     *  	karena bishop dapat berjalan vertikal dan horisontal serta tanpa batasan selain menabrak bidak lain maka langkah hanya 
     *  	ditetapkan 4 langkah tidak perlu diinisiasi seperti kuda, raja dll
     * */
    public ArrayList<String> generateRook1Step(String pos) {
        ArrayList<String> steps = new ArrayList<String>();
        //maju
        boolean available = true;
        Posisi p = new Posisi(pos);
        while (p.y < 8 && available && !p.isOutOfBoard) {
            String ret = p.move(0, 1);
            available = this.isEmptyPos(ret);
            if (available && !p.isOutOfBoard) {
                steps.add(ret);
            }
        }
        //mundur
        available = true;
        p = new Posisi(pos);
        while (p.y > 1 && available) {
            String ret = p.move(0, -1);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }

        //ke kiri
        available = true;
        p = new Posisi(pos);
        while (p.x > 1 && available) {
            String ret = p.move(-1, 0);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }
        //ke kanan
        available = true;
        p = new Posisi(pos);
        while (p.x < 8 && available) {
            String ret = p.move(1, 0);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }
        return steps;
    }

    /* 
     * Move Knight 
     * Dapat pindah ke 
     * 		Posisi[x++][y+2] ke kanan atas: dua langkah y dan satu langkah x. Batasan: bersentuhan dg bidak lain/bidak musuh  
     * 		Posisi[x+2][y++] ke kanan atas: satu langkah y dan dua langkah x. Batasan: bersentuhan dg bidak lain/bidak musuh  
     *  	Posisi[x+2][y--] ke kanan bawah: dua langkah x dan satu langkah y. Batasan: bersentuhan dg bidak lain/bidak musuh 
     *  	Posisi[x++][y-2] ke kanan bawah: satu langkah x dan dua langkah y. Batasan: bersentuhan dg bidak lain/bidak musuh
     *  	Posisi[x--][y-2] ke kiri bawah: satu langkah x dan dua langkah y. Batasan: bersentuhan dg bidak lain/bidak musuh
     * 		Posisi[x-2][y--] ke kiri bawah: dua langkah x dan satu langkah y. Batasan: bersentuhan dg bidak lain/bidak musuh
     *		Posisi[x-2][y++] ke kiri atas: dua langkah x dan satu langkah y. Batasan: bersentuhan dg bidak lain/bidak musuh
     *		Posisi[x--][y+2] ke kiri atas: satu langkah x dan dua langkah y. Batasan: bersentuhan dg bidak lain/bidak musuh  
     *  
     * */
    public ArrayList<String> generateKnight1Step(String pos) {
        ArrayList<String> steps = new ArrayList<String>();
        // inisiasi langkah kuda, dy=1 dan dx=2 berarti maju ke atas/utara, dx=-2 dan dy=1 berarti maju ke kiri atas dst
        int dx[] = {1, 2, 2, 1, -1, -2, -2, -1};
        int dy[] = {2, 1, -1, -2, -2, -1, 1, 2};
        for (int i = 0; i < 8; i++) {
            Posisi p = new Posisi(pos);
            String ret = p.move(dx[i], dy[i]);
            if (this.isEmptyPos(ret) && !p.isOutOfBoard) {
                steps.add(ret);
            }
        }
        return steps;
    }

    /* 
     * Move Bishop 
     * Dapat pindah ke 
     * 		Posisi[x++][y++] ke timur laut, Batasan: y <=8 atau bersentuhan dg bidak lain/bidak musuh  
     * 		Posisi[x++][y--] ke tenggara, Batasan: x <=8 (h) atau bersentuhan dg bidak lain/bidak musuh  
     *  	Posisi[x--][y--] ke barat daya, Batasan: y >=8 atau bersentuhan dg bidak lain/bidak musuh 
     *  	Posisi[x--][y++] ke barat laut, Batasan: x >=1 (a) atau bersentuhan dg bidak lain/bidak musuh
     *  	karena bishop dapat berjalan miring dan tanpa batasan selain menabrak bidak lain maka langkah hanya 
     *  	ditetapkan 4 langkah tidak perlu diinisiasi seperti kuda, raja dll
     * */
    public ArrayList<String> generateBishop1Step(String pos) {
        ArrayList<String> steps = new ArrayList<String>();
        //maju kanan
        boolean available = true;
        Posisi p = new Posisi(pos);
        while (p.y < 8 && p.x < 8 && available && !p.isOutOfBoard) {
            String ret = p.move(1, 1);
            available = this.isEmptyPos(ret);
            if (available && !p.isOutOfBoard) {
                steps.add(ret);
            }
        }
        //mundur kanan
        available = true;
        p = new Posisi(pos);
        while (p.y > 1 && p.x < 8 && available && !p.isOutOfBoard) {
            String ret = p.move(1, -1);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }

        //mundur kiri
        available = true;
        p = new Posisi(pos);
        while (p.x > 1 && p.y > 1 && available && !p.isOutOfBoard) {
            String ret = p.move(-1, -1);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }
        //maju kiri
        available = true;
        p = new Posisi(pos);
        while (p.x > 1 && p.y < 8 && available && !p.isOutOfBoard) {
            String ret = p.move(-1, 1);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }
        return steps;
    }

    /* 
     * Move Queen 
     * Dapat pindah ke 
     * 		Posisi[x++][y++] ke timur laut, Batasan: y <=8 atau bersentuhan dg bidak lain/bidak musuh  
     * 		Posisi[x++][y--] ke tenggara, Batasan: x <=8 (h) atau bersentuhan dg bidak lain/bidak musuh  
     *  	Posisi[x--][y--] ke barat daya, Batasan: y >=8 atau bersentuhan dg bidak lain/bidak musuh 
     *  	Posisi[x--][y++] ke barat laut, Batasan: x >=1 (a) atau bersentuhan dg bidak lain/bidak musuh
     * 		Posisi[x][y++] ke atas. Batasan: y <=8 atau bersentuhan dg bidak lain/bidak musuh  
     * 		Posisi[x++][x] ke kanan. Batasan: x <=8 (h) atau bersentuhan dg bidak lain/bidak musuh  
     *  	Posisi[x][y--] ke bawah. Batasan: y >=8 atau bersentuhan dg bidak lain/bidak musuh 
     *  	Posisi[x--][x] ke kiri. Batasan: x >=1 (a) atau bersentuhan dg bidak lain/bidak musuh
     *  	8 langkah queen merupakan kombinasi antara langkah Rook dan Bishop
     *  
     * */
    public ArrayList<String> generateQueen1Step(String pos) {
        ArrayList<String> steps = new ArrayList<String>();
        //maju kanan
        boolean available = true;
        Posisi p = new Posisi(pos);
        while (p.y < 8 && p.x < 8 && available && !p.isOutOfBoard) {
            String ret = p.move(1, 1);
            available = this.isEmptyPos(ret);
            if (available && !p.isOutOfBoard) {
                steps.add(ret);
            }
        }
        //mundur kanan
        available = true;
        p = new Posisi(pos);
        while (p.y > 1 && p.x < 8 && available) {
            String ret = p.move(1, -1);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }

        //mundur kiri
        available = true;
        p = new Posisi(pos);
        while (p.x > 1 && p.y > 1 && available) {
            String ret = p.move(-1, -1);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }
        //maju kiri
        available = true;
        p = new Posisi(pos);
        while (p.x > 1 && p.y < 8 && available) {
            String ret = p.move(-1, 1);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }

        //maju
        available = true;
        p = new Posisi(pos);
        while (p.y < 8 && available) {
            String ret = p.move(0, 1);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }
        //mundur
        available = true;
        p = new Posisi(pos);
        while (p.y > 1 && available) {
            String ret = p.move(0, -1);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }

        //ke kiri
        available = true;
        p = new Posisi(pos);
        while (p.x > 1 && available) {
            String ret = p.move(-1, 0);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }
        //ke kanan
        available = true;
        p = new Posisi(pos);
        while (p.x < 8 && available) {
            String ret = p.move(1, 0);
            available = this.isEmptyPos(ret);
            if (available) {
                steps.add(ret);
            }
        }
        return steps;
    }


    /* 
     * Move King 
     * Dapat pindah ke 
     * 		Posisi[x++][y++] ke timur laut, Batasan: y <=8 atau bersentuhan dg bidak lain/bidak musuh  
     * 		Posisi[x++][y--] ke tenggara, Batasan: x <=8 (h) atau bersentuhan dg bidak lain/bidak musuh  
     *  	Posisi[x--][y--] ke barat daya, Batasan: y >=8 atau bersentuhan dg bidak lain/bidak musuh 
     *  	Posisi[x--][y++] ke barat laut, Batasan: x >=1 (a) atau bersentuhan dg bidak lain/bidak musuh
     * 		Posisi[x][y++] ke atas. Batasan: y <=8 atau bersentuhan dg bidak lain/bidak musuh  
     * 		Posisi[x++][x] ke kanan. Batasan: x <=8 (h) atau bersentuhan dg bidak lain/bidak musuh  
     *  	Posisi[x][y--] ke bawah. Batasan: y >=8 atau bersentuhan dg bidak lain/bidak musuh 
     *  	Posisi[x--][x] ke kiri. Batasan: x >=1 (a) atau bersentuhan dg bidak lain/bidak musuh
     * */
    public ArrayList<String> generateKing1Step(String pos) {
        ArrayList<String> steps = new ArrayList<String>();
        
        // inisiasi langkah raja, dy=1 dan dx=0 berarti maju ke atas/utara, dx=1 dan dy=1 berarti maju ke kanan atas/timur laut dst
        int dx[] = {1, 1, -1, -1, 0, 1, 0, -1};
        int dy[] = {1, -1, -1, 1, 1, 0, -1, 0};
        for (int i = 0; i < 8; i++) {
            Posisi p = new Posisi(pos);
            String ret = p.move(dx[i], dy[i]);
            if (this.isEmptyPos(ret) && !p.isOutOfBoard) {
                steps.add(ret);
            }
        }
        return steps;
    }

    public boolean isEmptyPos(String pos) {
        Posisi p = new Posisi(pos);
        if (p.xb > 7 || p.yb > 7 || p.xb < 0 || p.yb < 0) {
            return false;
        } else {
            return board[p.xb][p.yb] == this.emptyMark || board[p.xb][p.yb]=='.';
        }
    }

    
    // method untuk mengecek langkah yang tersedia dari sebuah bidak yang akan melakukan skak terhadap raja lawan
    public ArrayList<String> getAvailableStep(char bidak, String pos1) {
        ArrayList<String> availablePos = new ArrayList<String>();

        switch (bidak) {
            case 'P':	// langkah yang tersedia untuk bidak Pawn
                availablePos = this.generatePawn1Step(pos1);
                break;
            case 'R':	// langkah yang tersedia untuk bidak  Rook
                availablePos = this.generateRook1Step(pos1);
                break;
            case 'N': // langkah yang tersedia untuk bidak  Knight
                availablePos = this.generateKnight1Step(pos1);
                break;
            case 'B': // langkah yang tersedia untuk bidak  Bishop
                availablePos = this.generateBishop1Step(pos1);
                break;
            case 'Q': // langkah yang tersedia untuk bidak Queen
                availablePos = this.generateQueen1Step(pos1);
                break;
            case 'K': // langkah yang tersedia untuk bidak King
                availablePos = this.generateKing1Step(pos1);
                break;
        }
        return availablePos;
    }
    
    
    public boolean move(String pos1, String pos2) {
        char temp = this.emptyMark;
        this.emptyMark = '.';
        boolean status = !this.isEmptyPos(pos1) && this.isEmptyPos(pos2);
        Posisi pos = new Posisi(pos1);
        char bidak = this.board[pos.xb][pos.yb];

        if (status) {
            ArrayList<String> availablePos = this.getAvailableStep(bidak, pos1);
            if (!availablePos.contains(pos2)) {
                status = false;
            } 
            
            else {
                Posisi pos_awal = new Posisi(pos1);
                Posisi pos_akhir = new Posisi(pos2);
                this.board[pos_akhir.xb][pos_akhir.yb] = bidak;
                this.board[pos_awal.xb][pos_awal.yb] = '.';
                this.log.add(new LogData(bidak, pos1, pos2));
            }
        }
        
        this.emptyMark = temp;
        return status;
    }

    // Mencetak log data 
    public void cetakLog(int nomor) {
    	
        if (nomor == 0) {
        	
            if (log.size() <= 0) {
                System.out.println("Log is empty");
            } 
            
            else {
                for (int i = 0; i < this.log.size(); i++) {
                    int no = i + 1;
                    System.out.println(no + ". " + log.get(i).bidak + ": " + log.get(i).start + " " + log.get(i).end);
                }
            }
        } 
        
        else {
            if (log.size() < nomor) {
                System.out.println("Invalid entry");
            } 
            
            else {
                System.out.println(nomor + ". " + log.get(nomor - 1).bidak + ": " + log.get(nomor - 1).start + " " + log.get(nomor - 1).end);
            }
        }
    }

    
    /*
     * Method untuk mencetak path yang dibentuk oleh bidak
     * */
    public void printPath(String pos) {
        
    	HashMap<String, String> path = new HashMap<String, String>();
        ArrayList<String> keyMap = new ArrayList<String>();
        
        // looping sebanyak data yang ada di dalam variable log
        for (int i = 0; i < log.size(); i++) {
            String bidak = log.get(i).bidak + "";
            String end = log.get(i).end;
            String start = log.get(i).start;

            // jika posisi start ke i sama dengan posisi bidak yang akan dicetak 
            if (start.equalsIgnoreCase(pos)) {
            	
            	// jika ada bidak dilalam path yang dicari maka tidak perlu ditambahkan kedalam keyMap
                if (path.containsKey(bidak)) {
                    String data = path.get(bidak);
                    data = data + " " + end + ",";
                    path.put(bidak, data);
                } 
                
                else {
                    path.put(bidak, " "+start+", " + end + ",");
                    keyMap.add(bidak);
                }
            } 
            
            // jika data bidak tersedia maka ditambahkan ke dalam hasmap
            else {
                if (path.containsKey(bidak)) {
                    String data = path.get(bidak);
                    data = data + " " + end + ",";
                    path.put(bidak, data);
                }
            }
        }
        
        // jika path kosong maka print: "Path tidak ditemukan"
        if (path.isEmpty()) {
            System.out.println("Path not found");
        } 
        
        // jika tidak kosong maka print data yang ada didalam path
        else {
        	
        	// looping data data yang disimpan dalam path. 1,2,3 <- urutan path yang ada
            for (int i = 0; i < keyMap.size(); i++) {
                String dataPath = path.get(keyMap.get(i));
                dataPath = dataPath.substring(0, dataPath.length() - 1);
                System.out.println(keyMap.get(i) + ":" + dataPath);
            }
        }
    }
}

/*
 * Class untuk menyimpan data log bidak
 * */
class LogData {

    public char bidak;
    public String start;
    public String end;

    // log bidak dari koordinat awal hingga koordinat tujuan yang diberikan oleh inputan
    LogData(char bidak, String start, String end) {
        this.bidak = bidak;
        this.start = start;
        this.end = end;
    }
}


/*
 * Class posisi adalah class yang mengatur posisi keberadaan bidak dalam format papan catur yang 
 * dalam proses programmingnya di terjemahkan kedalam matrik data 
 * */
class Posisi {

    public String pos; // posisis sebenarnya pada board nyata dengan simbol huruf
    public int x; //posisi sebenarnya pada board nyata
    public int y;
    public int xb; // posisi pada array board
    public int yb;
    char[] map = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    public boolean isOutOfBoard = false;

    Posisi(String pos) {
        this.pos = pos;
        this.convertToXY();
    }

    Posisi(int x, int y) {
        this.x = x;
        this.y = y;
        String p = this.convertPosBoardToReal();

    }

    // menerjemahkan a,b,c,...,i ke angka untuk dimasukan ke dalam matrik data
    public void convertToXY() {
        char col = pos.charAt(0);
        this.y = Integer.parseInt(pos.charAt(1) + "");
        switch (col) {
            case 'a':
                this.x = 1;
                break;
            case 'b':
                this.x = 2;
                break;
            case 'c':
                this.x = 3;
                break;
            case 'd':
                this.x = 4;
                break;
            case 'e':
                this.x = 5;
                break;
            case 'f':
                this.x = 6;
                break;
            case 'g':
                this.x = 7;
                break;
            case 'h':
                this.x = 8;
                break;
        }
        this.yb = this.x - 1;
        this.xb = 8 - this.y;
    }

    // convert 1,2,3,...,8 ke a,b,c...
    public String convertToString() {        
        pos = map[this.x - 1] + "" + this.y;
        this.yb = this.x - 1;
        this.xb = 8 - this.y;
        return pos;
    }
    
    // convert posisi board yang terbalik 8,7,6,... ke 1,2,3,...
    public String convertPosBoardToReal(){
        this.xb = x;
        this.yb = y;
        this.x = this.yb+1;
        this.y = 8-this.xb;
        
        pos = map[this.x - 1] + "" + this.y;
        return pos;
    }

    // membaca langkah bidak dan menconvertnya kedalam format string
    public String move(int dx, int dy) {
        this.x = x + dx;
        this.y = y + dy;
        String p = this.pos;
        if (x > 8 || x < 1 || y > 8 || y < 1) {
            this.x = x - dx;
            this.y = y - dy;
            this.isOutOfBoard = true;
        } 
        
        else {
            p = this.convertToString();
        }
        return p;
    }
}
