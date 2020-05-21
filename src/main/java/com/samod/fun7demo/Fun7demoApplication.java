package com.samod.fun7demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class Fun7demoApplication {
	static DbController db;
	public static void main(String[] args) {
		db = null;
		try {
			db = new DbController();
			Scanner sc = new Scanner(System.in);
			var app = SpringApplication.run(Fun7demoApplication.class, args);
			while(app.isActive()){
				if (sc.hasNextLine() && sc.nextLine().equals("exit")){
					break;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
		} finally {
			if(db != null){ db.close(); db=null; }
			System.exit(0);
		}
	}
}