package com.samod.fun7demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;
import java.util.Scanner;

@SpringBootApplication
public class Fun7demoApplication {
	static DbController db;

	//constructs a db controller and starts the spring application
	//runs until user inputs "exit" into command line
	public static void main(String[] args) {
		db = null;
		try {
			db = new DbController(); //construct db controller
			Scanner sc = new Scanner(System.in); //scanner to read input

			var app = SpringApplication.run(Fun7demoApplication.class, args); //run spring app

			while(app.isActive()){ //check input for 'exit' to keep thread from finishing
				if (sc.hasNextLine() && sc.nextLine().equals("exit")){
					break;
				}
			}

		} catch (SQLException e){ //handle db init errors
			DbController.printSQLException(e);

		} finally { //close resources
			if(db != null){ db.close(); db=null; }
			System.exit(0);
		}
	}
}