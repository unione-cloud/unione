package com.unione.cloud.common.service;

import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.unione.cloud.common.dto.DocFileDto;
import com.unione.cloud.common.model.DocFile;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.feign.api.FeignDelete;
import com.unione.cloud.core.feign.api.FeignDetail;
import com.unione.cloud.core.feign.api.FeignFind;
import com.unione.cloud.core.feign.api.FeignFindById;
import com.unione.cloud.core.feign.api.FeignSave;

import io.swagger.v3.oas.annotations.Operation;



public interface DocFileService extends FeignSave<DocFileDto>,FeignDelete<DocFile>,FeignFind<DocFile>,FeignFindById<DocFile>,FeignDetail<DocFile>{
	
	   @PostMapping({"/set/owner/{ownerId}"})
	   @Operation(summary = "设置文件归属",description = "参数body中未文件id集合，返回body为成功数")
	   public Results<Integer> setOwner(@RequestBody Set<Long> ids,@PathVariable("ownerId") Long ownerId);
	
	   
	   @PostMapping({"/find/mine"})
	   @Operation(summary = "我的文件",description = "只查询自己上传的文件")
	   public Results<List<DocFile>> findMine(@RequestBody Params<DocFileDto> params);
	   
	   
	   @PostMapping({"/find/other"})
	   @Operation(summary = "他人的公开文件",description = "查询他人的公开的文件，查询自己租户下其他人上传的公开文件")
	   public Results<List<DocFile>> findOther(@RequestBody Params<DocFile> params);
		
		
	   @PostMapping("/find/mineShare")
	   @Operation(summary = "我共享的文件",description="")
	   public Results<List<DocFile>> findMineShare(@RequestBody Params<DocFile> params);
		
		
	   @PostMapping("/find/shareMine")
	   @Operation(summary = "共享给我的文件",description="")
	   public Results<List<DocFileDto>> findShareMine(@RequestBody Params<DocFileDto> params);
	   
}
