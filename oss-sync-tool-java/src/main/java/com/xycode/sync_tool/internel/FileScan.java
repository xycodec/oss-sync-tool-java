package com.xycode.sync_tool.internel;
/**
 * 为MyFileUtils服务的工具类,不建议其它程序调用
 */
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.xycode.sync_tool.config.Config;
import com.xycode.sync_tool.utils.MyFileUtils;
import com.xycode.sync_tool.utils.MyLogger;
public class FileScan{
	public static List<File> source_list=new ArrayList<File>();
	static {
		init();
	}
	
	private static void init() {
		source_list=getAllPath(Config.local_path_list);
	}
	
	public static ArrayList<File> scan(String folderPath){
		ArrayList<File> result = new ArrayList<>();
		File dir = new File(folderPath);
		if(!dir.isDirectory()){
			MyLogger.logger.warning(folderPath + " is not a directory!");
		}else {
			File[] filelist = dir.listFiles();
			for(int i=0; i<filelist.length;++i){
				//如果当前是文件夹，递归扫描文件夹
				String filepath=MyFileUtils.format(filelist[i].getAbsolutePath());
				if(filelist[i].isDirectory()){
					//递归扫描下面的文件夹
					String[] tmp=filepath.split("/");
					if(tmp[tmp.length-1].charAt(0)!='.')//.开头的文件夹多半是缓存与配置文件夹,故不去扫描它
						result.addAll(scan(filepath));//这里尤其注意，要不子任务的结果添加进来
				}else{
					if(Config.included_suffix.contains(MyFileUtils.getSuffix(filepath).toLowerCase())) {
						result.add(new File(filepath));
						//source_list.add(new File(filepath));
						//System.out.println(filepath);
					}
				}
			}
		}
		return result;
	}
	
	public static List<File> getAllPath(List<String> folderPath) {
		List<File> result=new ArrayList<File>();
		for(String path: folderPath) {
			result.addAll(scan(path));
		}
		return result;

	}
	
	public static void main(String[] args) {
		for(File f:source_list) {
			System.out.println(f);
		}
	}

}
