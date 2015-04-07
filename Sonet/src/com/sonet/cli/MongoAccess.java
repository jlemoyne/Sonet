package com.sonet.cli;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.Hash;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MongoAccess {
	public DB db = null;
	public MongoClient mongoClient;
	
	public boolean connect(String dbname) {
		boolean connected = false;
		try {
//			MongoClient mongoClient = new MongoClient();
			// or
//			mongoClient = new MongoClient( "quanta.local" , 27017);
			mongoClient = new MongoClient( "localhost" , 27017);
			// or
//			MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
			// or, to connect to a replica set, with auto-discovery of the primary, supply a seed list of members
//			MongoClient mongoClient = new MongoClient(Arrays.asList(new ServerAddress("localhost", 27017),
//			                                      new ServerAddress("localhost", 27018),
//			                                      new ServerAddress("localhost", 27019)));

			db = mongoClient.getDB( dbname );
			connected = true;
			System.out.println("Mongodb: client CONNECTED!");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.err.println("UNABLE TO CONNECT to mongoDB!");
		}
		return connected;
	}
	
	public void close() {
		mongoClient.close();
		System.out.println("Mongodb: client DISCONNECTED");
	}
	
	public int checkMongod() {
		int state = 1;
		JSch jsch = new JSch();
		try {
	        jsch.setKnownHosts("/Users/jclaudel/.ssh/known_hosts");
	        jsch.addIdentity("/Users/jclaudel/.ssh/id_rsa");
			Session session = jsch.getSession("jclaudel", "quanta.local", 22);
			session.setPassword("operacom");
//			session.setConfig("StrictHostKeyChecking", "no");
//			java.util.Properties config = new java.util.Properties(); 
//			config.put("StrictHostKeyChecking", "no");
//			session.setConfig(config);			
			session.connect();
			// execute command
			String cmd = "ps -elf | grep 'mongod' | grep -v grep";
			cmd = "ps ax | grep mongod";
//			String command=JOptionPane.showInputDialog("Enter command", 
//                    "set|grep SSH");			
			String command=JOptionPane.showInputDialog("Enter command", 
                    cmd);			
		    Channel channel=(Channel) session.openChannel("exec");
		    ((ChannelExec)channel).setCommand(command);
		    
		    ((ChannelExec)channel).setErrStream(System.err);
		    
		    channel.setInputStream(null);
//		    channel.setOutputStream(System.out);
		    
		    InputStream in=channel.getInputStream();
		      
		    channel.connect();
		 
		    byte[] tmp = new byte[1024];
		    while(true) {
		    		while(in.available() > 0){
		        int i=in.read(tmp, 0, 1024);
		        if(i < 0) break;
		        		System.out.print(new String(tmp, 0, i));
		        }
		        if(channel.isClosed()){
		        		if(in.available()>0) continue; 
		        		System.out.println("exit-status: "+ channel.getExitStatus());
		        		break;
		        }
		        try{Thread.sleep(1000);}catch(Exception ee){}		      
		    	}
		    channel.disconnect();
		    session.disconnect();		    
			state = 0;
		} catch (JSchException | IOException e) {
			e.printStackTrace();
		}
		return state;
	}
	
	public int checkMongoServer() {
		int state = 1;
		
		String ssh_host = "localhost";
		String ssh_user = "jclaudel";
		String ssh_password = "operacom";
	    ConnBean cb = new ConnBean(ssh_host, ssh_user, ssh_password);
	    
	    // Put the ConnBean instance as parameter for SSHExec static method getInstance(ConnBean) to retrieve a singleton SSHExec instance
	    SSHExec ssh = SSHExec.getInstance(cb);
	    // Connect to server
	    if (!ssh.connect()) {
	    		System.err.println("SSH: NOT connected!");
	    		return state;
	    }
	    CustomTask sampleTask1 = new ExecCommand("echo $SSH_CLIENT"); // Print Your Client IP By which you connected to ssh server on Horton Sandbox
	    try {
			System.out.println(ssh.exec(sampleTask1));
			CustomTask sampleTask2 = new ExecCommand("ps -elf | grep 'mongod' | grep -v grep");					
		    Result res = ssh.exec(sampleTask2);
		    System.out.println("exec success: " + res.isSuccess);
		    if (res.isSuccess) state = 0;
		    else {
		    		CustomTask sampleTask3 = new ExecCommand("/usr/local/bin/mongod &");
		    		res = ssh.exec(sampleTask3);
		    		if (res.isSuccess) state = 0;
		    }
		    ssh.disconnect();   
		} catch (TaskExecFailException e) {
			e.printStackTrace();
		}
	    return state;
		
	}
	
	public  DBCollection createCollection(String collname) {
		if (db.collectionExists(collname)) {
			return db.getCollection(collname);
		}
		BasicDBObject doc = new BasicDBObject("capped", true);
		doc.append("size",  5242880);
		doc.append("max", 5000);
		return db.createCollection(collname, doc );	
	}
	
	public BasicDBObject createDoc() {
		return new BasicDBObject();
	}
	
	public BasicDBObject insertSelect(String mongoCollection, BasicDBObject doc, String attrib, String prop) {
		DBCollection coll = db.getCollection(mongoCollection);
		if (doc != null)
			doc.append(attrib, prop);
		System.out.println(doc);
		coll.insert(doc);
		return doc;
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
		MongoAccess mongo = new MongoAccess();
		System.out.println("=== ssh state: " + mongo.checkMongod());
//		if (mongo.checkMongoServer() != 0) {
//			System.err.println("...server not running!");
//			System.exit(101);
//		}
		
		if (!mongo.connect("sonet"))
			System.exit(105);
		DBCollection coll = mongo.createCollection("netgraf");
		BasicDBObject doc = mongo.createDoc();
		doc = mongo.insertSelect("netgraf", doc, "vertex", "src");
		mongo.manipulate();
		mongo.close();
	}

}
