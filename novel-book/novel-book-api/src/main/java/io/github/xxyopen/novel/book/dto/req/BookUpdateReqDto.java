package io.github.xxyopen.novel.book.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "小说更新请求DTO")
public class BookUpdateReqDto {

    @Schema(description = "小说ID")
    @NotNull(message = "小说ID不能为空")
    private Long id;

    @Schema(description = "作品方向;0-男频 1-女频")
    @NotNull(message = "作品方向不能为空")
    private Integer workDirection;

    @Schema(description = "类别ID")
    @NotNull(message = "类别ID不能为空")
    private Long categoryId;

    @Schema(description = "类别名")
    @NotBlank(message = "类别名不能为空")
    private String categoryName;

    @Schema(description = "小说封面地址")
    @NotBlank(message = "小说封面不能为空")
    private String picUrl;

    @Schema(description = "小说名")
    @NotBlank(message = "小说名不能为空")
    private String bookName;

    @Schema(description = "书籍描述")
    @NotBlank(message = "书籍描述不能为空")
    private String bookDesc;

    @Schema(description = "书籍状态;0-连载中 1-已完结")
    @NotNull(message = "书籍状态不能为空")
    private Integer bookStatus;

    @Schema(description = "是否收费;1-收费 0-免费")
    @NotNull(message = "是否收费不能为空")
    private Integer isVip;

    @Schema(description = "作家ID")
    private Long authorId;
}