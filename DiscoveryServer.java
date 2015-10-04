import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class DiscoveryServer {

	private static HashMap<IPAddressAndPort, String> convAddressMap = new HashMap<IPAddressAndPort, String>();

	private static ArrayList<String> convList = new ArrayList<String>();

	private static File f;

	private static int[][] matrix = new int[5][5];

	private static HashMap<String, Integer> indexMap = new HashMap<String, Integer>();

	public static void main(String[] args) {

		// check argument
		if (args.length != 1) {
			System.out
					.println("Wrong argument. Run this server by inputing \"java DiscoryServer port\"");
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
				process(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handle the request sent by socket
	 * 
	 * @param socket
	 */
	private static void process(Socket socket) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println("You are connected to the Discovery Server!\n");
			String request = in.readLine();
			System.out.println("Recv. msg: " + request);
			if (request == null) {
				// wrong argument input by client
				System.out.println("No input recieved.");
				closeSocket(in, out, socket);
				return;
			}
			processCertainAction(request, out);
			closeSocket(in, out, socket);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * do certain action requested by client
	 * 
	 * @param request
	 */
	private static void processCertainAction(String request, PrintWriter out) {
		if (request.toLowerCase().startsWith("add ")) {
			// store address of server
			insertConvAddress(request.substring(4), out);
		} else if (request.toLowerCase().startsWith("remove ")) {
			// remove requested address
			removeConvAddress(request.substring(7), out);
		} else if (request.toLowerCase().startsWith("lookup ")) {
			// get address for client
			getConvAddress(request.substring(7), out);
		} else {
			out.println("Unsupported command");
		}

		for (Entry<IPAddressAndPort, String> entry : convAddressMap.entrySet()) {

			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
	}

	private static void getConvAddress(String type, PrintWriter out) {
		String paras[] = type.split(" ");
		IPAddressAndPort addr = null;
		if (paras.length != 2) {
			out.println("Wrong argument.\nUsage: lookup ft in");
			return;
		}

		for (Entry<IPAddressAndPort, String> entry : convAddressMap.entrySet()) {
			if (entry.getValue().equals(
					paras[0].toLowerCase() + " " + paras[1].toLowerCase())
					|| entry.getValue().equals(
							paras[1].toLowerCase() + " "
									+ paras[0].toLowerCase())) {
				out.println(entry.getKey().address + " " + entry.getKey().port);
				return;
			}
		}
		// unsupported conversion type
		out.println("none");

		// if ((addr = convAddressMap.get(paras[0].toLowerCase() + " "
		// + paras[1].toLowerCase())) != null
		// || (addr = convAddressMap.get(paras[1].toLowerCase() + " "
		// + paras[0].toLowerCase())) != null) {
		// out.println(addr.address + " " + addr.port);
		// } else {
		// // unsupported conversion type
		// out.println("none");
		// }
	}

	/**
	 * Insert requested type into map
	 * 
	 * @param address
	 * @param out
	 */
	private static void insertConvAddress(String address, PrintWriter out) {
		String paras[] = address.split(" ");
		if (paras.length != 4) {
			out.println("Wrong argument.\nUsage: add unit1 unit2 IP_address port");
			return;
		}
		IPAddressAndPort ap = new IPAddressAndPort(paras[2],
				Integer.parseInt(paras[3]));
		// check existence
		boolean existed = false;
		for (Entry<IPAddressAndPort, String> entry : convAddressMap.entrySet()) {
			if (entry.getKey().equals(ap)) {
				System.out.println("EQUAL");
				existed = true;
				break;
			}
			System.out.println(entry.getValue());
		}
		if (!existed) {
			convAddressMap.put(ap,
					paras[0].toLowerCase() + " " + paras[1].toLowerCase());
			updateMatrix(paras[0], paras[1], "add");
			out.println("SUCCESS");
		} else {
			out.println("FAILURE: existed");
		}
		System.out.println("size=" + convAddressMap.size());
	}

	private static void updateMatrix(String type1, String type2, String action) {
		int indexOfType1 = indexMap.get(type1), indexOfType2 = indexMap
				.get(type2);
		if (action.equals("add")) {
			matrix[indexOfType1][indexOfType2] = 1;
			matrix[indexOfType2][indexOfType1] = 1;
		} else if (action.equals("remove")) {
			// check if there exists same conversion type provided by other
			// server
			boolean exists = false;

			for (Entry<IPAddressAndPort, String> entry : convAddressMap
					.entrySet()) {
				if ((type1.toLowerCase() + " " + type2.toLowerCase())
						.equals(entry.getValue())
						|| (type2.toLowerCase() + " " + type1.toLowerCase())
								.equals(entry.getValue())) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				matrix[indexOfType1][indexOfType2] = 0;
				matrix[indexOfType2][indexOfType1] = 0;
			}
		}

	}

	/**
	 * Remove requested type from map
	 * 
	 * @param type
	 *            type to remove
	 * @param out
	 */
	private static void removeConvAddress(String type, PrintWriter out) {
		String paras[] = type.split(" ");
		if (paras.length != 2) {
			out.println("Wrong argument.\nUsage: remove IP_address port");
			return;
		}
		IPAddressAndPort addr = new IPAddressAndPort();
		String success=null;
		Iterator<Entry<IPAddressAndPort, String>> iterator = convAddressMap.entrySet().iterator();
		Entry<IPAddressAndPort, String> entry;
		while (iterator.hasNext()) {
			entry = iterator.next();
			if (entry.getKey().equals(new IPAddressAndPort(paras[0], Integer.parseInt(paras[1])))) {
				success=entry.getValue();
				iterator.remove();
			}
		}
		if (success != null) {
			updateMatrix(success.split(" ")[0], success.split(" ")[1], "remove");
			out.println("SUCCESS");
		} else {
			out.println("FAILURE");
		}
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

	static {
		indexMap.put("ft", 0);
		indexMap.put("in", 1);
		indexMap.put("cm", 2);
		indexMap.put("m", 3);
		indexMap.put("km", 4);
	}

}

class IPAddressAndPort {

	String address;
	int port;

	public IPAddressAndPort() {
	}

	public IPAddressAndPort(String address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public String toString() {
		return address + " " + port;
	}

	@Override
	public boolean equals(Object obj) {
		IPAddressAndPort temp = (IPAddressAndPort) obj;
		return address.equals(temp.address) && port == temp.port;
	}

}