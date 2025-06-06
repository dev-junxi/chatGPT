
package org.springblade.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.entity.Post;
import org.springblade.system.service.IPostService;
import org.springblade.system.vo.PostVO;
import org.springblade.system.wrapper.PostWrapper;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.springblade.core.cache.constant.CacheConstant.SYS_CACHE;

/**
 * 岗位表 控制器
 *
 *
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/post")
@Api(value = "岗位", tags = "岗位")
public class PostController extends BladeController {

	private final IPostService postService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入post")
	public R<PostVO> detail(Post post) {
		Post detail = postService.getOne(Condition.getQueryWrapper(post));
		return R.data(PostWrapper.build().entityVO(detail));
	}

	/**
	 * 分页 岗位表
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入post")
	public R<IPage<PostVO>> list(Post post, Query query) {
		IPage<Post> pages = postService.page(Condition.getPage(query), Condition.getQueryWrapper(post));
		return R.data(PostWrapper.build().pageVO(pages));
	}


	/**
	 * 自定义分页 岗位表
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "分页", notes = "传入post")
	public R<IPage<PostVO>> page(PostVO post, Query query) {
		IPage<PostVO> pages = postService.selectPostPage(Condition.getPage(query), post);
		return R.data(pages);
	}

	/**
	 * 新增 岗位表
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入post")
	public R save(@Valid @RequestBody Post post) {
		CacheUtil.clear(SYS_CACHE);
		return R.status(postService.save(post));
	}

	/**
	 * 修改 岗位表
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入post")
	public R update(@Valid @RequestBody Post post) {
		CacheUtil.clear(SYS_CACHE);
		return R.status(postService.updateById(post));
	}

	/**
	 * 新增或修改 岗位表
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改", notes = "传入post")
	public R submit(@Valid @RequestBody Post post) {
		CacheUtil.clear(SYS_CACHE);
		return R.status(postService.saveOrUpdate(post));
	}


	/**
	 * 删除 岗位表
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		CacheUtil.clear(SYS_CACHE);
		return R.status(postService.deleteLogic(Func.toLongList(ids)));
	}

	/**
	 * 下拉数据源
	 */
	@GetMapping("/select")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "下拉数据源", notes = "传入post")
	public R<List<Post>> select(String tenantId, BladeUser bladeUser) {
		List<Post> list = postService.list(Wrappers.<Post>query().lambda().eq(Post::getTenantId, Func.toStrWithEmpty(tenantId, bladeUser.getTenantId())));
		return R.data(list);
	}

	/**
	 * 根据岗位编号删除岗位
	 */
	@PostMapping("/remove-by-postCode")
	@ApiOperationSupport(order = 9)
	@ApiOperation(value = "根据岗位编号删除", notes = "传入postCode")
	public R removeByPostCode(@ApiParam(value = "岗位编号", required = true) @RequestParam String postCode) {
		CacheUtil.clear(SYS_CACHE);
		// 先查询出符合条件的岗位
		List<Post> posts = postService.list(Wrappers.<Post>query().lambda().eq(Post::getPostCode, postCode));
		if (posts == null || posts.isEmpty()) {
			return R.fail("未找到对应的岗位");
		}
		// 提取岗位ID并进行逻辑删除
		List<Long> ids = posts.stream().map(Post::getId).collect(Collectors.toList());
		return R.status(postService.deleteLogic(ids));
	}

}
