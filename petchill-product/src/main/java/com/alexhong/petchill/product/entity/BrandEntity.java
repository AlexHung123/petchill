package com.alexhong.petchill.product.entity;

import com.alexhong.common.valid.AddGroup;
import com.alexhong.common.valid.ListValue;
import com.alexhong.common.valid.UpdateGroup;
import com.alexhong.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 21:13:39
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@NotNull(message = "", groups = {UpdateGroup.class})
	@Null(message = "", groups = {AddGroup.class})
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotEmpty(message = "Brank Name should not be blank", groups = {AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotEmpty(groups = {AddGroup.class})
	@URL(message = "logo should be a valid URL address", groups = {AddGroup.class, UpdateGroup.class})
	@NotEmpty
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */

	@ListValue(vals = {0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-z]$", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull
	@Min(value = 0, message = "Sort should not be less than 0")
	private Integer sort;

}
