import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class DiscoveryServer {

	private static HashMap<IPAddressAndPort, String> convAddressMap = new HashMap<IPAddressAndPort, String>();

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
		// for (Entry<String, IPAddressAndPort> entry :
		// convAddressMap.entrySet()) {
		// System.out.println(entry.getKey()+" key="+entry.getValue().address+":"+entry.getValue().port);
		// }
		String paras[] = type.split(" ");
		IPAddressAndPort addr = null;
		if (paras.length != 2) {
			out.println("Wrong argument.\nUsage: get ft in");
			return;
		}
		Iterator<Entry<IPAddressAndPort, String>> iterator = convAddressMap
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<IPAddressAndPort, String> entry = iterator.next();
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
			out.println("SUCCESS");
		} else {
			out.println("FAILURE: existed");
		}
		System.out.println("size=" + convAddressMap.size());
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
		Iterator<Entry<IPAddressAndPort, String>> iterator = convAddressMap
				.entrySet().iterator();
		boolean success=false;
		while (iterator.hasNext()) {
			Entry<IPAddressAndPort, String> entry = iterator.next();
			if (entry.getKey().address.equals(paras[0]) && entry.getKey().port==Integer.parseInt(paras[1])) {
				iterator.remove();
				success=true;
			}
		}
		if (success) {
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