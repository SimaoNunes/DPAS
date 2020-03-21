package Server;

import java.io.*;

import java.security.PublicKey;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.net.ServerSocket;
import java.net.Socket;

import Library.Request;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.apache.commons.io.*;

public class App 
{
    public static void main( String[] args ) {

		try {
			ServerSocket ss = new ServerSocket(9000);
			Server server = new Server(ss);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
