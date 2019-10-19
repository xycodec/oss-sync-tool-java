package com.xycode.sync_tool.internel;
/**
 * 是为MyFileUtils服务的工具类,不建议其它程序调用
 */
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.io.FileUtils;

import com.xycode.sync_tool.config.Config;
import com.xycode.sync_tool.utils.MyFileUtils;
import com.xycode.sync_tool.utils.MyLogger;

public class GenerateCache implements Runnable{
	private List<File> src_file_list;//源目录
	private List<File> cache_file_list;//是根据原目录与缓存目录拼接后所产生的缓存文件列表
	
	private static final ReentrantReadWriteLock lock=new ReentrantReadWriteLock();
	private static final WriteLock write_lock=lock.writeLock();
	private static final ReadLock read_lock=lock.readLock();
	
	public static CountDownLatch latch=new CountDownLatch(Config.CThread_num);
	
	public GenerateCache(List<File> src_file_list, List<File> cache_file_list) {
		super();
		this.src_file_list = src_file_list;
		this.cache_file_list = cache_file_list;
	}

	@Override
	public void run() {
		//generate_cache
		assert src_file_list.size()==cache_file_list.size();
		try {
			for(int i=0;i<src_file_list.size();++i) {
				String filepath=MyFileUtils.format(cache_file_list.get(i).toString());
				File dir=new File(filepath.substring(0, filepath.lastIndexOf('/')+1));
				boolean exists=false;
				read_lock.lock();
				try {
					exists=dir.exists();
				}finally {
					read_lock.unlock();
				}
				if(!exists) {//不存在则创建
					//两种创建文件夹的方法,注意方法名后面有s,可以一次性创建多个嵌套的文件夹
					boolean flag=false;
					write_lock.lock();
					try {
						flag=dir.mkdirs();//写入的过程,不可以被其他正在mkdirs或读取exist状态的线程打断
					}finally {
						write_lock.unlock();
					}
//					try {
//						Files.createDirectories(dir.toPath());
//					} catch (IOException e) {
//						MyLogger.logger.warning("Fail to create directory: "+dir+" !");
//						e.printStackTrace();
//					}
					
					if(!flag) {//创建失败可能是文件夹已存在
						MyLogger.logger.warning("Fail to create directory: "+dir+", directory exsits!");
					}
				}
				
				if(MyFileUtils.newer(src_file_list.get(i),cache_file_list.get(i))) {//源文件较新时才更新
//					if(!cache_file_list.get(i).delete()) {
//						MyLogger.logger.warning("can't delete "+cache_file_list.get(i));
//						continue;
//					}
					//两种copy方式
					try {
						Files.copy(src_file_list.get(i).toPath(), 
								cache_file_list.get(i).toPath(),
								StandardCopyOption.REPLACE_EXISTING);//option: 文件已存在的话就替换
						//设置时间戳
						//cache_file_list.get(i).setLastModified(src_file_list.get(i).lastModified());
						MyLogger.logger.info("copy "+src_file_list.get(i)+" -> "+cache_file_list.get(i));
					} catch (IOException e1) {
						MyLogger.logger.warning("can't copy "+src_file_list.get(i)+" to "+cache_file_list.get(i).toPath());
						e1.printStackTrace();
					}
//					try {
//						FileUtils.copyFile(src_file_list.get(i), cache_file_list.get(i));
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
				}
					
			}
		}finally {
			latch.countDown();//完成了一个任务
		}

	}
	

	public static void main(String[] args) throws IOException {
//		System.out.println(new File("E:/$WorkSpace$/VS2017 Projects/Python Projects/oss2_sync_tool_public/").exists());
//		File s=new File("E:/VM Share/ss");//win系统下的硬盘的根目录默认禁止写入
//		s.createNewFile();
		
//		List<File> src_file_list=FileScan.source_list;
//		List<File> cache_file_list=new ArrayList<File>();
//		for(int i=0;i<src_file_list.size();++i) {
//			cache_file_list.add(new File(MyFileUtils
//					.concatPath(src_file_list.get(i),Config.local_workspace_name,Config.temp_path)));
//		}
//		System.out.println(src_file_list.size());
//		for(int i=0;i<cache_file_list.size();++i) {
//			System.out.println(cache_file_list.get(i));
//		}
//		Thread t1=new Thread(new GenerateCache(src_file_list,cache_file_list));
//		
//		t1.start();
//		try {
//			t1.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	
	}
}
