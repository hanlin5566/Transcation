package com.wiitrans.base.filesystem;

import java.io.File;
import java.io.FileInputStream;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import com.wiitrans.base.log.Log4j;

public class FastDFS {
	public static void main(String[] args) {
		FastDFS dfs = null;
		try {
			dfs = new FastDFS();
			dfs.Init();
//			String fileid = dfs.Upload("/root/Desktop/file/36_preprocess.xml",
//					"xml");
//			System.out.println(fileid);
//			String fileid1 = dfs.Upload("/root/Desktop/file/en-fnot-0721.doc",
//					"doc");
//			System.out.println(fileid1);

			dfs.Download("M00/10/05/wKgJzFbrVa2AWUwyAAAbHeSo9ZY320.xml",
					"/root/Desktop/fenxi/xiazai.xml");

		//	dfs.Download(fileid, "/root/Desktop/fenxi/test234234234234.doc");
			dfs.UnInit();
		} catch (Exception e) {
			Log4j.error(e.getMessage());
		} finally {
			if (dfs != null) {
				dfs.UnInit();
			}
		}

	}

	private TrackerClient trackerClient;
	private TrackerServer trackerServer;
	private StorageServer storageServer;
	private StorageClient storageClient;

	public void Init() {
		try {
			String classPath = "/etc/fdfs";
			String configFilePath = classPath + File.separator + "client.conf";
			ClientGlobal.init(configFilePath);

			this.trackerClient = new TrackerClient();
			this.trackerServer = trackerClient.getConnection();
			this.storageServer = null;
			this.storageClient = new StorageClient(trackerServer, storageServer);
		} catch (Exception ex) {
			Log4j.error(ex);
		}
	}

	public void Download(String fileid, String local_filename) throws Exception {
		try {
		    	//sdlxliff 特殊处理
			if(fileid.indexOf("sdlxliff")>=0){
			    fileid = fileid.replace(".sdlxliff", "");
			}
			String remote_filename = fileid;
			storageClient.download_file("group1", remote_filename,
					local_filename);
		} catch (Exception ex) {
			Log4j.error(ex);
		}
	}

	public void Delete(String fileid) throws Exception {
		try {
			String remote_filename = fileid;
			storageClient.delete_file("group1", remote_filename);
		} catch (Exception ex) {
			Log4j.error(ex);
		}
	}

	public String Upload(String local_filename, String ext) throws Exception {

		File file = new File(local_filename);
		FileInputStream fis = new FileInputStream(file);
		byte[] file_buff = null;
		if (fis != null) {
			int len = fis.available();
			file_buff = new byte[len];
			fis.read(file_buff);
		}

		try {
			String[] results = storageClient.upload_file(file_buff, ext, null);

			if (results == null || results.length < 2) {
				System.err.println("upload file fail, error code: "
						+ storageClient.getErrorCode());
				return "";
			} else {
				return results[1];
			}

		} catch (Exception e) {
			// System.err.println(e.toString());
			Log4j.error(e);
		}
		// finally {
		// if (trackerServer != null) {
		// trackerServer.close();
		// trackerServer = null;
		// }
		// if (storageServer != null) {
		// storageServer.close();
		// storageServer = null;
		// }
		// }

		return "";
	}

	public void UnInit() {
		try {
			if (trackerServer != null) {
				trackerServer.close();
				trackerServer = null;
			}
			if (storageServer != null) {
				storageServer.close();
				storageServer = null;
			}
		} catch (Exception ex) {
			Log4j.error(ex);
		}
	}

}
