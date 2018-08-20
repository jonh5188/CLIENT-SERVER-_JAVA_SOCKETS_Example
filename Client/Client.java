import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
	public static void main(String[] args) {
	Scanner in = new Scanner(System.in);
	System.out.print("Hostname: ");
	String hostname = in.next();
	System.out.print("Port #: ");
	int port = in.nextInt();

    Socket s = null;  
    OutputStream os = null;
    ObjectOutputStream oos = null;
    InputStream is = null;
    ObjectInputStream ois = null;
    FileOutputStream out = null;
    FileInputStream in2 = null;
    
    try {
    	s = new Socket(hostname, port);
    	os = s.getOutputStream();  
    	oos = new ObjectOutputStream(os);
    	is = s.getInputStream();  
    	ois = new ObjectInputStream(is); 
    } catch (UnknownHostException e) {
    	System.err.println("Unknown Host: " + hostname);
    } catch (IOException e) {
    	System.err.println("IO error while trying to communicate with): " + hostname);
        }
	
	if (s == null || os == null || oos == null || is == null || ois == null) {
	    System.err.println("An unknown error has occurred");
	    return;
	}

	try {
		File currentDir = new File(System.getProperty("user.dir"));
		
	    while (true) {
	    	String cmd = in.nextLine();
	    	
	    	
	    	// LIST ALL FILES IN THE CURRENT DIRECTORY ON THE SERVER
	    	if (cmd.equals("LIST")) {
	    		String L = cmd;
	    		cmdRequest list = new cmdRequest(L);
	    		oos.writeObject(list);  
	    		File[] paths = (File[])ois.readObject();
	    		String file;
	    		if (paths.length == 0 || paths == null) {
    				System.out.println("No files exist in current directory");
    			}
	    		for (File path:paths) {
	    			if (path.isFile()) {
	    				file = path.getName();
	    				System.out.println(file);
	    			}
	    		} System.out.println();
	    	}
	    	
	    	
	    	// GET A FILE FROM THE SERVER
	    	if (cmd.startsWith("GET")) {
	    		String G = cmd;
	    		cmdRequest GET = new cmdRequest(G);
				oos.writeObject(GET);
	    		String[] part2 = cmd.split(" ", 2);
				String C = part2[0];
				String D = part2[1];
				String file = currentDir + "/" +D;
				File f = new File(file);
				for (int i = 0; i < 1; i++) {  
		            out = new FileOutputStream(f);   
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
		                System.out.println("File does not exist on the server");  
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
		                out.write(buf, 0, c);  
		            }     
		            out.close();
		            System.out.println();
		        }  
	    	}
	    	
	    	
	    	// PUT A FILE ON THE SERVER
	    	if (cmd.startsWith("PUT")) {
	    		String P = cmd;
	    		cmdRequest PUT = new cmdRequest(P);
				oos.writeObject(PUT);
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
		                in2 = new FileInputStream(ff);  
		                byte[] bb = new byte[1024];  
		                for (int c = in2.read(bb); c > -1; c = in2.read(bb)) {  
		                    os.write(bb, 0, c);  
		                }  
		                in2.close();
		           }  
				} else if (file1.exists() == false){
					System.out.println("File does not exist");
				}
	    	}
	    	
	    	
	    	// CHANGE THE SERVER DIRECTORY
	    	if (cmd.startsWith("CD")) {
	      		String C = cmd;
	    		cmdRequest CD = new cmdRequest(C);
	    		oos.writeObject(CD);
	    		Object dir = (Object)ois.readObject();
	    		System.out.println("Current Directory: " + dir);
	    		System.out.println();
	      	}
	    	
	    	
	    	// CHANGE THE CLIENT DIRECTORY
	    	if (cmd.startsWith("LCD")) {
	    		String[] part = cmd.split(" ", 2);
				String C = part[0];
				String D = part[1];
				File tmp = new File(D);
				if (tmp.exists()) {
				currentDir = new File(D);
				System.out.println("Current Directory: " + currentDir);
				System.out.println();
				} else {
					System.out.println("Current Directory: Directory does not exist");
					System.out.println();
				}
	    	}
	    	
	    	
	    	// SEND THE FILE SIZE
	    	if (cmd.startsWith("SIZE")) {
	    		String S = cmd;
	    		cmdRequest SIZE = new cmdRequest(S);
	    		oos.writeObject(SIZE);
	    		Object receive = (Object)ois.readObject();
	    		System.out.println("File Size: "+receive+" bytes");
	    		System.out.println();
	    	}
	    	
	    	
	    	// CLOSE THE CLIENT CONNECTION
	    	if (cmd.equals("QUIT")) {
	    		String Q = cmd;
	    		cmdRequest QUIT = new cmdRequest(Q);
	    		oos.writeObject(QUIT);
	    		break;
	      	}
	    }
	    in.close();
	    oos.close();
	    os.close();
	    ois.close();
	    is.close();
	    s.close();   
	} catch (UnknownHostException e) {
	    System.err.println("Trying to connect to unknown host: " + e);
	} catch (IOException e) {
	    System.err.println("IOException:  " + e);
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	}
    }           
}
