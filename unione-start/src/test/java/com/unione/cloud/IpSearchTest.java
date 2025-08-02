package com.unione.cloud;

import com.unione.cloud.core.util.RequestUtils;
import com.unione.cloud.core.util.RequestUtils.ClientLocation;


public class IpSearchTest {
    public static void main(String[] args) {
		ClientLocation location = RequestUtils.getClientLocation("113.70.88.158");
	    System.out.println("根据ip获取位置信息,ip:113.70.88.158,location:"+location);
	}
}
