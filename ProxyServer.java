
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ProxyServer {

	static Map<String, Integer> conversionMap = new HashMap<String, Integer>();

	static String msg = null, tempMsg = null;
	static Socket toAnother = null; // socket that connects to conversion server
	static PrintWriter outToServer = null;
	static BufferedReader inFromServer = null;

	public static void main(String args[]) {
		// check argument
		if (args.length != 1) {
			System.out
					.println("Wrong argument. Run this server by inputing \"java ProxyServer port\"");
			System.exit(-1);
		}
		// host a server
		int port = Integer.parseInt(args[0]);
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Server hosted at port " + port);
		try {
			while (true) {
				Socket socket = server.accept();
				System.out.println("Got Connection from:"
						+ socket.getInetAddress() + ":" + socket.getPort());
				convert(socket);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void convert(Socket socket) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println("You are connected to the Proxy Server!\n");
			String request = in.readLine();
			if (request == null) {
				// wrong argument input by client
				System.out.println("No input recieved.");
				closeSocket(in, out, socket);
				return;
			}
			// split parameters
			String[] paras = request.split(" ");
			if (paras.length != 3) {
				System.out.println("Wrong argument.");
				out.println("Wrong argument.\nUsage: unit1 unit2 1");
				closeSocket(in, out, socket);
				return;
			}
			System.out.println("request is:" + paras[0] + "->" + paras[1]);
			double temp = 0; // used to store result received from conversion
								// server
			try {
				temp = Double.parseDouble(paras[2]);
			} catch (NumberFormatException e) {
				// invalid number
				System.out.println("Invalid number format: " + paras[2]);
				closeSocket(in, out, socket);
				return;
			}
			IPAddressAndPort address = new IPAddressAndPort();
			String type=paras[0]+" "+paras[1];
			if ((temp = getAddrandConv(type, temp, false, in, out,
					socket)) == -1) {
				return;
			}
			out.println(temp + "\n");
			// close socket, out and inputStream
			closeSocket(in, out, socket);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private static double getAddrandConv(String type, double temp, boolean rev,
			BufferedReader in, PrintWriter out, Socket socket) throws Exception {
		IPAddressAndPort address = new IPAddressAndPort();
		address = getAddress(type);
		if (address == null) {
			out.println("Unsupported conversion:" + type);
			closeSocket(in, out, socket);
			return -1;
		}
		temp = convert(temp, type, rev, address.address, address.port);
		return temp;
	}

	/**
	 * Get conversion server address and port
	 * 
	 * @param type
	 * @return IPAddressAndPort
	 */
	private static IPAddressAndPort getAddress(String type) {
		IPAddressAndPort ap;
		Socket searchSocket = null;
		try {
			searchSocket = new Socket("baobaoioz.koding.io", 23456);
			PrintWriter out = new PrintWriter(searchSocket.getOutputStream(),
					true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					searchSocket.getInputStream()));
			out.println("lookup " + type);
			String tempMsg, msg = null;
			while ((tempMsg = in.readLine()) != null) {
				msg = tempMsg;
				System.out.println("msg from discover:" + msg);
			}
			if (msg == null) {
				// wrong argument input by client
				System.out.println("No input recieved.");
				closeSocket(in, out, searchSocket);
				return null;
			}
			if (msg.toLowerCase().equals("none")) {
				return null;
			}
			if (msg.toLowerCase().split(" ").length == 2) {
				String paras[] = msg/* .substring(8) */.split(" ");
				ap = new IPAddressAndPort(paras[0], Integer.parseInt(paras[1]));
				return ap;
			} else {
				return null;
			}
		} catch (Exception e) {
			try {
				searchSocket.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	// close socket to the client
	private static void closeSocket(BufferedReader in, PrintWriter out,
			Socket socket) {
		try {
			out.close();
			out.flush();
			in.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param temp
	 *            temp value
	 * @param type
	 *            conversion type
	 * @param rev
	 *            reverse or not
	 * @throws Exception
	 */
	private static double convert(double temp, String type, boolean rev,
			String addr, int port) throws Exception {
		String from = null, to = null;
		String paras[] = type.split(" ");
		from=paras[0];
		to=paras[1];
		toAnother = new Socket(addr, port);
		
		inFromServer = new BufferedReader(new InputStreamReader(
				toAnother.getInputStream()));
		outToServer = new PrintWriter(toAnother.getOutputStream(), true);
		outToServer.println(from + " " + to + " " + temp);
		while ((tempMsg = inFromServer.readLine()) != null) {
			msg = tempMsg;
		}
		System.out.print(temp + " " + from + " = ");
		temp = Double.parseDouble(msg);
		System.out.println(temp + " " + to);
		// close socket, out and inputStream
		toAnother.close();
		outToServer.flush();
		outToServer.close();
		inFromServer.close();
		return temp;
	}

	static {
		// initial conversion types
		conversionMap.put("ft in", 1);
		conversionMap.put("in cm", 2);
		conversionMap.put("cm m", 3);
		conversionMap.put("m km", 4);
	}
}
