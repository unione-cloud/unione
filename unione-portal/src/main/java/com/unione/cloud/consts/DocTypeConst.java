package com.unione.cloud.consts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.ListUtil;

public class DocTypeConst {

    public static Map<String, List<String>> DOC_TYPE_MAP = new HashMap<>();
    static{
        List<String> TXT_SUFFIX = Arrays.asList("txt", "html", "css","java","js","xml","sql","json","properties","md","conf","ini","vue","php","py","bat","gitignore","log","htm","tsv","cnf");
        List<String> DOC_SUFFIX = Arrays.asList("pdf", "odt", "ott", "sxw", "doc", "docx", "rtf", "wpd", "ods", "ots", "sxc", "xls", "xlsx", "odp", "otp", "sxi", "ppt", "pptx", "odg", "otg");
        List<String> AUDIO_SUFFIX = Arrays.asList("mp3", "ogg", "wav", "ape", "cda", "au", "midi", "mac", "aac");
        List<String> VIDEO_SUFFIX = Arrays.asList("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm");
        List<String> IMAGE_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif");

        DOC_TYPE_MAP.put("img", IMAGE_SUFFIX);
        DOC_TYPE_MAP.put("audio", AUDIO_SUFFIX);
        DOC_TYPE_MAP.put("video", VIDEO_SUFFIX);
        DOC_TYPE_MAP.put("doc", DOC_SUFFIX);
        DOC_TYPE_MAP.put("txt", TXT_SUFFIX);
    }

    public static List<String> getSuffix(String docType){
        List<String> suffixList = DOC_TYPE_MAP.get(docType);
        if(suffixList!=null){
            return new ArrayList<>(suffixList);
        }
        return ListUtil.empty();
    }

}
