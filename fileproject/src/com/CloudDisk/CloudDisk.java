package com.CloudDisk;

import java.io.File;
import java.net.URL;
import java.util.Set;

/**
 * ����һ������������࣬ÿ����������̣����ɽ���̡�dropbox�ȣ���������һ������ʵ��
 */
public abstract class CloudDisk {

	static Set<CloudDisk> diskSet;

	File filename;
	String diskName;// �����̵�����
	URL url;// �����̵ĵ�ַ
	// ...... ���������̵�����������

	String usermame;
	String password;
	String token;
	String secrect;
	// ...... ���������̵�����������

	int speed;
	int capacity;
	int reliability;

	// ...... ���������̵�QOS����

	abstract boolean available();

	abstract boolean login();

	abstract boolean getUserInfo();

	abstract boolean uploadFile();

	abstract boolean listDir();

	abstract boolean getList();// ��ȡ������ȫ���ļ���list��ĳЩ���̿��ܲ�֧��

	abstract boolean removeFile();

	abstract boolean downloadFile();

	abstract boolean getFileInfo();
	// ..... �������̵��û����ļ���������

}

