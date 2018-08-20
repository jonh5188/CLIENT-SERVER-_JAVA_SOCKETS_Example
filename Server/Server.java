import java.io.*;
import java.net.*;
import java.util.Scanner;

class cmdRequest implements Serializable {   
	String id;  
	public cmdRequest(String str){    
		this.id=str;  
	}  
}  

class cmdRequest2 implements Serializable {   
	long id;  
	public cmdRequest2(long l){    
		this.id=l;  
	}  
} 

public class Server {
    public static void main(String args[]) {
    	Scanner in1 = new Scanner(System.in);
    	System.out.print("Port #: ");
    	int port = in1.nextInt();
    	Server server = new Server( port );
    	server.startServer();
    }
    
    ServerSocket ss = null;
    Socket s = null;
    int connections = 0;
    int port;
	
    public Server( int port ) {
    	this.port = port;
    }
    
    public void startServer() {
        try {
        	ss = new ServerSocket(port);
        } catch (IOException e) {
        	System.out.println(e);
        }   
	
	System.out.println("Waiting for Client connections...");

	
	// CREATE A THREAD FOR EACH CLIENT
	while (true) {
	    try {
	    	s = ss.accept();
	    	connections++;
	    	ServerConnection oneconnection = new ServerConnection(s, connections, this);
	    	new Thread(oneconnection).start();
	    } catch (IOException e) {
	    	System.out.println(e);
	    }
		}
    }
}

class ServerConnection implements Runnable {
    InputStream is;
    ObjectInputStream ois;
    OutputStream os;
    ObjectOutputStream oos;
    Socket s;
    int id;
    Server server;
    FileInputStream in = null;
    FileOutputStream out1 = null;


    public ServerConnection(Socket s, int id, Server server) {
    	this.s = s;
    	this.id = id;
    	this.server = server;
    	System.out.println("Connection " + id + " accepted with: " + s);
    	try {
    		is = s.getInputStream();  
    		ois = new ObjectInputStream(is);
    		os = s.getOutputStream();  
    		oos = new ObjectOutputStream(os);  
    	} catch (IOException e) {
    		System.out.println(e);
    	}
    }
    public void run() {
    	File currentDir = new File(System.getProperty("user.dir"));
	try {
		// PROCESS COMMANDS FROM THE CLIENT	
		boolean serve = true;
		while (serve != false) {
			cmdRequest receive = (cmdRequest)ois.readObject();
			String cmd = receive.id;
			
			
			// SEND LIST OF FILES
			if (cmd.equals("LIST")) {
				File[] paths = currentDir.listFiles();
				oos.writeObject(paths);
			} 
			
			
			// CHANGE THE CURRENT DIRECTORY
			if (cmd.startsWith("CD")) {
				String[] part = cmd.split(" ", 2);
				String C = part[0];
				String D = part[1];
				File tmp = new File(D);
				if (tmp.exists()) {
				currentDir = new File(D);
				oos.writeObject(currentDir);
				} else {
					String strr = "Directory does not exist on the server";
					Object ff = strr;
					oos.writeObject(ff);
				}
			}
			
			
			// SEND THE FILE SIZE
			if (cmd.startsWith("SIZE")) {
				File f = null;
				boolean bool = false;
				String[] part1 = cmd.split(" ", 2);
				String C = part1[0];
				String D = part1[1];
				String file = D;
				f = new File(currentDir + "/" + D);
				bool = f.exists();
		        if(bool) {
		        	long len = f.length();
		        	Object tmp = len;
		            oos.writeObject(len);
		        } else {
		        	int none = -1;
		        	Object tmp = none;
		        	oos.writeObject(tmp);
		        }
			}
			
			
			// RECEIVE FILE FROM THE CLIENT
			if (cmd.startsWith("PUT")) {
	    		String[] part2 = cmd.split(" ", 2);
				String C = part2[0];
				String D = part2[1];
				String file = currentDir + "/" +D;
				File f = new File(file);
				for (int i = 0; i < 1; i++) {  
		            out1 = new FileOutputStream(f); 
		            byte[] buf0 = new byte[12];  
		            int readBytes = 0;  
		            while(readBytes < 12){  
		                if(readBytes > 0){  
		                    readBytes += is.read(buf0, readBytes, 12 - readBytes);  
		                } else readBytes += is.read(buf0, 0, 12);  
		            }  
		            int fLength = 0;  
		            try{  
		                fLength = new Integer(new String(buf0));  
		            } catch (NumberFormatException e ){  
		                System.out.println(e);  
		                System.exit(-1);  
		            }  
		            readBytes = 0;  
		            byte[] buf = new byte[1024];  
		            int c;  
		            while(readBytes  < fLength){  
		                if(fLength - readBytes > 1024){  
		                c = is.read(buf, 0, 1024);  
		                } else{  
		                    c = is.read(buf, 0, fLength - readBytes);  
		                }  
		                readBytes += c;  
		                out1.write(buf, 0, c);  
		            }     
		            out1.close();
		            System.out.println();
		        }  
			}
			
			
			// SEND FILE TO THE CLIENT
			if (cmd.startsWith("GET")) {
				boolean bool = false;
				String[] part2 = cmd.split(" ", 2);
				String C = part2[0];
				String D = part2[1];
				String file = D;
				String findFile = currentDir + "/" + D;
				File file1 = new File(findFile);
				bool = file1.exists();
				if (bool) {
					for (int i = 0; i < 1; i++) {   
		                File ff = new File(findFile); 
		                byte[] b = (ff.length()+"").getBytes();  
		                int d = b.length;   
		                for(int k = d-1; d < 12; d++){  
		                    os.write("0".getBytes()); 
		                }  
		                os.write(b);  
		                System.out.write(b);  
		                System.out.println();  
		                in = new FileInputStream(ff);  
		                byte[] bb = new byte[1024];  
		                for (int c = in.read(bb); c > -1; c = in.read(bb)) {  
		                    os.write(bb, 0, c);  
		                }  
		                in.close();
		            } 
				} 
			}
			
			
			//  CLOSE THE CLIENT CONNECTION
			if (cmd.equals("QUIT")) {
				break;
			}
		}	    
	    System.out.println( "Connection " + id + " closed" );
	    	//in.close();
	    	ois.close();
            is.close();
            oos.close();
            os.close();
            s.close();
	} catch (IOException e) {
	    System.out.println(e);
	} catch (ClassNotFoundException e) {
		System.out.println(e);
	}
    }
}
