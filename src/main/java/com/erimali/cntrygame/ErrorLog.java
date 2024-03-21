package com.erimali.cntrygame;

import java.util.ArrayDeque;
import java.util.Deque;
//POPUP??
public class ErrorLog {
	private static Deque<String> errors = new ArrayDeque<>();

	public static void logError(String errorMessage) {
		errors.add(errorMessage);
	}
	public static void logError(Exception e) {
		errors.add(e.getMessage());
	}
	public static String retrieveErrors() {
		StringBuilder sb = new StringBuilder();
		while(!errors.isEmpty()) {
			sb.append(errors.poll()).append("\n");
		}
		return sb.toString();
	}
}
