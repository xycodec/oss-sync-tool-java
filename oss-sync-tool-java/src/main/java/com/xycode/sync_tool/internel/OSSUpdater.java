package com.xycode.sync_tool.internel;
/*
 * 为MyOSSUtils服务的工具类,不建议其它程序调用
 */
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.comm.ResponseMessage;
import com.xycode.sync_tool.config.Config;
import com.xycode.sync_tool.utils.MyLogger;
import com.xycode.sync_tool.utils.MyOSSUtils;

public class OSSUpdater implements Runnable{
	public static CountDownLatch latch=new CountDownLatch(Config.UThread_num);
	private static final OSS ossClient = new OSSClientBuilder()
			.build(Config.endpoint, Config.accessKeyId, Config.accessKeySecret);
	private List<File> src_file_list;
	private List<File> cache_file_list;
	private List<String> cloud_file_list;
	
	private static volatile boolean isUpdate;//当为false时就不更新,但是要保存待更新的列表
	private static List<File> update_list=new ArrayList<>();//待更新文件列表
	
	public OSSUpdater(List<File> src_file_list, List<File> cache_file_list, List<String> cloud_file_list) {
		super();
		this.src_file_list = src_file_list;
		this.cache_file_list = cache_file_list;
		this.cloud_file_list = cloud_file_list;
	}
	
	public static List<File> getUpdateList() {
		return update_list;
	}
	
	public static void clearUpdateList() {
		update_list.clear();
	}
	
	
	public static void setIsUpdate(boolean _isUpdate) {
		isUpdate=_isUpdate;
	}
	
	public static boolean getIsUpdate() {
		return isUpdate;
	}

	public static void shutdown() {
		ossClient.shutdown();
		MyLogger.logger.info("ossClient shutdown!");
	}

	@Override
	public void run() {
		assert cache_file_list.size()==cloud_file_list.size();
		try {
			for(int i=0;i<cache_file_list.size();++i) {
				boolean exists = ossClient.doesObjectExist(Config.bucket_name, cloud_file_list.get(i));
				//System.out.println(cloud_file_list.get(i)+" : "+exists);
				if(exists) {
					ResponseMessage response=ossClient.getObject(Config.bucket_name, cloud_file_list.get(i)).getResponse();
					if(response.getStatusCode()!=200) {
						MyLogger.logger.warning("fail to access "+Config.bucket_name+" : "+cloud_file_list+".");
						continue;
					}
					long cloud_timestamp=MyOSSUtils.dateToNum(response.getHeaders().get("Last-Modified"));//注意这里有时区的转换GMT -> CST
					if(cloud_timestamp<(cache_file_list.get(i).lastModified()/1000)) {//本地文件较新的话才更新
						if(isUpdate) {
							ossClient.putObject(Config.bucket_name, 
									cloud_file_list.get(i), cache_file_list.get(i));//更新到云端
							MyLogger.logger.info("update "+src_file_list.get(i)+" -> "+cloud_file_list.get(i)+".");
						}else {
							update_list.add(src_file_list.get(i));
							MyLogger.logger.info(src_file_list.get(i)+", needs to update, it's SIZE: "+src_file_list.get(i).length()/1000+" KB.");
						}
					}else {
						MyLogger.logger.fine(src_file_list.get(i)+ ", doesn't need update.");
					}
					
				}else {//云端不存在对应文件的话就直接上传即可
					if(isUpdate) {
						ossClient.putObject(Config.bucket_name, 
								cloud_file_list.get(i), cache_file_list.get(i));//更新到云端
						MyLogger.logger.info("upload "+cache_file_list.get(i)+" -> "+cloud_file_list.get(i)+".");
					}else {
						update_list.add(src_file_list.get(i));
						MyLogger.logger.info(src_file_list.get(i)+" needs to update, it's SIZE: "+src_file_list.get(i).length()/1000.0+" KB.");
					}
				}
			}
		}finally {
			latch.countDown();//完成一个任务
		}
	}
	
	public static void main(String[] args) {
		System.out.println(Config.endpoint);
		System.out.println(Config.accessKeyId);
		System.out.println(Config.accessKeySecret);
		//Wed, 17 Jul 2019 08:07:56 GMT
		String dateFormatStr="E, dd MMM yyyy HH:mm:ss z";
		SimpleDateFormat sdf=new SimpleDateFormat(dateFormatStr,Locale.US);
		System.out.println(sdf.format(new Date(System.currentTimeMillis())));
		try {
			System.out.println(sdf.parse("Wed, 17 Jul 2019 08:07:56 GMT").getTime()-28800000);
			System.out.println(sdf.parse("Wed, 17 Jul 2019 08:07:56 CST").getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}


}
