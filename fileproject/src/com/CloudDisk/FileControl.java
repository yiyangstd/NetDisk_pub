package com.CloudDisk;

import com.kuaipan.client.KuaipanAPI;
import com.kuaipan.client.exception.KuaipanAuthExpiredException;
import com.kuaipan.client.exception.KuaipanIOException;
import com.kuaipan.client.exception.KuaipanServerException;
import com.kuaipan.client.model.KuaipanFile;
import com.kuaipan.client.model.KuaipanHTTPResponse;
import com.kuaipan.client.session.OauthSession;
import com.kuaipan.test.KPTestUtility;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

public class FileControl {
	private KuaipanAPI api = null;
	private String path = "/";
	private BufferedWriter stdout;
	private BufferedReader stdin;
	private String prompt = ">";
	
	public FileControl(OutputStream stdout, InputStream stdin){
		OauthSession session = new OauthSession(KPTestUtility.CONSUMER_KEY, 
				KPTestUtility.CONSUMER_SECRET, OauthSession.Root.APP_FOLDER);
		api = new KuaipanAPI(session);
		try {
			this.stdout = new BufferedWriter(new OutputStreamWriter(stdout, System.getProperty("file.encoding")));
			this.stdin = new BufferedReader(new InputStreamReader(stdin, System.getProperty("file.encoding")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public FileControl(BufferedWriter stdout, BufferedReader stdin){
		OauthSession session = new OauthSession(KPTestUtility.CONSUMER_KEY, 
				KPTestUtility.CONSUMER_SECRET, OauthSession.Root.APP_FOLDER);
		api = new KuaipanAPI(session);
		this.stdout = stdout;
		this.stdin = stdin;
	}
	
	public void do_ls() throws KuaipanIOException, KuaipanServerException, KuaipanAuthExpiredException {
		KuaipanFile file = api.metadata(path, true);		
		for (Iterator<KuaipanFile> it=file.files.iterator(); it.hasNext();) {
			KuaipanFile temp = it.next();
			println(temp.name);
		}
	}
	
	public void do_cd(String dir) {
		this.path = joinPath(dir);
	}
	
	public void do_mkdir(String dir) throws KuaipanIOException, KuaipanServerException, KuaipanAuthExpiredException {
		api.createFolder(joinPath(dir));
	}
	
	public void do_rm(String dir) throws KuaipanIOException, KuaipanServerException, KuaipanAuthExpiredException {
		api.delete(joinPath(dir));
	}
	
	public void do_upload(String dir, String file_location) 
			throws KuaipanIOException, KuaipanServerException, KuaipanAuthExpiredException {
		File file = new File(file_location);
		long size = file.length();

		InputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {}
		KuaipanFile kpfile = api.uploadFile(joinPath(dir), is, size, true, null);
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//println(kpfile.toString());
	}
	
	public boolean do_download(String file_location, String dir)
			throws KuaipanIOException, KuaipanAuthExpiredException, KuaipanServerException{
		File file = new File(file_location);
		OutputStream os = null;
		try{
			os = new FileOutputStream(file);
		}catch (FileNotFoundException e) {}
		KuaipanHTTPResponse resp = api.downloadFile(joinPath(dir), os, null);
		//assertTrue(resp.code == 200);
		
		//println(resp.toString());
		if(200 == resp.code){
			try {
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		else
			return false;
	}
	
	public void do_cat(String dir) throws KuaipanIOException, KuaipanServerException, KuaipanAuthExpiredException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		KuaipanHTTPResponse resp = api.downloadFile(joinPath(dir), os , null);
		//assertTrue(resp.code == 200);
		String download_content = KPTestUtility.outputStream2String(os);
		try {
			os.close();
		} catch (IOException e) {}
		println(download_content);
	}
	
	public void do_login() throws KuaipanIOException, KuaipanServerException, KuaipanAuthExpiredException {
		String auth_url = api.requestToken();
		openBrowser(auth_url);
		println(auth_url);
		print("Visit this url and authorize the client, then press ENTER to continue.");
		waitForInput();
		api.accessToken();
		System.out.println("login success");
	}
	
	private String joinPath(String dir) {
		if (dir.startsWith("/")) 
			return dir;
		else if (dir.equals("..")) {
			String[] path_slices = this.path.split("/");
			if (path_slices.length > 1)
				return stringJoin(path_slices, '/', 1, path_slices.length-1);
			return "/";
		}
		else {
			if (!path.endsWith("/"))
				return path + "/" + dir;
			return this.path + dir;
		}
	}
	
	private void empty() {
		print(path + prompt);
	}
	
	private void println(String str) {
		print(str+"\n");
	}
	
	private void print(String str) {
		try {
			stdout.write(str);
			stdout.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void openBrowser(String url) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
		}
		try {
			java.awt.Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
		}
	}
	
	private void waitForInput() {
		readOneLine();
	}
	
	private String[] readOneLine() {
		try {
			return stdin.readLine().split(" ");
		} catch (IOException e) {}
		return null;
	}
	
	private String stringJoin(String[] seq, char c, int start, int end) {
		if (start >= end) return "/";
		StringBuffer buf = new StringBuffer();
		for (int i=start; i<end; i++) {
			buf.append(c);
			buf.append(seq[i]);			
		}
		return buf.toString();
	}

}
