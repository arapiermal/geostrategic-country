package com.erimali.cntrygame;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SaveGame {
	//does every subclass need serializable!??!?!
	public static String saveGamePath = "saveGames/";

	public static void saveGame(String name, GLogic g) {

		try {
			FileOutputStream fileOut = new FileOutputStream(saveGamePath + name + ".ser");
			// we create the file that accepts byte stream
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(g);
			// we convert the object to byte stream and write these byte streams to the file
			out.close();
			fileOut.close();

		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public static GLogic loadGame(String name) {
		try {
			FileInputStream fileIn = new FileInputStream(saveGamePath + name + ".ser");
			// we access the file to read byte stream
			ObjectInputStream in = new ObjectInputStream(fileIn);
			GLogic g = (GLogic) in.readObject();
			// we convert byte stream into real Object of the type GLogic. We need to
			// downcast to GLogic because the method readObject() return an Object type
			in.close();
			fileIn.close();
			return g;
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("GLogic class not found");
			c.printStackTrace();
			return null;
		}
	}
}
