package com.envisioncn.apiserver;

import java.io.File;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledService {
	
	@Autowired
	ConfigBean configBean;

	@Scheduled(cron = "0 0/1 * * * ?")
    public void deleteFiles(){
		Instant t = Instant.now();
		long epochMilli = t.toEpochMilli();
        System.out.println("UTC time now: " + t);
        
        File rootDir = new File(configBean.getRootpath());
        File[] userDirs = rootDir.listFiles();
        for (File userDir: userDirs) {
        	File[] csvFiles = userDir.listFiles();
        	for (File csvFile: csvFiles) {
        		if (csvFile.isFile()) {
        			if (epochMilli - Long.parseLong(csvFile.getName().split("_")[0]) > configBean.getRetentionperiod() * 24 * 60 * 60 * 1000 ) {
            			System.out.println(csvFile.getName() + " is deleted.");
            			csvFile.delete();
            		}
        		}
        	}
        }
    }
}
