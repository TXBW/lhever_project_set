/*
 * @copyright Copyright (c) 2015-2015 iWonCloud Tech Co., LTD
 * @license http://www.iwoncloud.com/code/license
 * @author dengdan
 * @date 2015-7-13 上午9:29:43
 * @version v1.0
 */
package com.iwoncloud.familyface.server.test.user;

import static com.iwoncloud.familyface.server.common.constants.UserBehaviorTargetType.ARTICLE;
import static com.iwoncloud.familyface.server.common.constants.UserBehaviorTargetType.PHOTO_BATCH;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.iwoncloud.familyface.server.common.constants.ErrorCodeDic;
import com.iwoncloud.familyface.server.test.ResourceTest;
import com.iwoncloud.familyface.server.user.dao.UserCommentDao;
import com.iwoncloud.familyface.server.user.pojo.UserComment;
import com.iwoncloud.familyface.server.user.resource.UserCommentResource;
import com.iwoncloud.familyface.server.user.resource.UserLikeResource;
import com.iwoncloud.familyface.server.user.resource.impl.UserCommentResourceImpl;
import com.iwoncloud.familyface.server.user.service.UserCommentService;
 
/**
 * @author lihong 2015-8-13 下午5:57:45
 * @version v1.0
 */
public class UserCommentResourceTest extends ResourceTest<UserCommentResourceImpl>
{
	
//  token的格式为： 32位的token + 32位的userId	
	
	@Autowired
	private UserCommentService service;
	
	@Autowired
	private UserCommentDao dao;
	
	
	String targetId = "04131945c33d4d0faaa225d5581dda79";
	
	private void setUserComment(String userId, short targetType, String targetId, String content, Integer user, String nickname)
	{
        resetTarget(userId);
		addFormParam("targetType", targetType);
		addFormParam("targetId", targetId);
		addFormParam("content", content);
		addFormParam("nickname",nickname);
//		addFormParam("repliedCommentId",repliedCommentId);
		applyAuth(user);
		postAndRead();
	}
	
	@Test
	public void testComment() {
		resetTarget("/" + userId);
		String content = "当前版本无法查看哈哈";
		
		/**
		 * 评论文章
		 */
		setUserComment(userId, ARTICLE, targetId, content, USER, "牛X文章");
		assertOK();
		
		/**
		 * 测试评论相册
		 */
		setUserComment(userId, PHOTO_BATCH, targetId, content, USER, "牛X相册");
		assertOK();
		
		/**
		 * 权限测试
		 */
		setUserComment(userId, ARTICLE, targetId, content, null, "牛XXXXX");
		assertNoAuth();
		

		/**
		 * ---- 验证参数：昵称过长
		 */
		setUserComment(userId, PHOTO_BATCH, targetId, content, USER, "牛XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		assertError(UserCommentResource.NICKNAME_TOO_LONG);
	}
	
	
	
	
	private void deleteComment(String userId, String commentId,Integer user) {
		resetTarget("/" + userId);
		addQueryParam("userId", userId);
		addQueryParam("commentId",commentId);
		applyAuth(user);
		deleteAndRead();
	}
	
	/**
	 * 测试是否删除成功
	 * @param userId
	 * @param commentId void
	 * @author lihong 2015-8-13 下午8:55:23
	 * @since v1.0
	 */
	@Test
	public void testDeleteComment()
	{
        /**
         * 正常验证
         */
        deleteComment(userId, (String)getCommentId(0).get("id"),USER);  //删除数据
        assertOK();
		
		
//       /**
//         * 权限验证
//         */
//        deleteComment(userId, (String)getCommentId(0).get("id"),null);  //删除数据
//        assertNoAuth();
//		
//	    /**
//		 * 参数验证--userId不能为空
//		 */
//        deleteComment(null, (String)getCommentId(0).get("id"),USER);  //删除数据
//        assertError(UserCommentResource.USERID_CANNOT_BE_BLANK);	
//	
//		/**
//		 * 业务异常--验证是否能删除别人的评论（权限）
//		 */
//        deleteComment("da9dad5ce8f440cfb3d4685f3473xxxx", (String)getCommentId(0).get("id"), USER);  //删除数据
//        assertBusiException(ErrorCodeDic.ERROR_NO_SUCH_AUTH);
//        
//        /**
//         * 业务异常--验证评论存在与否
//         */
//        deleteComment("da9dad5ce8f440cfb3d4685f3473xxxx", (String)getCommentId(0).get("id"),USER); 
//        assertBusiException(ErrorCodeDic.ERROR_NO_SUCH_COMMENT);
	}
	
	
	
	private LinkedHashMap getCommentId(int i)
	{
		getCommentInPage(ARTICLE, targetId, 1, 5, 1000L, USER);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<LinkedHashMap> list = (List<LinkedHashMap>) result.getData().get("result");
		return (LinkedHashMap) list.get(i);
	}
	
	/**
	 * 添加查询参数
	 * @param userId
	 * @param commentId
	 * @param targetType
	 * @param targetId
	 * @param start
	 * @param limit
	 * @param timestamp
	 * @param user void
	 * @author lihong 2015-8-14 下午5:55:21
	 * @since v1.0
	 */
	private void getCommentInPage(Short targetType, String targetId, Integer start, Integer limit,
	        Long timestamp, Integer user)
	{
		resetTarget(ARTICLE +"/" + targetId);
		addQueryParam("targetType", targetType);
		addQueryParam("targetId", targetId);
		addQueryParam("start", start);
		addQueryParam("limit", limit);
		addQueryParam("timestamp", timestamp);
		applyAuth(user);
		getAndRead();
	}

	@Test
	public void testGetCommentInPage()
	{
		/**
		 * 正常验证-文章评论
		 */
		getCommentInPage(ARTICLE, targetId, 1, 5, 1439395200L, USER);
		assertOK();
		/**
		 * 正常验证--相片集
		 */
		getCommentInPage(PHOTO_BATCH, targetId, 1, 5, 1439395200L, USER);
		assertOK();

		/**
		 * 参数验证
		 */
		getCommentInPage(new Short("99"), targetId, 1, 5, 1439395200L, USER);
		assertError(UserCommentResource.MSG_INVALID_TARGET_TYPE);

		/**
		 * 权限验证
		 */
		getCommentInPage(new Short("99"), targetId, 1, 5, 1439395200L, null);
		assertNoAuth();

		/**
		 * 业务异常
		 */
	}
	
}
