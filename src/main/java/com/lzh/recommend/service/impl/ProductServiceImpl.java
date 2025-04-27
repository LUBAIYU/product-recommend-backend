package com.lzh.recommend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.constant.CommonConsts;
import com.lzh.recommend.constant.ProductConsts;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.enums.OpTypeEnum;
import com.lzh.recommend.enums.ProductStatusEnum;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.mapper.ProductMapper;
import com.lzh.recommend.mapper.RecordMapper;
import com.lzh.recommend.model.dto.*;
import com.lzh.recommend.model.entity.Product;
import com.lzh.recommend.model.entity.Record;
import com.lzh.recommend.model.entity.SaleRecord;
import com.lzh.recommend.model.vo.ProductVo;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.ProductService;
import com.lzh.recommend.service.RecordService;
import com.lzh.recommend.service.SaleRecordService;
import com.lzh.recommend.service.UserService;
import com.lzh.recommend.upload.FilePictureUpload;
import com.lzh.recommend.upload.UrlPictureUpload;
import com.lzh.recommend.utils.PageBean;
import com.lzh.recommend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author by
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    private final UserService userService;
    private final RecordService recordService;
    private final ProductMapper productMapper;
    private final RecordMapper recordMapper;
    private final FilePictureUpload filePictureUpload;
    private final UrlPictureUpload urlPictureUpload;
    private final SaleRecordService saleRecordService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ExecutorService executorService;

    @Value("${product.recommend.path.product-image-prefix}")
    private String productImagePrefix;

    /**
     * 协同过滤算法的N值，即取前N个最相似用户进行推荐
     */
    private final int N = 10;

    private final Lock lock = new ReentrantLock();

    @Override
    public long addProduct(ProductAddDto productAddDto) {
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
            ProductStatusEnum statusEnum = ProductStatusEnum.getEnumByCode(status);
            if (statusEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.STATUS_PARAM_ERROR);
            }
        }
        Product product = new Product();
        BeanUtil.copyProperties(productAddDto, product);
        if (status == null) {
            product.setStatus(ProductStatusEnum.ON_SALE.getCode());
        }
        //新增
        this.save(product);
        return product.getId();
    }

    @Override
    public void deleteById(Long id) {
        //判断商品是否存在
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //判断商品是否处于上架状态
        if (product.getStatus().equals(ProductStatusEnum.ON_SALE.getCode())) {
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
        wrapper.orderByDesc(Product::getUpdateTime);
        //查询
        this.page(page, wrapper);
        //返回记录
        return PageBean.of(page.getTotal(), page.getRecords());
    }

    @Override
    public void alterStatus(Long id, Integer status) {
        //判断参数是否合法
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductStatusEnum statusEnum = ProductStatusEnum.getEnumByCode(status);
        if (statusEnum == null) {
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
        // 查询条件转JSON，然后再MD5加密
        String questionStr = JSONUtil.toJsonStr(searchProductDto);
        String hashKey = DigestUtils.md5DigestAsHex(questionStr.getBytes());
        // 构建缓存Key
        String cacheKey = String.format("product-recommend:searchProducts:%s", hashKey);

        // 查 Redis 缓存，如果存在则返回
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        String cacheValues = opsForValue.get(cacheKey);
        if (StrUtil.isNotBlank(cacheValues)) {
            // 将字符串转为对象
            PageBean<ProductVo> cacheResult = JSONUtil.toBean(
                    cacheValues,
                    new TypeReference<PageBean<ProductVo>>() {
                    },
                    true
            );
            // 用户自行输入搜索才保存记录
            if (StrUtil.isNotBlank(searchProductDto.getName())) {
                // 存储用户搜索记录
                List<ProductVo> productVos = cacheResult.getRecords();
                // 异步改记录
                executorService.submit(() -> recordService.addRecords(productVos, request));
            }
            return cacheResult;
        }

        // 获取请求参数
        Integer current = searchProductDto.getCurrent();
        Integer pageSize = searchProductDto.getPageSize();
        String name = searchProductDto.getName();
        // 校验参数
        if (current == null || pageSize == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        if (current <= 0 || pageSize < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        // 添加分页条件
        Page<Product> page = new Page<>(current, pageSize);
        // 添加查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(name), Product::getName, name);
        wrapper.eq(Product::getStatus, ProductStatusEnum.ON_SALE.getCode());
        // 查询
        this.page(page, wrapper);

        long total = page.getTotal();
        List<Product> records = page.getRecords();
        // 如果为空直接返回
        if (CollectionUtils.isEmpty(records)) {
            // 缓存空数据
            PageBean<ProductVo> pageBean = PageBean.of(total, Collections.emptyList());
            setCache(opsForValue, pageBean, cacheKey);
            return pageBean;
        }

        // 搜索结果脱敏
        List<ProductVo> productVos = records.stream()
                .map(product -> BeanUtil.copyProperties(product, ProductVo.class))
                .collect(Collectors.toList());

        // 如果用户输入的商品名称为空直接返回
        PageBean<ProductVo> searchResult = PageBean.of(total, productVos);
        setCache(opsForValue, searchResult, cacheKey);
        if (StrUtil.isBlank(name)) {
            return searchResult;
        }
        // 异步存储用户搜索记录
        executorService.submit(() -> recordService.addRecords(productVos, request));
        // 返回结果
        return searchResult;
    }

    @Override
    public List<ProductVo> recommend(Integer count, HttpServletRequest request) {
        // 获取记录
        long total = recordService.count();
        if (total <= 0) {
            // 如果无记录，则随机推荐
            return randomProducts(count);
        }

        // 获取当前登录用户ID
        UserVo userVo = userService.getLoginUser(request);
        Long loginUserId = userVo.getId();

        // 根据用户ID去查询记录表
        // 获取用户记录
        Long loginUserCount = recordService.lambdaQuery()
                .eq(Record::getUserId, loginUserId)
                .count();

        // 根据用户ID进行分组，每组的值是一个商品ID集合（覆盖索引优化）
        List<Record> recordList = recordService.lambdaQuery()
                .select(Record::getUserId, Record::getProductId)
                .list();
        Map<Long, Set<Long>> userIdProductIdsMap = recordList.stream()
                .collect(Collectors.groupingBy(Record::getUserId,
                        Collectors.mapping(Record::getProductId, Collectors.toSet())));

        //如果登录用户不存在记录或者只有当前用户的记录，则进行冷启动处理
        if (loginUserCount == 0 || loginUserCount == total) {
            return handleColdStart(count, userIdProductIdsMap, request);
        }

        //当登录用户和其他用户都存在记录时，则利用用户协同过滤算法进行推荐
        return collaborativeFiltering(loginUserId, count, userIdProductIdsMap, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void purchaseProduct(Long productId, HttpServletRequest request) {
        // 判断商品是否存在
        Product product = this.getById(productId);
        ThrowUtils.throwIf(product == null, ErrorCode.NOT_FOUND_ERROR);

        // 获取当前登录用户ID
        UserVo loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();

        // 加锁，防止库存超卖
        lock.lock();
        try {
            // 判断商品库存是否足够
            Integer stock = product.getStock();
            if (stock == 0 || stock < 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PRODUCT_STOCK_ERROR);
            }
            // 更新库存
            product.setStock(stock - 1);
            this.updateById(product);
            // 更新商品销售记录
            saleRecordService.saveOrUpdate(productId, 1);
        } finally {
            // 解锁
            lock.unlock();
        }

        // 添加或更新记录
        recordService.saveOrUpdate(loginUserId, productId, OpTypeEnum.PRODUCT_PURCHASE);
    }

    @Override
    public List<ProductVo> recommendHotProducts(Integer count) {
        // 取出所有商品的销售记录
        List<SaleRecord> saleRecordList = saleRecordService.list();
        if (CollUtil.isEmpty(saleRecordList)) {
            // 随机取出
            return randomProducts(count);
        }

        // 对所有记录根据销售量进行降序排序
        List<Long> productIdList = saleRecordList.stream()
                .sorted(Comparator.comparing(SaleRecord::getSaleNum).reversed())
                .map(SaleRecord::getProductId)
                .limit(count)
                .collect(Collectors.toList());

        // 取出商品列表，然后对列表按照商品ID列表的顺序进行排序
        List<Product> productList = this.lambdaQuery()
                .in(Product::getId, productIdList)
                .list();
        if (CollUtil.isEmpty(productList)) {
            // 随机取出
            return randomProducts(count);
        }
        // 传入的ID列表是3,1,2，出来的结果是3,1,2对应的商品，保持统一顺序
        List<Product> sortedProductList = productList.stream()
                .sorted(Comparator.comparingLong(
                        product -> productIdList.indexOf(product.getId()))
                )
                .collect(Collectors.toList());

        // 脱敏返回
        return sortedProductList.stream()
                .map(product -> BeanUtil.copyProperties(product, ProductVo.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVo> randomProducts(Integer count) {
        if (count == null || count < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //从商品数据表中随机取出count条数据
        List<Product> productList = productMapper.randomProducts(count);
        //如果为空直接返回
        if (CollectionUtils.isEmpty(productList)) {
            return Collections.emptyList();
        }
        // 返回脱敏数据
        return productList.stream()
                .map(product -> BeanUtil.copyProperties(product, ProductVo.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductVo> collaborativeFiltering(Long loginUserId,
                                                  Integer count,
                                                  Map<Long, Set<Long>> userIdProductIdsMap,
                                                  HttpServletRequest request) {
        // 获取登录用户商品ID集合
        Set<Long> loginUserProductIdSet = userIdProductIdsMap.get(loginUserId);
        // 获取排序后的融合相似度集合
        Map<Long, Double> similarityMap = getSortedFusionSimilarityMap(loginUserId,
                userIdProductIdsMap,
                loginUserProductIdSet,
                request);

        // 如果相似度最高的用户的相似度为0，则推荐热门商品
        if (!similarityMap.isEmpty() && similarityMap.values().iterator().next() == 0) {
            return recommendHotProducts(count);
        }

        // 取前n个最相似用户
        int num = 0;
        // 用于保存前n个用户的相似度之和
        double similaritySum = 0;
        // 统计商品被前 N 个相似用户覆盖的次数
        Map<Long, Integer> coverCountMap = new HashMap<>(N << 1);
        for (Map.Entry<Long, Double> entry : similarityMap.entrySet()) {
            if (num >= N) {
                break;
            }
            Long userId = entry.getKey();
            Set<Long> productIds = userIdProductIdsMap.get(userId);
            // 如果没有商品，则降级添加相似度
            if (CollUtil.isEmpty(productIds)) {
                similaritySum += entry.getValue() * 0.5;
                continue;
            }
            // 保存商品的次数
            productIds.forEach(productId -> {
                coverCountMap.put(productId, coverCountMap.getOrDefault(productId, 0) + 1);
            });
            // 计算相似度之和
            similaritySum += entry.getValue();
            num++;
        }

        // 筛选被至少0.2*N个用户覆盖的商品
        Set<Long> commonProductSet = coverCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() >= 0.2 * N)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // 如果集合为空，则推荐热门商品
        if (CollUtil.isEmpty(commonProductSet)) {
            return recommendHotProducts(count);
        }
        // 移除登录用户已打分的商品
        commonProductSet.removeAll(loginUserProductIdSet);
        // 再次判空
        if (CollUtil.isEmpty(commonProductSet)) {
            return recommendHotProducts(count);
        }

        // 利用基于评分差值进行加权的方法对登录用户未打分的商品进行分数预测
        // 计算最终预测分数
        Map<Long, Double> finalScoreMap = getFinalScoreMap(commonProductSet, similarityMap,
                loginUserId, similaritySum);

        // 取前count个商品，如果数量小于count，则取全部
        List<Product> productList = this.lambdaQuery()
                .in(Product::getId, finalScoreMap.keySet())
                .list();
        if (productList.size() > count) {
            productList = productList.subList(0, count);
        }

        // 数据脱敏，返回最终商品数据
        return productList.stream()
                .map(product -> BeanUtil.copyProperties(product, ProductVo.class))
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, Double> getSimilarityMapByBehaviorRecord(Long loginUserId,
                                                              Map<Long, Set<Long>> userIdProductIdsMap,
                                                              Set<Long> loginUserProductIdSet) {
        // 保存行为相似度
        Map<Long, Double> recordSimilarityMap = new HashMap<>((int) userService.count() - 1 << 1);
        // 计算其他用户与登录用户的相似度
        for (Map.Entry<Long, Set<Long>> entry : userIdProductIdsMap.entrySet()) {
            Long userId = entry.getKey();
            if (loginUserId.equals(userId)) {
                continue;
            }
            // 拿到其他用户的商品ID集合
            BigDecimal bd = getRecordSimilarity(loginUserProductIdSet, entry);
            //添加到相似度集合中
            recordSimilarityMap.put(userId, bd.doubleValue());
        }
        return recordSimilarityMap;
    }


    @Override
    public Map<Long, Double> getFinalScoreMap(Set<Long> commonProductSet, Map<Long, Double> similarityMap,
                                              Long loginUserId,
                                              double similaritySum) {
        // 计算登录用户的商品基础分，即商品平均分
        double loginUserAvgScore = recordMapper.avgScore(loginUserId);
        // 计算未打分商品集合中每个商品的预测分数
        Map<Long, Double> finalScoreMap = new HashMap<>(commonProductSet.size() << 1);

        // 批量获取相似用户的商品平均分
        Map<Long, Double> userAvgScoreMap = new HashMap<>(similarityMap.keySet().size() << 1);
        for (Long userId : similarityMap.keySet()) {
            userAvgScoreMap.put(userId, recordMapper.avgScore(userId));
        }

        // 批量获取相似用户对待预测商品集合的分数
        LambdaQueryWrapper<Record> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Record::getUserId, similarityMap.keySet());
        queryWrapper.in(Record::getProductId, commonProductSet);
        List<Record> recordList = recordService.list(queryWrapper);

        // 根据用户ID分组，进行集合转换：List -> Map（userId -> Map<productId,score>）
        Map<Long, Map<Long, Integer>> userIdProductIdScoreMap = recordList.stream()
                .collect(Collectors.groupingBy(
                                Record::getUserId,
                                Collectors.toMap(Record::getProductId, Record::getScore)
                        )
                );

        // 遍历每个待预测的商品
        for (Long productId : commonProductSet) {
            int num = 0;
            double productScore = 0.0;
            // 遍历前 N 个相似用户
            for (Map.Entry<Long, Double> entry : similarityMap.entrySet()) {
                Long userId = entry.getKey();
                Double similarity = entry.getValue();
                // 判空操作
                if (!userIdProductIdScoreMap.containsKey(userId)) {
                    continue;
                }
                if (!userIdProductIdScoreMap.get(userId).containsKey(productId)) {
                    continue;
                }
                // 获取相似用户打分商品的平均分
                double avgScore = userAvgScoreMap.get(userId);
                // 获取相似用户对待预测商品的分数
                double score = userIdProductIdScoreMap.get(userId).get(productId);
                // 计算相似度与分数差值之积，然后再求和
                productScore += similarity * (score - avgScore);
                num++;
                if (num >= N) {
                    break;
                }
            }
            //计算最终地预测分数
            double predictScore = loginUserAvgScore + productScore / similaritySum;
            // 使用 BigDecimal 保留两位小数（四舍五入）
            BigDecimal bd = new BigDecimal(predictScore)
                    .setScale(2, RoundingMode.HALF_UP);
            finalScoreMap.put(productId, bd.doubleValue());
        }

        // 对最终地预测分数集合进行一次倒序排序
        return finalScoreMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new)
                );
    }

    @Override
    public String uploadImage(MultipartFile multipartFile, HttpServletRequest request) {
        // 获取当前登录用户
        UserVo loginUser = userService.getLoginUser(request);
        // 构造地址前缀
        String uploadImagePrefix = String.format("%s/%s", productImagePrefix, loginUser.getId());
        // 上传图片
        return filePictureUpload.uploadPicture(multipartFile, uploadImagePrefix);
    }

    @Override
    public ProductVo getProductVoById(Long id, HttpServletRequest request) {
        // 获取数据
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户ID
        UserVo loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        // 添加或更新记录
        recordService.saveOrUpdate(loginUserId, id, OpTypeEnum.PRODUCT_DETAIL);
        // 返回数据
        return BeanUtil.copyProperties(product, ProductVo.class);
    }

    @Override
    public void addProductByBatch(ProductFetchDto productFetchDto, HttpServletRequest request) {
        // 获取参数
        String searchText = productFetchDto.getSearchText();
        Integer count = productFetchDto.getCount();
        String namePrefix = productFetchDto.getNamePrefix();
        Integer priceUp = productFetchDto.getPriceUp();
        Integer priceDown = productFetchDto.getPriceDown();

        // 校验参数
        if (StrUtil.isBlank(searchText)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        if (count == null || count <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "最多批量新增30个商品");
        // 校验价格
        checkParams(priceUp, priceDown);

        // 获取登录用户ID
        Long loginUserId = userService.getLoginUser(request).getId();
        // 图片上传路径
        String uploadImagePrefix = String.format("%s/%s", productImagePrefix, loginUserId);

        // 抓取商品图片
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取页面失败");
        }

        // 商品列表
        List<Product> productList = new ArrayList<>();
        // 解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjectUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element element : imgElementList) {
            String fileUrl = element.attr("src");
            // 如果链接为空，直接跳过
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过：{}", fileUrl);
                continue;
            }
            // 处理图片地址
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            String imageUrl = urlPictureUpload.uploadPicture(fileUrl, uploadImagePrefix);
            // 创建商品信息
            Product product = new Product();
            product.setImage(imageUrl);
            product.setName(namePrefix + (++uploadCount));
            product.setStatus(ProductStatusEnum.ON_SALE.getCode());
            product.setStock(RandomUtil.randomInt(0, 100));
            if (priceUp != null) {
                product.setPrice(RandomUtil.randomInt(priceDown, priceUp));
            } else {
                product.setPrice(RandomUtil.randomInt(10, 3000));
            }
            productList.add(product);
            if (uploadCount >= count) {
                break;
            }
        }

        // 批量插入
        ProductService proxyService = (ProductService) AopContext.currentProxy();
        boolean saved = proxyService.saveBatch(productList);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据库操作失败");
        }
    }

    /**
     * 缓存商品搜索结果
     *
     * @param operations redis String 操作类
     * @param pageBean   商品搜索结果
     * @param cacheKey   缓存键名
     */
    private void setCache(ValueOperations<String, String> operations, PageBean<?> pageBean, String cacheKey) {
        // 将对象转为 JSON 字符串
        String jsonStr = JSONUtil.toJsonStr(pageBean);
        // 设置缓存，并设置过期时间随机
        int cacheTime = 300 + RandomUtil.randomInt(0, 300);
        try {
            operations.set(cacheKey, jsonStr, cacheTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("缓存设置失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "缓存设置失败");
        }
    }

    /**
     * 校验参数
     *
     * @param priceUp   价格上限
     * @param priceDown 价格下限
     */
    private void checkParams(Integer priceUp, Integer priceDown) {
        // 两个参数都为null（允许通过）
        if (priceUp == null && priceDown == null) {
            return;
        }
        // 其中一个为null（不合法）
        if (priceUp == null || priceDown == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查价格是否为负数
        if (priceUp <= 0 || priceDown <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 检查价格下限是否大于上限
        if (priceDown > priceUp) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    /**
     * 获取融合相似度集合
     *
     * @param recordSimilarityMap       行为相似度集合
     * @param userPropertySimilarityMap 用户属性相似度集合
     * @param userIdProductIdsMap       用户ID与商品ID集合映射
     * @return 融合相似度集合
     */
    private Map<Long, Double> getFusionSimilarityMap(Map<Long, Double> recordSimilarityMap,
                                                     Map<Long, Double> userPropertySimilarityMap,
                                                     Map<Long, Set<Long>> userIdProductIdsMap) {
        // 获取所有用户ID
        Set<Long> allUserIdSet = Stream.concat(
                        recordSimilarityMap.keySet().stream(),
                        userPropertySimilarityMap.keySet().stream()
                )
                .collect(Collectors.toSet());

        // 保存每个用户的动态Alpha值
        Map<Long, Double> dynamicAlphaMap = new HashMap<>(allUserIdSet.size() << 1);
        // 计算动态Alpha值
        allUserIdSet.forEach(userId -> {
            // 获取行为记录数
            int behaviorCount = userIdProductIdsMap.getOrDefault(userId, Collections.emptySet())
                    .size();
            double alpha = Math.min(
                    ProductConsts.MAX_POWER,
                    ProductConsts.BASIC + ProductConsts.GROWTH_RATE * behaviorCount
            );
            dynamicAlphaMap.put(userId, alpha);
        });

        // 计算融合相似度
        return allUserIdSet.stream().collect(Collectors.toMap(
                userId -> userId,
                userId -> {
                    // 获取行为相似度
                    double recordSimilarity = recordSimilarityMap.getOrDefault(userId, 0.0);
                    // 获取用户属性相似度
                    double userPropertySimilarity = userPropertySimilarityMap.getOrDefault(userId, 0.0);
                    // 获取动态Alpha值
                    double alpha = dynamicAlphaMap.get(userId);
                    // 计算融合相似度
                    double mixSimilarity = alpha * recordSimilarity + (1 - alpha) * userPropertySimilarity;

                    // 使用 BigDecimal 保留两位小数（四舍五入）
                    return new BigDecimal(mixSimilarity)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                }
        ));
    }

    /**
     * 使用余弦相似度计算行为记录相似度
     *
     * @param loginUserProductIdSet 登录用户商品ID集合
     * @param entry                 Entry对象
     * @return 相似度
     */
    private BigDecimal getRecordSimilarity(Set<Long> loginUserProductIdSet, Map.Entry<Long, Set<Long>> entry) {
        Set<Long> productIdSet = entry.getValue();
        int loginUserProductSize = loginUserProductIdSet.size();
        int otherUserProductSize = productIdSet.size();

        // 计算交集大小
        int intersectionSize = CollUtil.intersection(loginUserProductIdSet,
                productIdSet).size();

        // 余弦相似度计算
        if (loginUserProductSize == 0 || otherUserProductSize == 0) {
            return BigDecimal.ZERO;
        }
        double denominator = Math.sqrt(loginUserProductSize) * Math.sqrt(otherUserProductSize);
        // 计算相似度
        double similarity = denominator == 0 ? 0.0 : (double) intersectionSize / denominator;

        // 使用 BigDecimal 保留两位小数（四舍五入）
        return new BigDecimal(similarity)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 处理冷启动
     * 混合推荐方式：50% 热门商品推荐，30% 用户属性推荐，20% 随机商品推荐
     *
     * @param count               推荐条数
     * @param request             请求对象
     * @param userIdProductIdsMap 用户ID和商品ID集合映射
     * @return 推荐商品列表
     */
    private List<ProductVo> handleColdStart(Integer count, Map<Long, Set<Long>> userIdProductIdsMap, HttpServletRequest request) {
        // 获取用户属性相似度集合
        Map<Long, Double> propertySimilarityMap = userService.getSimilarityMapByUserProperty(request);
        // 如果集合为空，则进行热门商品推荐
        if (CollUtil.isEmpty(propertySimilarityMap)) {
            return recommendHotProducts(count);
        }

        // 对相似度集合进行排序并取前 N 个用户
        List<Long> similarUserIdList = propertySimilarityMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(N)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        // 获取相似用户购买的商品
        Set<Long> productIdSet = similarUserIdList.stream()
                .flatMap(userId -> {
                    if (userIdProductIdsMap.containsKey(userId)) {
                        return userIdProductIdsMap.get(userId).stream();
                    }
                    return Stream.empty();
                })
                .collect(Collectors.toSet());

        // 50% 热门
        List<ProductVo> hotProductList = recommendHotProducts((int) (0.5 * count));

        List<ProductVo> similarUserProductList;
        List<ProductVo> randomProductList;
        Set<ProductVo> productVoSet;

        // 50% 热门 + 30% 相似用户偏好 + 20% 随机
        if (CollUtil.isNotEmpty(productIdSet)) {
            // 30% 相似用户偏好
            similarUserProductList = this.lambdaQuery()
                    .in(Product::getId, productIdSet)
                    .list()
                    .stream()
                    .map(product -> BeanUtil.copyProperties(product, ProductVo.class))
                    .limit((long) (0.3 * count))
                    .collect(Collectors.toList());

            // 20% 随机
            randomProductList = randomProducts((int) (0.2 * count));

            // 合并推荐结果
            productVoSet = Stream.concat(Stream.concat(hotProductList.stream(), similarUserProductList.stream()),
                            randomProductList.stream())
                    .collect(Collectors.toSet());
        } else {
            // 50% 热门 + 50% 随机
            randomProductList = randomProducts((int) (0.5 * count));

            // 合并推荐结果
            productVoSet = Stream.concat(hotProductList.stream(),
                            randomProductList.stream())
                    .collect(Collectors.toSet());
        }

        List<ProductVo> productVoList;
        // 避免出现重复商品
        while (true) {
            if (productVoSet.size() >= count) {
                productVoList = new ArrayList<>(productVoSet);
                break;
            }
            // 再随机取出一些商品进行合并，直到达到 count 条数据
            randomProductList = randomProducts(count - productVoSet.size());
            productVoSet.addAll(randomProductList);
        }
        return productVoList;
    }

    /**
     * 获取排序后的融合相似度集合
     *
     * @param loginUserId           登录用户ID
     * @param userIdProductIdsMap   用户ID和商品ID集合映射
     * @param loginUserProductIdSet 登录用户商品ID集合
     * @param request               请求对象
     * @return 排序后的融合相似度集合
     */
    private Map<Long, Double> getSortedFusionSimilarityMap(Long loginUserId,
                                                           Map<Long, Set<Long>> userIdProductIdsMap,
                                                           Set<Long> loginUserProductIdSet,
                                                           HttpServletRequest request) {
        // 获取行为相似度集合
        Map<Long, Double> recordSimilarityMap = getSimilarityMapByBehaviorRecord(
                loginUserId, userIdProductIdsMap, loginUserProductIdSet
        );
        // 获取用户属性相似度集合
        Map<Long, Double> userPropertySimilarityMap = userService.getSimilarityMapByUserProperty(request);
        // 获取融合相似度集合
        Map<Long, Double> similarityMap = recordSimilarityMap;
        if (CollUtil.isNotEmpty(userPropertySimilarityMap)) {
            similarityMap = getFusionSimilarityMap(
                    recordSimilarityMap, userPropertySimilarityMap, userIdProductIdsMap
            );
        }

        // 对相似度集合根据相似度进行倒序排序
        return similarityMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new)
                );
    }
}




