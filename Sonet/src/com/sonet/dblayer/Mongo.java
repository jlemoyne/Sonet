package com.sonet.dblayer;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ParallelScanOptions;
import com.mongodb.util.Hash;

import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Mongo {
	public DB db = null;
	
	public boolean connect(String dbname) {
		boolean connected = false;
		MongoClient mongoClient;
		try {
//			MongoClient mongoClient = new MongoClient();
			// or
			mongoClient = new MongoClient( "localhost" , 27017);
			// or
//			MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
			// or, to connect to a replica set, with auto-discovery of the primary, supply a seed list of members
//			MongoClient mongoClient = new MongoClient(Arrays.asList(new ServerAddress("localhost", 27017),
//			                                      new ServerAddress("localhost", 27018),
//			                                      new ServerAddress("localhost", 27019)));

			db = mongoClient.getDB( dbname );
			connected = true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connected;
	}
		
	
	public void manipulate() {
		System.out.println("~~~~~~ commands:");
//		System.out.println("use data-terminal");
//		db.command("use data-terminal");
		System.out.println("====> " + db.getName());
		Set<String> colls = db.getCollectionNames();

		for (String s : colls) {
		    System.out.println(s);
		}
	}
	

	public static void main(String[] args) {
		Mongo mongo = new Mongo();
		if (mongo.connect("sonet"))
			System.out.println("Connected to mongoDB!");
		else {
			System.out.println("NOT CONNECTED to mongoDB!");
			System.exit(105);
		}
		mongo.manipulate();
	}

}
