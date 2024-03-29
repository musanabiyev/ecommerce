package com.base.ecommerce.service;

import com.base.ecommerce.dto.ErrorCode;
import com.base.ecommerce.dto.FavoriteDto;
import com.base.ecommerce.dto.converter.FavoriteDtoConverter;
import com.base.ecommerce.dto.converter.FavoriteSaveRequestConverter;
import com.base.ecommerce.dto.request.FavoriteSaveRequest;
import com.base.ecommerce.exception.customException.FavoriteAlreadyException;
import com.base.ecommerce.exception.customException.ProductNotFoundException;
import com.base.ecommerce.model.Favorite;
import com.base.ecommerce.repository.FavoriteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {


    private final FavoriteRepository favoriteRepository;
    private final ProductService productService;
    private final FavoriteDtoConverter favoriteDtoConverter;
    private final FavoriteSaveRequestConverter favoriteSaveRequestConverter;
    private static final String PRODUCT_IS_ALREADY_ATTACHED = "the product is already attached";
    private final Logger logger = LoggerFactory.getLogger(FavoriteService.class);


    @Autowired
    public FavoriteService(FavoriteRepository favoriteRepository, ProductService productService, FavoriteDtoConverter favoriteDtoConverter,
                           FavoriteSaveRequestConverter favoriteSaveRequestConverter) {
        this.favoriteRepository = favoriteRepository;
        this.productService = productService;
        this.favoriteDtoConverter = favoriteDtoConverter;
        this.favoriteSaveRequestConverter = favoriteSaveRequestConverter;


    }

    public FavoriteDto addFavoriteProduct(FavoriteSaveRequest favoriteSaveRequest) {

        final Optional<Favorite> favoriteOptional = favoriteRepository.findByUserIdAndProductId(favoriteSaveRequest.getUserId(), favoriteSaveRequest.getProductId());
        if (favoriteOptional.isPresent()) {
            logger.error(PRODUCT_IS_ALREADY_ATTACHED);
            throw new FavoriteAlreadyException(PRODUCT_IS_ALREADY_ATTACHED);
        } else {
            logger.info("favorite object added");
            Favorite favorite = favoriteSaveRequestConverter.favoriteSaveRequestToFavorite(favoriteSaveRequest);
            return favoriteDtoConverter.convertToFavorite(favoriteRepository.save(favorite));
        }

    }

    @Cacheable(cacheNames = "favorite")
    public List<?> getFavorite() {

        final List<FavoriteDto> list = this.favoriteRepository.findAll()
                .stream()
                .map(favoriteDtoConverter::convertToFavorite)
                .collect(Collectors.toList());

        return list.stream()
                .map(product -> productService.findByIdProduct(product.getProductId()))
                .collect(Collectors.toList());
    }

    protected void deleteById(int id) {
        if (favoriteRepository.existsById(id))
            favoriteRepository.deleteById(id);
        else
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND.name());
    }
}
