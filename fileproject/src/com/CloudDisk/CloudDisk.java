package com.CloudDisk;

import java.io.File;
import java.net.URL;
import java.util.Set;

/**
 * 这是一个抽象的云盘类，每个具体的云盘（如金山网盘、dropbox等）都是他的一个具体实现
 */
public abstract class CloudDisk {

	static Set<CloudDisk> diskSet;

	File filename;
	String diskName;// 该云盘的名字
	URL url;// 该云盘的地址
	// ...... 其他该云盘的描述性属性

	String usermame;
	String password;
	String token;
	String secrect;
	// ...... 其他该云盘的连接性属性

	int speed;
	int capacity;
	int reliability;

	// ...... 其他该云盘的QOS属性

	abstract boolean available();

	abstract boolean login();

	abstract boolean getUserInfo();

	abstract boolean uploadFile();

	abstract boolean listDir();

	abstract boolean getList();// 获取网盘上全部文件的list，某些网盘可能不支持

	abstract boolean removeFile();

	abstract boolean downloadFile();

	abstract boolean getFileInfo();
	// ..... 其他云盘的用户和文件操作方法

}

