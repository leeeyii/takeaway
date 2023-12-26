package com.fubukiss.rikky.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fubukiss.rikky.common.R;
import com.fubukiss.rikky.dto.DishDto;
import com.fubukiss.rikky.entity.Dish;
import com.fubukiss.rikky.service.CategoryService;
import com.fubukiss.rikky.service.DishFlavorService;
import com.fubukiss.rikky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>Project: rikky-takeaway - DishController 菜品相关的控制类
 * <p>Powered by river On 2023/01/12 2:50 PM
 *
 * @author Riverify
 * @version 1.0
 * @since JDK8
 */
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    /**
     * 菜品服务
     */
    @Autowired
    private DishService dishService;
    /**
     * 菜品口味服务
     */
    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 分类服务
     */
    @Autowired
    private CategoryService categoryService;


    /**
     * <h2>新增菜品<h2/>
     *
     * @param dishDto dto: data transfer object，主要用于多表查询时，将查询结果封装成一个对象，方便前端使用，如在本项目的菜品新增中，
     *                前端需要传入菜品的基本信息，以及菜品的口味信息，而菜品和菜品口味是两张表，在后端拥有两个实体类，
     *                所以需要将这两个实体类封装成一个对象。@RequestBody注解用于将前端传入的json数据转换成对象
     * @return 通用返回对象
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("新增菜品，dishDto: {}", dishDto.toString());       // Slf4j的日志输出
        // 保存菜品
        dishService.saveWithFlavors(dishDto);   // saveWithFlavors方法为非mybatis-plus提供的方法，用于同时保存菜品和菜品口味

        return R.success("新增菜品成功");
    }


    /**
     * <h2>分页查询菜品<h2/>
     * <p>其中菜品的图片由{@link CommonController}提供下载到页面的功能。
     *
     * @param page     前端传入的分页参数，一次性传入当前页码
     * @param pageSize 前端传入的分页参数，一次性传入每页显示的条数
     * @param name     查询条件，如果name为空，则查询所有菜品
     * @return Page对象，mybatis-plus提供的分页对象，包含了分页的所有信息
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("分页查询菜品，page: {}, pageSize: {}, name: {}", page, pageSize, name);

        // 查询dishDtoPage对象
        Page<DishDto> dishDtoPage = dishService.page(page, pageSize, name);

        // 返回dishDtoPage对象
        return R.success(dishDtoPage);
    }


    /**
     * <h2>根据id获取某菜品的信息和口味信息<h2/>
     *
     * @param id 菜品id
     * @return 菜品信息（包含DishFlavor）
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        // 查询dishDto对象
        DishDto dishDto = dishService.getByIdWithFlavors(id);

        return R.success(dishDto);
    }


    /**
     * <h2>修改保存菜品<h2/>
     *
     * @param dishDto dto: data transfer object，主要用于多表查询时，将查询结果封装成一个对象，方便前端使用，如在本项目的菜品新增中，
     *                前端需要传入菜品的基本信息，以及菜品的口味信息，而菜品和菜品口味是两张表，在后端拥有两个实体类，
     *                所以需要将这两个实体类封装成一个对象。@RequestBody注解用于将前端传入的json数据转换成对象
     * @return 通用返回
     */
    @PutMapping
    public R<String> put(@RequestBody DishDto dishDto) {
        log.info("修改菜品，dishDto: {}", dishDto.toString());       // Slf4j的日志输出

        // 保存菜品
        dishService.updateWithFlavors(dishDto);

        return R.success("修改成功");
    }


    /**
     * <h2>修改菜品状态，如果是停售，则修改为在售，如果是在售，则修改为停售<h2/>
     * <p>能够批量修改状态
     *
     * @param ids    前端传入的菜品id，可能是一个，也可能是多个，多个数据是以逗号分隔的
     * @param status 菜品需要修改成的状态，该参数是在路径中传入的，所以需要使用@PathVariable注解
     * @return 通用返回类，返回结果消息
     */
    @PostMapping("/status/{status}")
    public R<String> changeStatus(String ids, @PathVariable Integer status) {
        log.info("修改菜品状态，id: {}, status: {}", ids, status);

        // 修改菜品状态
        dishService.updateDishStatus(ids, status);

        return R.success("修改成功");
    }


    /**
     * <h2>删除菜品（逻辑删除)<h2/>
     * <p>能够批量删除操作
     *
     * @param ids 前端传入的菜品id，可能是一个，也可能是多个，多个数据是以逗号分隔的
     * @return 通用返回类，返回结果消息
     */
    @DeleteMapping
    public R<String> delete(String ids) {
        log.info("删除菜品，id: {}", ids);

        // 删除菜品
        dishService.deleteByIds(ids);

        return R.success("删除成功");
    }   // fixme:没有做到同步删除菜品和菜品口味的关联表

    /**
     * <h2>根据条件查询相应的菜品数据<h2/>
     * <p>如：前端传入的是CategoryId(Dish中的一个参数，为分类id)，则查询该分类下的所有菜品。主要应对套餐添加时的添加菜品功能。
     * 也用于前台菜品列表的查询功能（需要包含菜品的口味信息）
     *
     * @param dish 菜品实体
     * @return 菜品列表，包含菜品的口味信息（用于前台展示）
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        log.info("查询菜品列表，dish: {}", dish);

        // 查询菜品列表
        List<DishDto> dishDtoList = dishService.listWithFlavors(dish);

        return R.success(dishDtoList);
    }

}
