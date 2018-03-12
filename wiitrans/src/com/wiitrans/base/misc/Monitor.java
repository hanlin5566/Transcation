package com.wiitrans.base.misc;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.io.File;

import com.wiitrans.base.log.Log4j;

public class Monitor {

	private String _localPath = null;
	private WatchService _ws = null;

	enum EVENT {
		NONE, CREATE, MODIFY, DELETE
	};

	public Monitor() {
	}

	public String GetLocalPath() {
		return _localPath;
	}

	public int SetLocalPath(String localPath) {
		int ret = -1;

		try {
			File tmp = new File(localPath);
			if (tmp.exists()) {
				_localPath = localPath;

				ret = 0;
			}
		} catch (Exception e) {
			Log4j.error(e);
		}

		return ret;
	}

	public int Start() {
		int ret = -1;

		try {
			_ws = FileSystems.getDefault().newWatchService();
			Path path = Paths.get(_localPath);
			path.register(_ws, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY,
					StandardWatchEventKinds.ENTRY_DELETE);

		} catch (Exception e) {
			Log4j.error(e);
		}

		return ret;
	}

	public int Stop() {
		int ret = -1;

		try {

			_ws.close();
			ret = 0;

		} catch (Exception e) {
			Log4j.error(e);
		}

		return ret;
	}
	
	public int WaitAll(ArrayList<String> files) {
		int ret = -1;
		
		// TODO
		
		return ret;
	}

	@SuppressWarnings("unchecked")
	public int Wait(EVENT event, ArrayList<String> files) {
		int ret = -1;
		
		if(EVENT.NONE != event)
		{
			try
			{
				WatchKey key;
				key = _ws.take();

				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					Kind<?> kind = watchEvent.kind();
					if (kind == StandardWatchEventKinds.OVERFLOW) {
						continue;
					}
					if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
						if(EVENT.CREATE != event)
						{
							continue;
						}
					}
					if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
						if(EVENT.MODIFY != event)
						{
							continue;
						}
					}
					if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
						if(EVENT.DELETE != event)
						{
							continue;
						}
					}				

					WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
					files.add(watchEventPath.context().toString());
				}

				boolean valid = key.reset();
				if (!valid) {
					ret = -1;
				}

			} catch (Exception e) {
				Log4j.error(e);
			}
		}
		
		return ret;
	}
}
