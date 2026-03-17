package com.system.user.controller;

import com.system.common.Result;
import com.system.user.entity.CartItem;
import com.system.admin.entity.Brand;
import com.system.admin.entity.Goods;
import com.system.admin.mapper.BrandMapper;
import com.system.admin.mapper.GoodsMapper;
import com.system.user.entity.CartItem;
import com.system.user.entity.CartItemView;
import com.system.user.entity.User;
import com.system.user.mapper.CartMapper;
import com.system.user.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CartMapper cartMapper;

    private User currentUser(HttpServletRequest request) {
        // 由 AuthInterceptor 基于 token 设置
        return (User) request.getAttribute("currentUser");
    }

    @GetMapping
    public Result<List<CartItemView>> list(HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        List<CartItem> items = cartService.listByUserId(user.getId());
        // 组装前端展示所需的商品信息（名称、品牌、价格、当前库存）
        List<CartItemView> views = new java.util.ArrayList<>();
        if (items != null) {
            for (CartItem item : items) {
                Goods goods = goodsMapper.selectById(item.getGoodsId());
                if (goods == null) {
                    // 商品被删除时，跳过该购物车项
                    continue;
                }
                CartItemView v = new CartItemView();
                v.setId(item.getId());
                v.setGoodsId(item.getGoodsId());
                v.setQuantity(item.getQuantity());
                v.setGoodsName(goods.getName());
                v.setPrice(goods.getPrice());
                v.setStock(goods.getStock());
                // 品牌名称
                String brandName = null;
                if (goods.getBrandId() != null) {
                    Brand brand = brandMapper.selectById(goods.getBrandId());
                    if (brand != null) {
                        brandName = brand.getName();
                    }
                }
                v.setBrandName(brandName);
                views.add(v);
            }
        }
        return Result.success(views);
    }

    @PostMapping
    public Result<Void> add(@RequestBody CartItem item, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        if (item.getGoodsId() == null) {
            return Result.fail("商品ID不能为空");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            return Result.fail("商品数量必须大于 0");
        }
        // 校验当前库存，避免一次性加入超出库存的数量
        Goods goods = goodsMapper.selectById(item.getGoodsId());
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        if (goods.getOwnerId() != null && goods.getOwnerId().equals(user.getId())) {
            return Result.fail("不能购买自己发布的商品");
        }
        Integer stock = goods.getStock();
        if (stock == null || stock < item.getQuantity()) {
            return Result.fail("商品库存不足，当前库存为：" + (stock == null ? 0 : stock));
        }
        // 叠加已有购物车数量后再做一次校验，避免总数量超过库存
        CartItem exists = cartMapper.selectByUserAndGoods(user.getId(), item.getGoodsId());
        int existsQty = exists != null && exists.getQuantity() != null ? exists.getQuantity() : 0;
        int need = item.getQuantity();
        if (stock != null && existsQty + need > stock) {
            return Result.fail("加入购物车后总数量超过库存，当前库存：" + stock + "，购物车中已存在数量：" + existsQty);
        }
        item.setUserId(user.getId());
        cartService.addItem(item);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CartItem item, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            return Result.fail("商品数量必须大于 0");
        }
        // 更新数量时也要校验库存，防止超过当前库存
        if (item.getGoodsId() == null) {
            return Result.fail("商品信息异常");
        }
        Goods goods = goodsMapper.selectById(item.getGoodsId());
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        Integer stock = goods.getStock();
        if (stock != null && item.getQuantity() > stock) {
            return Result.fail("购物车数量不能超过库存（当前库存：" + stock + "）");
        }
        item.setId(id);
        item.setUserId(user.getId());
        cartService.updateItem(item);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.removeItem(id);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear(HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        cartService.clearByUserId(user.getId());
        return Result.success();
    }
}

