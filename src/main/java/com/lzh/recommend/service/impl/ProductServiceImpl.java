package com.lzh.recommend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.constant.CommonConsts;
import com.lzh.recommend.constant.ProductConsts;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.enums.ScoreEnum;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.manager.FileManager;
import com.lzh.recommend.mapper.ProductMapper;
import com.lzh.recommend.mapper.RecordMapper;
import com.lzh.recommend.model.dto.PageProductDto;
import com.lzh.recommend.model.dto.ProductAddDto;
import com.lzh.recommend.model.dto.ProductUpdateDto;
import com.lzh.recommend.model.dto.SearchProductDto;
import com.lzh.recommend.model.entity.Cart;
import com.lzh.recommend.model.entity.Product;
import com.lzh.recommend.model.entity.Record;
import com.lzh.recommend.model.vo.ProductVo;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.CartService;
import com.lzh.recommend.service.ProductService;
import com.lzh.recommend.service.RecordService;
import com.lzh.recommend.service.UserService;
import com.lzh.recommend.utils.PageBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author by
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    @Resource
    private UserService userService;
    @Resource
    private RecordService recordService;
    @Resource
    private CartService cartService;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private RecordMapper recordMapper;
    @Resource
    private FileManager fileManager;

    @Value("${product.recommend.path.product-image-prefix}")
    private String productImagePrefix;

    private final Lock lock = new ReentrantLock();

    @Override
    public void addProduct(ProductAddDto productAddDto) {
        //获取请求参数
        String name = productAddDto.getName();
        String image = productAddDto.getImage();
        Integer price = productAddDto.getPrice();
        Integer stock = productAddDto.getStock();
        Integer status = productAddDto.getStatus();
        //判断参数是否符合要求
        if (StringUtils.isAnyBlank(name, image)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (price == null || price < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.PRICE_PARAM_ERROR);
        }
        if (stock == null || stock < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.STOCK_PARAM_ERROR);
        }
        if (status != null) {
            if (status < 0 || status > 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.STATUS_PARAM_ERROR);
            }
        }
        Product product = new Product();
        BeanUtil.copyProperties(productAddDto, product);
        //新增
        this.save(product);
    }

    @Override
    public void deleteById(Long id) {
        //判断商品是否存在
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //判断商品是否处于上架状态
        if (product.getStatus() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.DELETE_PRODUCT_ERROR);
        }
        //删除商品
        this.removeById(id);
    }

    @Override
    public void updateProduct(ProductUpdateDto productUpdateDto) {
        //获取请求参数
        Long id = productUpdateDto.getId();
        String name = productUpdateDto.getName();
        String image = productUpdateDto.getImage();
        Integer price = productUpdateDto.getPrice();
        Integer stock = productUpdateDto.getStock();
        //判断参数是否合法
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isAnyBlank(name, image)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (price == null || price < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.PRICE_PARAM_ERROR);
        }
        if (stock == null || stock < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.STOCK_PARAM_ERROR);
        }
        //创建更新请求体
        Product product = new Product();
        BeanUtil.copyProperties(productUpdateDto, product);
        //更新
        this.updateById(product);
    }

    @Override
    public PageBean<Product> listProductsByPage(PageProductDto pageProductDto) {
        //获取请求参数
        Integer current = pageProductDto.getCurrent();
        Integer pageSize = pageProductDto.getPageSize();
        Long id = pageProductDto.getId();
        String name = pageProductDto.getName();
        Integer status = pageProductDto.getStatus();
        //判断分页参数是否合法
        if (current == null || pageSize == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        if (current <= 0 || pageSize < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        //添加分页条件
        Page<Product> page = new Page<>(current, pageSize);
        //添加查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(id != null, Product::getId, id);
        wrapper.like(StrUtil.isNotBlank(name), Product::getName, name);
        wrapper.eq(status != null, Product::getStatus, status);
        //查询
        this.page(page, wrapper);
        //返回记录
        return PageBean.of(page.getTotal(), page.getRecords());
    }

    @Override
    public void alterStatus(Long id, Integer status) {
        //判断参数是否合法
        if (id <= 0 || status < 0 || status > 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断ID是否存在
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //判断当前状态值和数据库状态值是否一样
        if (product.getStatus().equals(status)) {
            return;
        }
        //更新状态
        product.setStatus(status);
        this.updateById(product);
    }

    @Override
    public PageBean<ProductVo> searchProducts(SearchProductDto searchProductDto, HttpServletRequest request) {
        //获取请求参数
        Integer current = searchProductDto.getCurrent();
        Integer pageSize = searchProductDto.getPageSize();
        String name = searchProductDto.getName();
        //校验参数
        if (current == null || pageSize == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        if (current <= 0 || pageSize < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        //添加分页条件
        Page<Product> page = new Page<>(current, pageSize);
        //添加查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Product::getName, name);
        wrapper.eq(Product::getStatus, 0);
        //查询
        this.page(page, wrapper);
        //搜索结果脱敏
        long total = page.getTotal();
        List<Product> records = page.getRecords();
        List<ProductVo> productVos = new ArrayList<>();
        //如果为空直接返回
        if (CollectionUtils.isEmpty(records)) {
            return PageBean.of(total, productVos);
        }
        //重新封装
        productVos = records.stream().map(product -> {
            ProductVo productVo = new ProductVo();
            BeanUtil.copyProperties(product, productVo);
            return productVo;
        }).collect(Collectors.toList());
        //如果用户输入的商品名称为空直接返回
        if (StrUtil.isBlank(name)) {
            return PageBean.of(total, productVos);
        }
        //存储用户搜索记录
        recordService.addRecords(productVos, request);
        //返回结果
        return PageBean.of(total, productVos);
    }

    @Override
    public List<ProductVo> recommend(Integer count, HttpServletRequest request) {
        //获取当前登录用户ID
        UserVo userVo = userService.getLoginUser(request);
        Long loginUserId = userVo.getId();
        //根据用户ID去查询记录表
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getUserId, loginUserId);
        long loginUserCount = recordService.count(wrapper);
        wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Record::getUserId, loginUserId);
        long otherUserCount = recordService.count(wrapper);
        //当记录表中无记录，或者登录用户不存在记录，或者其他用户不存在记录时，则随机推荐
        if (loginUserCount == 0 || otherUserCount == 0) {
            return this.randomProducts(count);
        }
        //当登录用户和其他用户都存在记录时，则利用用户协同过滤算法进行推荐
        return this.collaborativeFiltering(loginUserId, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purchaseProducts(Long cartId, HttpServletRequest request) {
        //判断购物车信息是否存在
        Cart cart = cartService.getById(cartId);
        if (cart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //获取登录用户ID
        UserVo userVo = userService.getLoginUser(request);
        Long userId = userVo.getId();
        //判断ID是否一致
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //加锁
        lock.lock();
        try {
            //判断商品库存是否为0
            Product product = this.getById(cart.getProductId());
            if (product.getStock() == 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PRODUCT_STOCK_ERROR);
            }
            //判断库存是否足够
            if (product.getStock() < cart.getNum()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PRODUCT_STOCK_ERROR);
            }
            //减少商品库存
            product.setStock(product.getStock() - cart.getNum());
            this.updateById(product);
            //删除购物车
            cartService.removeById(cartId);
        } finally {
            lock.unlock();
        }
        //根据用户ID和商品ID去查记录表
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getUserId, userId);
        wrapper.eq(Record::getProductId, cart.getProductId());
        Record record = recordService.getOne(wrapper);
        //给对应记录添加分数，值为3
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        record.setScore(record.getScore() + ScoreEnum.THREE.getScore());
        recordService.updateById(record);
    }

    @Override
    public List<ProductVo> randomProducts(Integer count) {
        if (count == null || count < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //从商品数据表中随机取出count条数据
        List<Product> productList = productMapper.randomProducts(count);
        //商品数据脱敏
        List<ProductVo> productVos = new ArrayList<>();
        //如果为空直接返回
        if (CollectionUtils.isEmpty(productList)) {
            return productVos;
        }
        productVos = productList.stream().map(product -> {
            ProductVo productVo = new ProductVo();
            BeanUtil.copyProperties(product, productVo);
            return productVo;
        }).collect(Collectors.toList());
        return productVos;
    }

    @Override
    public List<ProductVo> collaborativeFiltering(Long loginUserId, Integer count) {
        //用于保存用户相似度的Map
        Map<Long, Double> similarityMap = new HashMap<>((int) userService.count() - 1);
        //根据用户ID进行分组，每组的值是一个商品ID集合
        Map<Long, Set<Long>> userIdProductIdsMap = recordService.list().stream().collect(Collectors.groupingBy(Record::getUserId, Collectors.mapping(Record::getProductId, Collectors.toSet())));
        //获取登录用户商品ID集合
        Set<Long> loginUserProductIdSet = userIdProductIdsMap.get(loginUserId);
        //计算相似度，获取相似度集合
        this.getSimilarityMap(loginUserId, userIdProductIdsMap, loginUserProductIdSet, similarityMap);
        //对相似度集合根据相似度进行倒序排序
        similarityMap = similarityMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        //如果相似度最高的用户的相似度为0，则随机推荐
        for (Map.Entry<Long, Double> entry : similarityMap.entrySet()) {
            Double similarity = similarityMap.get(entry.getKey());
            if (similarity == 0) {
                return this.randomProducts(count);
            }
            break;
        }
        //取前三分之一的用户
        int num = 0;
        //用于保存相似用户列表的共同打分商品的集合
        Set<Long> commonProductSet = new HashSet<>();
        //用于保存前三分之一的用户的相似度之和
        double similaritySum = 0;
        for (Map.Entry<Long, Double> entry : similarityMap.entrySet()) {
            //获取用户ID
            Long userId = entry.getKey();
            //计算相似度之和
            similaritySum += entry.getValue();
            //获取用户ID对应的商品ID集合
            Set<Long> productIds = userIdProductIdsMap.get(userId);
            if (num == 0) {
                commonProductSet = new HashSet<>(productIds);
            } else {
                //求相似用户的商品交集
                commonProductSet.retainAll(productIds);
            }
            num++;
            if (num > similarityMap.size() / 3) {
                break;
            }
        }
        //求出商品交集中登录用户未打分的商品集合
        commonProductSet.removeIf(loginUserProductIdSet::contains);
        //如果集合为空，则随机推荐
        if (commonProductSet.isEmpty()) {
            return this.randomProducts(count);
        }
        //利用基于评分差值进行加权的方法对登录用户未打分的商品进行分数预测
        //计算登录用户的商品基础分，即商品平均分
        double sumScore = recordMapper.sumScore(loginUserId);
        double loginUserAvgScore = sumScore / loginUserProductIdSet.size();
        //计算未打分商品集合中每个商品的预测分数
        Map<Long, Double> finalScoreMap = new HashMap<>(commonProductSet.size());
        //计算最终预测分数
        this.getFinalScoreMap(commonProductSet, similarityMap, userIdProductIdsMap, loginUserAvgScore, finalScoreMap, similaritySum);
        //对最终地预测分数集合进行一次倒序排序
        finalScoreMap = finalScoreMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        //遍历集合，取前count个商品，如果数量小于count，则取全部
        List<ProductVo> productVoList;
        List<Product> productList = new ArrayList<>();
        int newCount = 0;
        for (Map.Entry<Long, Double> entry : finalScoreMap.entrySet()) {
            productList.add(this.getById(entry.getKey()));
            newCount++;
            if (newCount == count) {
                break;
            }
        }
        //数据脱敏，返回最终商品数据
        productVoList = productList.stream().map(product -> {
            ProductVo productVo = new ProductVo();
            BeanUtil.copyProperties(product, productVo);
            return productVo;
        }).collect(Collectors.toList());
        return productVoList;
    }

    @Override
    public void getSimilarityMap(Long loginUserId, Map<Long, Set<Long>> userIdProductIdsMap, Set<Long> loginUserProductIdSet, Map<Long, Double> similarityMap) {
        for (Long userId : userIdProductIdsMap.keySet()) {
            if (loginUserId.equals(userId)) {
                continue;
            }
            Set<Long> productIdSet = userIdProductIdsMap.get(userId);
            //创建两个集合用于保存交集和并集
            Set<Long> interSet = new HashSet<>(loginUserProductIdSet);
            Set<Long> outerSet = new HashSet<>(loginUserProductIdSet);
            //计算交集
            interSet.retainAll(productIdSet);
            //计算并集
            outerSet.addAll(productIdSet);
            //计算杰卡德相似系数
            double similarity = interSet.size() * 1.0 / outerSet.size();
            //添加到相似度集合中
            similarityMap.put(userId, similarity);
        }
    }

    @Override
    public void getFinalScoreMap(Set<Long> commonProductSet, Map<Long, Double> similarityMap, Map<Long, Set<Long>> userIdProductIdsMap, double loginUserAvgScore, Map<Long, Double> finalScoreMap, double similaritySum) {
        for (Long productId : commonProductSet) {
            int num = 0;
            double productScore = 0;
            for (Map.Entry<Long, Double> entry : similarityMap.entrySet()) {
                //获取用户打分商品的平均分
                double totalScore = recordMapper.sumScore(entry.getKey());
                double avgScore = totalScore / userIdProductIdsMap.get(entry.getKey()).size();
                //获取用户对商品的分数
                LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Record::getUserId, entry.getKey());
                wrapper.eq(Record::getProductId, productId);
                double score = recordService.getOne(wrapper).getScore();
                //计算相似度与分数差值之积，然后再求和
                productScore += entry.getValue() * (score - avgScore);
                num++;
                if (num > similarityMap.size() / 3) {
                    break;
                }
            }
            //计算最终地预测分数
            double predictScore = loginUserAvgScore + productScore / similaritySum;
            finalScoreMap.put(productId, predictScore);
        }
    }

    @Override
    public String uploadImage(MultipartFile multipartFile, HttpServletRequest request) {
        // 获取当前登录用户
        UserVo loginUser = userService.getLoginUser(request);
        // 构造地址前缀
        String uploadImagePrefix = String.format("%s/%s", productImagePrefix, loginUser.getId());
        // 上传图片
        return fileManager.uploadFile(multipartFile, uploadImagePrefix);
    }
}




